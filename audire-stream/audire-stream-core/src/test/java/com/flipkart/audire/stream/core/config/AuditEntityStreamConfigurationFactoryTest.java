package com.flipkart.audire.stream.core.config;

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
class AuditEntityStreamConfigurationFactoryTest {

    @Mock
    private AuditEntityStreamConfiguration configuration;

    private AuditEntityStreamConfigurationFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new AuditEntityStreamConfigurationFactory(ImmutableMap.<EntityType, AuditEntityStreamConfiguration>builder()
                .put(EntityType.CAMPAIGN_AUDIT, configuration)
                .build());
    }

    @Test
    void testGetReturnsValidatorWhenKeyIsPresent() {
        assertEquals(configuration, factory.get(EntityType.CAMPAIGN_AUDIT));
    }

    @Test
    void testGetThrowsExceptionWhenWhenKeyIsNotPresent() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factory.get(EntityType.AD_PRODUCT_AUDIT));
        assertTrue(exception.getMessage().contains("No Audit Entity Stream Configuration available for entity"));
    }
}
