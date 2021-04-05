package com.flipkart.audire.example.app.module;

import com.flipkart.audire.example.app.config.AudireIntegrationAppConfiguration;
import com.flipkart.audire.stream.core.config.AudireStreamAppConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.flipkart.audire.stream.core.config.AuditStoreSinkConfiguration;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class AudireIntegrationAppModule extends AbstractModule {

    @Provides
    @Singleton
    @Inject
    private AuditEntityStreamConfigurationFactory getAuditEntityConfigurationFactory(AudireIntegrationAppConfiguration config) {
        return new AuditEntityStreamConfigurationFactory(ImmutableMap.<String, AuditEntityStreamConfiguration>builder()
                .put("DUMMY_ENTITY_AUDIT", config.getAudireAppConfig().getAuditStreamEntityConfig().get("DUMMY_ENTITY_AUDIT"))
                .build());
    }

    @Provides
    @Singleton
    private AuditStoreSinkConfiguration getAuditStoreSinkConfiguration(AudireStreamAppConfiguration config) {
        return config.getAuditStoreSinkConfig();
    }
}
