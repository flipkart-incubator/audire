package com.flipkart.audire.service.app.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Provider;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.flipkart.audire.service.app.healthcheck.ElasticSearchClusterHealthCheck.KEY_HEALTHCHECK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ElasticSearchClusterHealthCheckTest {

    @Mock
    private RestHighLevelClient mockClient;

    private ElasticSearchClusterHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        Provider<RestHighLevelClient> mockProvider = mock(Provider.class);
        lenient().when(mockProvider.get()).thenReturn(mockClient);
        this.healthCheck = new ElasticSearchClusterHealthCheck(mockProvider);
    }

    @Test
    void testCheckReturnsHealthyWhenClientPingsSuccessfully() throws Exception {
        when(mockClient.ping()).thenReturn(true);
        HealthCheck.Result result = healthCheck.check();
        assertTrue(result.isHealthy());
    }

    @Test
    void testCheckReturnsUnhealthyClientPingFails() throws Exception {
        when(mockClient.ping()).thenReturn(false);
        HealthCheck.Result result = healthCheck.check();
        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("Ping failed"));
    }

    @Test
    void testGetName() {
        assertEquals(KEY_HEALTHCHECK, healthCheck.getName());
    }
}
