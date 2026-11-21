package com.recoverx.customer.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recoverx.customer.dto.CustomerRequest;
import com.recoverx.customer.dto.CustomerResponse;
import com.recoverx.customer.entity.Customer;
import com.recoverx.customer.exception.CustomerAlreadyExistsException;
import com.recoverx.customer.exception.CustomerNotFoundException;
import com.recoverx.customer.repository.CustomerRepository;
import com.recoverx.customer.utility.CustomerMapper;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @Override
    public List<CustomerResponse> getMyCustomerConnections(Long userId) {
        return customerRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse createCustomer(Long businessId, CustomerRequest request) {
        validateBusinessId(businessId);

        String normalizedEmail = normalizeEmail(request.getEmail());

        if (customerRepository.existsByBusinessIdAndEmailIgnoreCase(
                businessId, normalizedEmail)) {
            throw new CustomerAlreadyExistsException(
                    "A customer with this email already exists in this business");
        }

        Customer customer = CustomerMapper.toEntity(businessId, request);
        customer.setEmail(normalizedEmail);

        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(Long businessId) {
        validateBusinessId(businessId);

        return customerRepository.findAllByBusinessIdOrderByCreatedAtDesc(businessId)
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long businessId, Long customerId) {
        validateBusinessId(businessId);

        Customer customer = findCustomer(businessId, customerId);
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomer(
            Long businessId, Long customerId, CustomerRequest request) {
        validateBusinessId(businessId);

        Customer customer = findCustomer(businessId, customerId);
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (customerRepository.existsByBusinessIdAndEmailIgnoreCaseAndIdNot(
                businessId, normalizedEmail, customerId)) {
            throw new CustomerAlreadyExistsException(
                    "Another customer with this email already exists in this business");
        }

        customer.setName(request.getName().trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(normalizePhone(request.getPhone()));

        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    private Customer findCustomer(Long businessId, Long customerId) {
        return customerRepository.findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with ID: " + customerId
                                + " in business ID: " + businessId));
    }

    private void validateBusinessId(Long businessId) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("Business ID must be a positive number");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
