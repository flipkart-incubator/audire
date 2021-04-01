package com.flipkart.audire.stream.core.processor;

public class AuditEntityStreamChangeEventProcessorException extends RuntimeException {

    public AuditEntityStreamChangeEventProcessorException(String message, Throwable ex) {
        super(message, ex);
    }
}
