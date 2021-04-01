package com.flipkart.audire.stream.core.enricher;

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
class AuditEntityStreamChangeEventEnricherFactoryTest {

    @Mock
    private AuditEntityStreamChangeEventEnricher configuration;

    private AuditEntityStreamChangeEventEnricherFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new AuditEntityStreamChangeEventEnricherFactory(ImmutableMap.<EntityType, AuditEntityStreamChangeEventEnricher>builder()
                .put(EntityType.BUSINESS_ACCOUNT_AUDIT, configuration)
                .build());
    }

    @Test
    void testGetReturnsValidatorWhenKeyIsPresent() {
        assertEquals(configuration, factory.get(EntityType.BUSINESS_ACCOUNT_AUDIT));
    }

    @Test
    void testGetThrowsExceptionWhenWhenKeyIsNotPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.get(EntityType.PURCHASE_ORDER_AUDIT));
        assertTrue(exception.getMessage().contains("No Audit Entity Change Event Enricher available for entity"));
    }
}
