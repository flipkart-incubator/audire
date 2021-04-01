package com.flipkart.audire.service.app.module;

import com.flipkart.audire.service.app.config.AudireServiceAppConfiguration;
import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.flipkart.audire.service.core.module.AudireServiceCoreModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class AudireServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AudireServiceCoreModule());
    }

    @Provides
    @Singleton
    private ESClientConfiguration getESClientConfiguration(AudireServiceAppConfiguration configuration) {
        return configuration.getEsClientConfig();
    }
}
