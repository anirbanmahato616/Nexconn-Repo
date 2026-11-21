package com.recoverx.payment.exception;
public class InvoiceIntegrationException extends RuntimeException {
    public InvoiceIntegrationException(String message) { super(message); }
    public InvoiceIntegrationException(String message, Throwable cause) { super(message, cause); }
}
