package com.flipkart.audire.stream.core.processor;

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
class AuditEntityStreamChangeEventProcessorFactoryTest {

    @Mock
    private AuditEntityStreamChangeEventProcessor configuration;

    private AuditEntityStreamChangeEventProcessorFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new AuditEntityStreamChangeEventProcessorFactory(ImmutableMap.<String, AuditEntityStreamChangeEventProcessor>builder()
                .put("A", configuration)
                .build());
    }

    @Test
    void testGetReturnsValidatorWhenKeyIsPresent() {
        assertEquals(configuration, factory.get("A"));
    }

    @Test
    void testGetThrowsExceptionWhenWhenKeyIsNotPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.get("B"));
        assertTrue(exception.getMessage().contains("No Audit Entity Change Event Processor available for entity"));
    }
}
