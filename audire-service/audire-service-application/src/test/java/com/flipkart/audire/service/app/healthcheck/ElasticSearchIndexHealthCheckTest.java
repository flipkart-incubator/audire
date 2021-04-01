package com.flipkart.audire.service.app.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.google.inject.Provider;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.flipkart.audire.service.app.healthcheck.ElasticSearchIndexHealthCheck.KEY_HEALTHCHECK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ElasticSearchIndexHealthCheckTest {

    @Mock
    private RestHighLevelClient mockClient;

    @Mock
    private ESClientConfiguration mockClientConfig;

    private ElasticSearchIndexHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        Provider<RestHighLevelClient> mockProvider = mock(Provider.class);
        lenient().when(mockProvider.get()).thenReturn(mockClient);
        this.healthCheck = new ElasticSearchIndexHealthCheck(mockProvider, mockClientConfig);
    }

    @Test
    void testCheckReturnsHealthyWhenGetIndexRequestReturnsSuccess() throws Exception {
        GetResponse mockResponse = mock(GetResponse.class);
        when(mockResponse.getIndex()).thenReturn("I1");
        when(mockClient.get(any())).thenReturn(mockResponse);

        HealthCheck.Result result = healthCheck.check();
        assertTrue(result.isHealthy());
        assertTrue(result.getMessage().contains("I1"));
        verify(mockClient, times(1)).get(any());
    }

    @Test
    void testCheckReturnsUnhealthyWhenGetIndexRequestFails() throws Exception {
        doThrow(new RuntimeException("Expected Error")).when(mockClient).get(any());
        HealthCheck.Result result = healthCheck.check();

        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("Expected Error"));
        verify(mockClient, times(1)).get(any());
    }

    @Test
    void testGetName() {
        assertEquals(KEY_HEALTHCHECK, healthCheck.getName());
    }
}
