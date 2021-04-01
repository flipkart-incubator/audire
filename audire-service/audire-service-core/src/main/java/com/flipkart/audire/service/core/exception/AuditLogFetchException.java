package com.flipkart.audire.service.core.exception;

public class AuditLogFetchException extends RuntimeException {

    public AuditLogFetchException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }

    public AuditLogFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
