package com.flipkart.audire.service.client.module;

import com.flipkart.audire.service.client.ManagedElasticSearchClient;
import com.google.inject.AbstractModule;
import org.elasticsearch.client.RestHighLevelClient;

public class AudireServiceClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RestHighLevelClient.class).toProvider(ManagedElasticSearchClient.class);
    }
}
