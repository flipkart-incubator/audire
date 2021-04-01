package com.flipkart.audire.service.app.healthcheck;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.elasticsearch.client.RestHighLevelClient;
import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

/**
 * This health check reports whether the cluster for which the REST high level client has been
 * built is up and running.
 */
@Singleton
public class ElasticSearchClusterHealthCheck extends NamedHealthCheck {

    private final Provider<RestHighLevelClient> esClientProvider;

    static final String KEY_HEALTHCHECK = "es-cluster-health";

    @Inject
    public ElasticSearchClusterHealthCheck(Provider<RestHighLevelClient> esClientProvider) {
        this.esClientProvider = esClientProvider;
    }

    @Override
    protected Result check() throws Exception {
        boolean ping = esClientProvider.get().ping();
        if (ping) {
            return Result.healthy();
        }
        return Result.unhealthy("Elastic Search Cluster is not reachable. Ping failed");
    }

    @Override
    public String getName() {
        return KEY_HEALTHCHECK;
    }
}
