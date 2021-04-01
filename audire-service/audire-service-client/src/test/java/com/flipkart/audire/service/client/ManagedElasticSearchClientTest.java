package com.flipkart.audire.service.client;

import com.flipkart.audire.service.client.config.ESClientConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagedElasticSearchClientTest {

    @Mock
    private ESClientConfiguration clientConfig;

    private ManagedElasticSearchClient managedClient;

    @Test
    void testClientLifecycle() throws Exception {
        managedClient = spy(new ManagedElasticSearchClient(clientConfig));
        when(clientConfig.getHost()).thenReturn("localhost");
        RestClient mockLowLevelClient = mock(RestClient.class);
        doReturn(mockLowLevelClient).when(managedClient).getLowLevelRestClient(any());

        managedClient.start();
        assertNotNull(managedClient.get());

        managedClient.stop();
        verify(mockLowLevelClient, times(1)).close();
    }

    @Test
    void testLowLevelRestClientIsReturned() {
        managedClient = new ManagedElasticSearchClient(clientConfig);
        assertNotNull(managedClient.getLowLevelRestClient(new HttpHost("H1")));
    }
}
