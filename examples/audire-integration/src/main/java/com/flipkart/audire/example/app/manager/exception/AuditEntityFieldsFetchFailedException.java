package com.flipkart.audire.example.app.manager.exception;

public class AuditEntityFieldsFetchFailedException extends RuntimeException {

    public AuditEntityFieldsFetchFailedException(Throwable cause) {
        super(cause);
    }

    public AuditEntityFieldsFetchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
