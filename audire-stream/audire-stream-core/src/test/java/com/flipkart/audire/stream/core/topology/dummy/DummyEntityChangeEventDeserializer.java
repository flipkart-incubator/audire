package com.flipkart.audire.stream.core.topology.dummy;

import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.flipkart.audire.stream.core.deserializer.AuditEntityStreamChangeEventDeserializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DummyEntityChangeEventDeserializer extends AuditEntityStreamChangeEventDeserializer {

    @Inject
    public DummyEntityChangeEventDeserializer(AuditEntityStreamConfigurationFactory factory) {
        super("DUMMY_ENTITY_AUDIT", factory);
    }
}
