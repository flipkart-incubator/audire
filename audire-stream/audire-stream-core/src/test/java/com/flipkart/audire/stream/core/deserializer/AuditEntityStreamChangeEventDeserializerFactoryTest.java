package com.flipkart.audire.stream.core.deserializer;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AuditEntityStreamChangeEventDeserializerFactoryTest {

    @Mock
    private AuditEntityStreamChangeEventDeserializer configuration;

    private AuditEntityStreamChangeEventDeserializerFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new AuditEntityStreamChangeEventDeserializerFactory(ImmutableMap.<String, AuditEntityStreamChangeEventDeserializer>builder()
                .put("topic-1", configuration)
                .build());
    }

    @Test
    void testGetReturnsValidatorWhenKeyIsPresent() {
        assertEquals(configuration, factory.get("topic-1"));
    }

    @Test
    void testGetThrowsExceptionWhenWhenKeyIsNotPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.get("topic-2"));
        assertTrue(exception.getMessage().contains("No Audit Entity Stream Change Event Deserializer configured for topic"));
    }
}
