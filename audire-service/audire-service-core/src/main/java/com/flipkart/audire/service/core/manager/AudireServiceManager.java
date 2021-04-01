package com.flipkart.audire.service.core.manager;

import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.service.client.AuditStoreClient;
import com.flipkart.audire.service.core.builder.AuditAPIResponseBuilder;
import com.flipkart.audire.service.core.exception.AuditLogFetchException;
import com.flipkart.audire.service.model.Filter;
import com.flipkart.audire.service.model.Pagination;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class AudireServiceManager {

    private final AuditStoreClient storeClient;
    private final AuditAPIResponseBuilder searchResponseBuilder;

    @Inject
    public AudireServiceManager(AuditStoreClient storeClient, AuditAPIResponseBuilder responseBuilder) {
        this.storeClient = storeClient;
        this.searchResponseBuilder = responseBuilder;
    }

    public AuditLogFetchAPIResponse getAudits(AuditLogFetchAPIRequest request, Pagination pagination, Filter filter) {
        try {
            SearchResponse searchResponse = storeClient.getSearchResponse(request, pagination, filter);
            long totalHits = searchResponse.getHits().getTotalHits();
            SearchHit[] hits = searchResponse.getHits().getHits();
            List<AuditLogFetchAPIResponse.Audit> audits = collectAudits(hits);
            int currentHits = (pagination.getPageNumber() - 1) * pagination.getLimit() + hits.length;

            return AuditLogFetchAPIResponse.builder()
                    .total(totalHits)
                    .pageCount(hits.length)
                    .audits(audits)
                    .hasNextPage(currentHits < totalHits)
                    .build();

        } catch (Exception ex) {
            throw new AuditLogFetchException(ex.getMessage(), ex);
        }
    }

    private List<AuditLogFetchAPIResponse.Audit> collectAudits(SearchHit[] hits) {
        return Stream.of(hits)
                .map(SearchHit::getSourceAsMap)
                .filter(Objects::nonNull)
                .map(searchResponseBuilder::buildAuditResponse)
                .collect(Collectors.toList());
    }
}
