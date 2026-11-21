package com.recoverx.conversation.exception;
public class InvoiceIntegrationException extends RuntimeException {
    public InvoiceIntegrationException(String message, Throwable cause) { super(message, cause); }
    public InvoiceIntegrationException(String message) { super(message); }
}
