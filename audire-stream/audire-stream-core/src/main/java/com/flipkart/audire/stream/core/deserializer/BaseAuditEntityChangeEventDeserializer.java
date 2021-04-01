package com.flipkart.audire.stream.core.deserializer;

import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.common.serialization.Deserializer;

@Singleton
public class BaseAuditEntityChangeEventDeserializer implements Deserializer<AuditStreamEntityChangeEvent> {

    private final AuditEntityStreamChangeEventDeserializerFactory deserializerFactory;

    @Inject
    public BaseAuditEntityChangeEventDeserializer(AuditEntityStreamChangeEventDeserializerFactory deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }

    @Override
    public AuditStreamEntityChangeEvent deserialize(String topic, byte[] bytes) {
        return this.deserializerFactory.get(topic).deserialize(bytes);
    }
}
