package com.recoverx.customer.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.recoverx.customer.client.BusinessMembershipClient;
import com.recoverx.customer.dto.AcceptInvitationRequest;
import com.recoverx.customer.dto.InvitationResponse;
import com.recoverx.customer.entity.Customer;
import com.recoverx.customer.entity.CustomerInvitation;
import com.recoverx.customer.entity.InvitationStatus;
import com.recoverx.customer.exception.CustomerNotFoundException;
import com.recoverx.customer.repository.CustomerInvitationRepository;
import com.recoverx.customer.repository.CustomerRepository;

@Service
public class CustomerInvitationService {

    private static final int TOKEN_BYTES = 32;
    private static final long INVITATION_VALID_HOURS = 48;

    private final CustomerRepository customerRepository;
    private final CustomerInvitationRepository invitationRepository;
    private final BusinessMembershipClient membershipClient;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String invitationUrl;

    public CustomerInvitationService(
            CustomerRepository customerRepository,
            CustomerInvitationRepository invitationRepository,
            BusinessMembershipClient membershipClient,
            JavaMailSender mailSender,
            @Value("${app.frontend.invitation-url}") String invitationUrl) {
        this.customerRepository = customerRepository;
        this.invitationRepository = invitationRepository;
        this.membershipClient = membershipClient;
        this.mailSender = mailSender;
        this.invitationUrl = invitationUrl;
    }

    @Transactional
    public InvitationResponse inviteCustomer(Long businessId, Long customerId,
            Long inviterUserId, String authorizationHeader) {

        membershipClient.requireInvitationPermission(
                businessId, inviterUserId, authorizationHeader);

        Customer customer = customerRepository.findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with ID: " + customerId
                                + " in business ID: " + businessId));

        if (customer.getUserId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This customer is already linked to a RecoverX account");
        }

        List<CustomerInvitation> previousInvitations =
                invitationRepository.findAllByBusinessIdAndCustomerIdAndStatus(
                        businessId, customerId, InvitationStatus.PENDING);
        for (CustomerInvitation previous : previousInvitations) {
            previous.setStatus(InvitationStatus.REVOKED);
        }
        invitationRepository.saveAll(previousInvitations);

        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = sha256(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(INVITATION_VALID_HOURS);

        CustomerInvitation invitation = new CustomerInvitation();
        invitation.setBusinessId(businessId);
        invitation.setCustomerId(customerId);
        invitation.setInvitedEmail(customer.getEmail().trim().toLowerCase(Locale.ROOT));
        invitation.setTokenHash(tokenHash);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedByUserId(inviterUserId);
        invitation.setExpiresAt(expiresAt);
        invitationRepository.save(invitation);

        String link = invitationUrl + "?businessId=" + businessId + "&token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customer.getEmail());
        message.setSubject("You're invited to connect on RecoverX");
        message.setText("Hello " + customer.getName() + ",\n\n"
                + "A business has invited you to connect with them on RecoverX.\n\n"
                + "Sign in or create your RecoverX account using this email address, then open this link to accept the invitation:\n"
                + link + "\n\n"
                + "This invitation expires in 48 hours and can be used only once.\n\n"
                + "If you were not expecting this invitation, you can ignore this email.");
        mailSender.send(message);

        return new InvitationResponse(
                "Invitation email sent successfully",
                businessId, customerId, customer.getEmail(), expiresAt);
    }

    @Transactional
    public InvitationResponse acceptInvitation(Long businessId,
            AcceptInvitationRequest request, Long authenticatedUserId,
            String authenticatedEmail) {

        String normalizedEmail = authenticatedEmail == null
                ? "" : authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            throw new AccessDeniedException("Authenticated account email is missing");
        }

        CustomerInvitation invitation = invitationRepository
                .findByTokenHash(sha256(request.getToken().trim()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invitation token is invalid"));

        if (!businessId.equals(invitation.getBusinessId())) {
            throw new AccessDeniedException("Invitation does not belong to this business");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Invitation is no longer pending");
        }
        if (!invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE, "Invitation has expired");
        }
        if (!normalizedEmail.equals(invitation.getInvitedEmail())) {
            throw new AccessDeniedException(
                    "Sign in with the email address that received this invitation");
        }

        Customer customer = customerRepository
                .findByIdAndBusinessId(invitation.getCustomerId(), businessId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Invited customer record no longer exists"));

        String customerEmail = customer.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.equals(customerEmail)) {
            throw new AccessDeniedException(
                    "Invitation email no longer matches the customer record");
        }
        if (customer.getUserId() != null
                && !customer.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This customer record is already linked to another account");
        }

        customer.setUserId(authenticatedUserId);
        customerRepository.save(customer);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedByUserId(authenticatedUserId);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        return new InvitationResponse(
                "Invitation accepted successfully",
                businessId, customer.getId(), customer.getEmail(),
                invitation.getExpiresAt());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to secure invitation token", ex);
        }
    }
}
