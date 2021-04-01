package com.flipkart.audire.service.core.module;

import com.flipkart.audire.service.client.module.AudireServiceClientModule;
import com.google.inject.AbstractModule;

public class AudireServiceCoreModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AudireServiceClientModule());
    }
}
