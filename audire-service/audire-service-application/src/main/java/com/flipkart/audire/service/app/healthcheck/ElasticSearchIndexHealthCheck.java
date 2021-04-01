package com.flipkart.audire.service.app.healthcheck;

import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

/**
 * This healthcheck reports if, for a healthy ElasticSearch cluster, the index specified in the
 * client configuration exists.
 */
@Singleton
public class ElasticSearchIndexHealthCheck extends NamedHealthCheck {

    private final Provider<RestHighLevelClient> esClientProvider;
    private final ESClientConfiguration config;

    static final String KEY_HEALTHCHECK = "es-index-status";

    @Inject
    public ElasticSearchIndexHealthCheck(Provider<RestHighLevelClient> esClientProvider, ESClientConfiguration config) {
        this.esClientProvider = esClientProvider;
        this.config = config;
    }

    @Override
    public String getName() {
        return KEY_HEALTHCHECK;
    }

    @Override
    protected Result check() {
        try {
            GetRequest request = getIndexExistsRequest();
            GetResponse response = esClientProvider.get().get(request);
            return Result.healthy(response.getIndex());

        } catch (Exception ex) {
            return Result.unhealthy(ex.getMessage());
        }
    }

    /**
     * Since index exist request cannot be invoked from a high level client in lower ES versions,
     * the behaviour is simulated using a normal GET request to the index and type using a default
     * (_) id. In case the index is present, the request invocation will lead to a 2xx regardless
     * of the id. If the index / type is wrong, an exception will be raised.
     *
     * @apiNote This behaviour should be removed once ES cluster is migrated to newer versions
     */
    private GetRequest getIndexExistsRequest() {
        return Requests.getRequest(config.getIndex())
                .type(config.getType())
                .id("_");
    }
}
