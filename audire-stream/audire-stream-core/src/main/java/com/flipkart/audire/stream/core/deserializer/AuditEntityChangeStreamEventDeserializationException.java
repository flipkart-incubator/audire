package com.flipkart.audire.stream.core.deserializer;

public class AuditEntityChangeStreamEventDeserializationException extends RuntimeException {

    public AuditEntityChangeStreamEventDeserializationException(Throwable ex) {
        super(ex);
    }
}
