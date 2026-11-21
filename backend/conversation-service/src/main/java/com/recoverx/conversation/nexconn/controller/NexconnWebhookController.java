package com.recoverx.conversation.nexconn.controller;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.recoverx.conversation.client.AIServiceClient;
import com.recoverx.conversation.client.CommitmentServiceClient;
import com.recoverx.conversation.dto.AICommitmentRequest;
import com.recoverx.conversation.dto.InternalAIAnalysisRequest;
import com.recoverx.conversation.entity.Conversation;
import com.recoverx.conversation.nexconn.config.NexconnProperties;
import com.recoverx.conversation.nexconn.entity.NexconnWebhookEvent;
import com.recoverx.conversation.nexconn.repository.NexconnWebhookEventRepository;
import com.recoverx.conversation.nexconn.service.NexconnService;
import com.recoverx.conversation.nexconn.service.NexconnSignature;
import com.recoverx.conversation.repository.ConversationRepository;

@RestController
@RequestMapping("/api/nexconn/webhook")
public class NexconnWebhookController {
    private final NexconnProperties properties;
    private final NexconnSignature signature;
    private final NexconnWebhookEventRepository eventRepository;
    private final ConversationRepository conversationRepository;
    private final NexconnService nexconnService;
    private final AIServiceClient aiServiceClient;
    private final CommitmentServiceClient commitmentServiceClient;
    private final ObjectMapper objectMapper;
    private final String timezone;

    public NexconnWebhookController(NexconnProperties properties, NexconnSignature signature,
            NexconnWebhookEventRepository eventRepository,
            ConversationRepository conversationRepository, NexconnService nexconnService,
            AIServiceClient aiServiceClient, CommitmentServiceClient commitmentServiceClient, ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${recoverx.default-timezone:Asia/Kolkata}") String timezone) {
        this.properties = properties;
        this.signature = signature;
        this.eventRepository = eventRepository;
        this.conversationRepository = conversationRepository;
        this.nexconnService = nexconnService;
        this.aiServiceClient = aiServiceClient;
        this.commitmentServiceClient = commitmentServiceClient;
        this.objectMapper = objectMapper;
        this.timezone = timezone;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader("AppKey") String appKey,
            @RequestHeader("Nonce") String nonce,
            @RequestHeader("Timestamp") String timestamp,
            @RequestHeader("Signature") String suppliedSignature,
            @RequestBody String rawBody) {
        verify(appKey, nonce, timestamp, suppliedSignature);
        try {
            JsonNode envelope = objectMapper.readTree(rawBody);
            String eventId = envelope.path("id").asText();
            String eventType = envelope.path("type").asText();
            JsonNode data = envelope.path("data");
            if (eventId.isBlank() || eventType.isBlank() || !data.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Nexconn event envelope");
            }

            NexconnWebhookEvent stored = eventRepository.findByEventId(eventId).orElseGet(() -> {
                NexconnWebhookEvent fresh = new NexconnWebhookEvent();
                fresh.setEventId(eventId);
                fresh.setEventType(eventType);
                fresh.setPayload(rawBody);
                try {
                    return eventRepository.save(fresh);
                } catch (DataIntegrityViolationException duplicate) {
                    return eventRepository.findByEventId(eventId).orElseThrow(() -> duplicate);
                }
            });

            if (stored.isProcessed()) return ResponseEntity.ok().build();

            if (!"message:send".equalsIgnoreCase(eventType)) {
                markProcessed(stored, "IGNORED_EVENT_TYPE");
                return ResponseEntity.ok().build();
            }

            for (JsonNode message : data) {
                processMessage(message, stored);
            }
            stored.setProcessed(true);
            if (stored.getProcessingStatus() == null) stored.setProcessingStatus("PROCESSED");
            eventRepository.save(stored);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid Nexconn webhook JSON payload", ex);
        }
    }

