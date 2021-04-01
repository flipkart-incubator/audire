package com.flipkart.audire.service.client;

import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
@Singleton
public class ManagedElasticSearchClient implements Managed, Provider<RestHighLevelClient> {

    private final ESClientConfiguration config;
    private RestHighLevelClient highLevelClient;
    private RestClient restClient;

    @Inject
    public ManagedElasticSearchClient(ESClientConfiguration clientConfig) {
        this.config = clientConfig;
    }

    @Override
    public void start() {
        log.debug("Starting Managed Elastic Search Client with config {}", config);
        HttpHost httpHost = new HttpHost(config.getHost(), config.getPort(), config.getScheme());

        this.restClient = getLowLevelRestClient(httpHost);
        this.highLevelClient = new RestHighLevelClient(this.restClient);
        log.debug("Started Managed Elastic Search Client");
    }

    @Override
    public void stop() throws Exception {
        if (this.restClient != null) {
            log.debug("Stopping Managed Elastic Search Client");

            this.restClient.close();
            log.debug("Stopped Managed Elastic Search Client");
        }
    }

    @Override
    public RestHighLevelClient get() {
        return Preconditions.checkNotNull(this.highLevelClient, "Elastic Search Client is not initialized yet");
    }

    @VisibleForTesting
    RestClient getLowLevelRestClient(HttpHost httpHost) {
        return RestClient.builder(httpHost).build();
    }
}
