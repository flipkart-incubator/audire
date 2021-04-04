package com.flipkart.audire.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.flipkart.audire.service.model.Filter;
import com.flipkart.audire.service.model.Pagination;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.util.Duration;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AuditStoreClientTest {

    @Mock
    private RestHighLevelClient mockRestClient;

    @Mock
    private ESClientConfiguration mockConfig;

    private AuditStoreClient client;

    @BeforeEach
    void setUp() {
        Provider<RestHighLevelClient> mockProvider = mock(Provider.class);
        when(mockProvider.get()).thenReturn(mockRestClient);
        when(mockConfig.getQueryTimeout()).thenReturn(Duration.seconds(10));
        when(mockConfig.getIndex()).thenReturn("index");

        this.client = new AuditStoreClient(mockProvider, mockConfig);
    }

    @Test
    void testGetSearchResponseWhenOnlySystemicIsSetToTrue() throws Exception {
        AuditLogFetchAPIRequest request = stubFetchAPIRequest();
        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        when(mockRestClient.search(requestCaptor.capture())).thenReturn(mock(SearchResponse.class));

        SearchResponse searchResponse = client.getSearchResponse(request, stubPagination(), stubFilter());
        SearchRequest searchRequest = requestCaptor.getValue();

        String expectedDocument = FixtureHelpers.fixture("fixtures/search_request_document.json");
        String actualDocument = new String(searchRequest.source().buildAsBytes(XContentType.JSON).toBytesRef().bytes);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedDocument), mapper.readTree(actualDocument));
        assertNotNull(searchResponse);
        verify(mockRestClient, times(1)).search(any());
    }

    private AuditLogFetchAPIRequest stubFetchAPIRequest() {
        return AuditLogFetchAPIRequest.builder()
                .entityId(Sets.newHashSet("E1", "E2")).entityType(AuditEntityType.USER_ROLE_AUDIT)
                .actor(Collections.singleton("A1")).ownerId(Sets.newHashSet("O1", "O2"))
                .changedFields(Lists.newArrayList(
                        new AuditLogFetchAPIRequest.Field("F1"),
                        new AuditLogFetchAPIRequest.Field("F2")
                ))
                .metaFilters(Lists.newArrayList(
                        new AuditLogFetchAPIRequest.MetaFilter("K1", "V1")
                )).onlySystemic(true)
                .includeFirst(false)
                .eventTraceId("ET1")
                .build();
    }

    private Filter stubFilter() {
        return Filter.builder().fromDate("F1").toDate("D1").build();
    }

    private Pagination stubPagination() {
        return Pagination.builder().sortOn("S1")
                .pageNumber(2).offset(3).limit(10)
                .ascending(true)
                .build();
    }
}
