package com.flipkart.audire.stream.core.enricher;

public class AuditEntityStreamChangeEventEnrichmentException extends RuntimeException {

    public AuditEntityStreamChangeEventEnrichmentException(String message) {
        super(message);
    }

    public AuditEntityStreamChangeEventEnrichmentException(String message, Throwable ex) {
        super(message, ex);
    }
}
