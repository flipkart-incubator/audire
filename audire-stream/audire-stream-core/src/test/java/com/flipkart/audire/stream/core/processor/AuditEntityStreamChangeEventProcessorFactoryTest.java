package com.flipkart.audire.stream.core.processor;

import com.flipkart.audire.stream.model.EntityType;
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
        this.factory = new AuditEntityStreamChangeEventProcessorFactory(ImmutableMap.<EntityType, AuditEntityStreamChangeEventProcessor>builder()
                .put(EntityType.USER_ROLE_AUDIT, configuration)
                .build());
    }

    @Test
    void testGetReturnsValidatorWhenKeyIsPresent() {
        assertEquals(configuration, factory.get(EntityType.USER_ROLE_AUDIT));
    }

    @Test
    void testGetThrowsExceptionWhenWhenKeyIsNotPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.get(EntityType.AD_ACCOUNT_AUDIT));
        assertTrue(exception.getMessage().contains("No Audit Entity Change Event Processor available for entity"));
    }
}
