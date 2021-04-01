package com.flipkart.audire.service.core.manager;

import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.service.client.AuditStoreClient;
import com.flipkart.audire.service.core.builder.AuditAPIResponseBuilder;
import com.flipkart.audire.service.core.exception.AuditLogFetchException;
import com.flipkart.audire.service.model.Pagination;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudireServiceManagerTest {

    @Mock
    private AuditStoreClient mockStoreClient;

    private AudireServiceManager manager;

    @BeforeEach
    void setUp() {
        this.manager = new AudireServiceManager(mockStoreClient, new AuditAPIResponseBuilder());
    }

    @Test
    void testGetAuditsWhenClientThrowsException() throws Exception {
        doThrow(RuntimeException.class).when(mockStoreClient).getSearchResponse(any(), any(), any());
        AuditLogFetchAPIRequest request = AuditLogFetchAPIRequest.builder().build();
        assertThrows(AuditLogFetchException.class, () -> manager.getAudits(request, null, null));
    }

    @Test
    void testGetAuditsWhenResponseWhenStoreClientReturnsSuccessfully() throws Exception {
        SearchResponse searchResponse = mock(SearchResponse.class, Mockito.RETURNS_DEEP_STUBS);
        when(searchResponse.getHits().getTotalHits()).thenReturn(10L);
        when(searchResponse.getHits().getHits()).thenReturn(
                new SearchHit[]{
                        new SearchHit(1), new SearchHit(2)
                }
        );
        when(mockStoreClient.getSearchResponse(any(), any(), any())).thenReturn(searchResponse);
        AuditLogFetchAPIRequest request = AuditLogFetchAPIRequest.builder().build();
        Pagination pagination = Pagination.builder().pageNumber(2).limit(3).build();

        AuditLogFetchAPIResponse response = manager.getAudits(request, pagination, null);
        assertEquals(10L, response.getTotal());
        assertTrue(response.getAudits().isEmpty());
        assertEquals(2, response.getPageCount());
        assertTrue(response.isHasNextPage());
    }
}