    private void processMessage(JsonNode message, NexconnWebhookEvent event) throws Exception {
        String senderNexconnId = message.path("userId").asText("");
        String channelId = message.path("channelId").asText("");
        String text = extractMessageText(message.path("content"));
        if (senderNexconnId.isBlank() || channelId.isBlank() || text.isBlank()) return;

        List<Conversation> matches = new ArrayList<>();
        // Nexconn direct channel ID is the other participant's user ID.
        for (Conversation candidate : conversationRepository.findAll()) {
            if (candidate.getOwnerUserId() == null || candidate.getCustomerUserId() == null) continue;
            String ownerNexconnId = nexconnService.toNexconnUserId(candidate.getOwnerUserId());
            String customerNexconnId = nexconnService.toNexconnUserId(candidate.getCustomerUserId());
            boolean customerSentToOwner = senderNexconnId.equals(customerNexconnId)
                    && channelId.equals(ownerNexconnId);
            if (customerSentToOwner) matches.add(candidate);
        }

        // Never guess the tenant or invoice if this Nexconn pair maps to multiple business conversations.
        if (matches.size() != 1) {
            event.setProcessingStatus(matches.isEmpty()
                    ? "SKIPPED_CONVERSATION_NOT_FOUND" : "SKIPPED_AMBIGUOUS_CONVERSATION");
            eventRepository.save(event);
            return;
        }
        Conversation conversation = matches.get(0);

        // Do not analyze invoice promises without an explicit invoice context.
        if (conversation.getInvoiceId() == null) {
            event.setProcessingStatus("SKIPPED_NO_INVOICE_CONTEXT");
            eventRepository.save(event);
            return;
        }

        InternalAIAnalysisRequest request = new InternalAIAnalysisRequest();
        request.setBusinessId(conversation.getBusinessId());
        request.setInvoiceId(conversation.getInvoiceId());
        request.setConversationId(conversation.getId());
        request.setSourceMessageId(stableNumericMessageId(message.path("messageId").asText("")));
        request.setMessage(text);
        request.setMessageTimestamp(parseMessageTime(message.path("time")));
        request.setTimezone(ZoneId.of(timezone).getId());

        JsonNode analysis = aiServiceClient.analyze(request);
        event.setAiAnalysisJson(objectMapper.writeValueAsString(analysis));
        event.setProcessingStatus("AI_ANALYZED");
        eventRepository.save(event);

        // Only a positive PAYMENT_PROMISE creates a PENDING_REVIEW commitment.
        // The Commitment Service remains responsible for idempotency and invoice validation.
        if ("PAYMENT_PROMISE".equalsIgnoreCase(analysis.path("analysisType").asText())
                && analysis.path("paymentIntent").asBoolean(false)) {
            AICommitmentRequest commitmentRequest = new AICommitmentRequest();
            commitmentRequest.setInvoiceId(conversation.getInvoiceId());
            commitmentRequest.setAnalysisType(analysis.path("analysisType").asText());
            commitmentRequest.setPaymentIntent(true);
            commitmentRequest.setConversationId(conversation.getId());
            commitmentRequest.setSourceMessageId(String.valueOf(request.getSourceMessageId()));
            commitmentRequest.setSummary(analysis.path("summary").asText(text));
            if (analysis.hasNonNull("amount") && !analysis.path("amount").isNull()) {
                commitmentRequest.setAmount(new java.math.BigDecimal(analysis.path("amount").asText()));
            }
            if (analysis.hasNonNull("proposedDate") && !analysis.path("proposedDate").asText().isBlank()) {
                commitmentRequest.setProposedDate(java.time.LocalDate.parse(analysis.path("proposedDate").asText()));
            }
            commitmentServiceClient.createFromAIAnalysis(conversation.getBusinessId(), commitmentRequest);
            event.setProcessingStatus("COMMITMENT_CREATED_PENDING_REVIEW");
            eventRepository.save(event);
        }
    }

    private String extractMessageText(JsonNode content) {
        if (content == null || content.isMissingNode() || content.isNull()) return "";
        JsonNode parsed = content;
        if (content.isTextual()) {
            String raw = content.asText();
            try { parsed = objectMapper.readTree(raw); }
            catch (Exception ignored) { return raw; }
        }
        String value = findText(parsed);
        return value == null ? "" : value.trim();
    }

    private String findText(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isObject()) {
            for (String key : List.of("text", "content", "message")) {
                JsonNode child = node.get(key);
                if (child != null) {
                    String found = findText(child);
                    if (found != null && !found.isBlank()) return found;
                }
            }
            var fields = node.properties().iterator();
            while (fields.hasNext()) {
                String found = findText(fields.next().getValue());
                if (found != null && !found.isBlank()) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String found = findText(child);
                if (found != null && !found.isBlank()) return found;
            }
        }
        return null;
    }

    private Instant parseMessageTime(JsonNode timeNode) {
        if (timeNode == null || timeNode.isMissingNode() || timeNode.isNull()) return Instant.now();
        try {
            if (timeNode.isNumber()) {
                long value = timeNode.asLong();
                return value > 10_000_000_000L ? Instant.ofEpochMilli(value) : Instant.ofEpochSecond(value);
            }
            String value = timeNode.asText();
            try { return Instant.parse(value); }
            catch (Exception ignored) { return Instant.ofEpochMilli(Long.parseLong(value)); }
        } catch (Exception ignored) {
            return Instant.now();
        }
    }

    private Long stableNumericMessageId(String messageId) throws Exception {
        if (messageId != null && messageId.matches("\\d+")) {
            try {
                long numeric = Long.parseLong(messageId);
                if (numeric > 0) return numeric;
            } catch (NumberFormatException ignored) {}
        }
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest((messageId == null ? "" : messageId).getBytes(StandardCharsets.UTF_8));
        long value = ByteBuffer.wrap(hash).getLong() & Long.MAX_VALUE;
        return value == 0 ? 1L : value;
    }

    private void markProcessed(NexconnWebhookEvent event, String status) {
        event.setProcessed(true);
        event.setProcessingStatus(status);
        eventRepository.save(event);
    }

    private void verify(String appKey, String nonce, String timestamp, String suppliedSignature) {
        if (properties.getAppKey() == null || properties.getAppSecret() == null
                || !properties.getAppKey().equals(appKey) || nonce == null || nonce.isBlank()
                || nonce.length() > 18 || timestamp == null || suppliedSignature == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Nexconn webhook signature");
        }
        try {
            long eventTime = Long.parseLong(timestamp);
            long ageSeconds = Math.abs(Instant.now().toEpochMilli() - eventTime) / 1000;
            if (ageSeconds > properties.getWebhookClockSkewSeconds()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired Nexconn webhook request");
            }
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Nexconn webhook timestamp");
        }
        String expected = signature.sign(properties.getAppSecret(), nonce, timestamp);
        if (!signature.matches(expected, suppliedSignature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Nexconn webhook signature");
        }
    }
}
