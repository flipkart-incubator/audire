package com.flipkart.audire.service.client;

import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.client.config.ESClientConfiguration;
import com.flipkart.audire.service.model.Filter;
import com.flipkart.audire.service.model.Pagination;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_ACTOR;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_AUDIT_PREV_ID;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_CHANGED_AT;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_CHANGES;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_CHANGES_CHANGED_FIELD;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_ENTITY_ID;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_ENTITY_TYPE;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_EVENT_TRACE_ID;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_EXTRAS;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_EXTRAS_KEY;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_EXTRAS_VAL;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_OWNER_ID;
import static com.flipkart.audire.service.client.constants.DocumentConstants.KEY_SYSTEMIC;

@Singleton
public class AuditStoreClient {

    private final Provider<RestHighLevelClient> esClientProvider;
    private final ESClientConfiguration config;

    @Inject
    public AuditStoreClient(Provider<RestHighLevelClient> esClientProvider, ESClientConfiguration config) {
        this.esClientProvider = esClientProvider;
        this.config = config;
    }

    public SearchResponse getSearchResponse(AuditLogFetchAPIRequest request, Pagination pagination, Filter filter) throws IOException {
        SearchRequest searchRequest = new SearchRequest(config.getIndex());
        BoolQueryBuilder boolQuery = buildEntityFilter(request);

        buildSystemicFilter(request, boolQuery);
        buildChangedFieldIdsFilter(request, boolQuery);
        buildOwnerActorFilter(request, boolQuery);
        buildMetaFilter(request, boolQuery);
        buildDateFilter(filter, boolQuery);

        SearchSourceBuilder sourceBuilder = buildSearchSourceBuilder(searchRequest, boolQuery);
        buildPagination(pagination, sourceBuilder);
        return esClientProvider.get().search(searchRequest);
    }

    /**
     * Entity Type is mandatory. Only a single entity can be fetched. However, multiple entity ids can
     * be passed which are ORd together.
     * Multiple docs can carry similar event trace ids which is useful for grouping related entities.
     */
    private BoolQueryBuilder buildEntityFilter(AuditLogFetchAPIRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(KEY_ENTITY_TYPE, request.getEntityType()));

        if (CollectionUtils.isNotEmpty(request.getEntityId())) {
            boolQuery.filter(QueryBuilders.termsQuery(KEY_ENTITY_ID, request.getEntityId()));
        }
        if (StringUtils.isNotBlank(request.getEventTraceId())) {
            boolQuery.filter(QueryBuilders.existsQuery(KEY_EVENT_TRACE_ID));
            boolQuery.filter(QueryBuilders.termQuery(KEY_EVENT_TRACE_ID, request.getEventTraceId()));
        }
        return boolQuery;
    }

    /**
     * Multiple owner ids and actors can be passed which are OR'ed individually and AND'd
     * in conjunction with each other.
     */
    private void buildOwnerActorFilter(AuditLogFetchAPIRequest request, BoolQueryBuilder boolQuery) {
        if (CollectionUtils.isNotEmpty(request.getOwnerId())) {
            boolQuery.filter(QueryBuilders.termsQuery(KEY_OWNER_ID, request.getOwnerId()));
        }
        if (CollectionUtils.isNotEmpty(request.getActor())) {
            boolQuery.filter(QueryBuilders.termsQuery(KEY_ACTOR, request.getActor()));
        }
    }

    /**
     * Multiple changed field ids can be passed which are OR'ed together
     */
    private void buildChangedFieldIdsFilter(AuditLogFetchAPIRequest request, BoolQueryBuilder boolQuery) {
        if (CollectionUtils.isNotEmpty(request.getChangedFields())) {
            Set<String> fieldIds = request.getChangedFields().stream()
                    .map(AuditLogFetchAPIRequest.Field::getField)
                    .collect(Collectors.toSet());

            boolQuery.filter(QueryBuilders.nestedQuery(KEY_CHANGES,
                    QueryBuilders.termsQuery(KEY_CHANGES_CHANGED_FIELD, fieldIds), ScoreMode.None));
        }
    }

    /**
     * Multiple meta filters can be specified which are OR'd together. Each key / value term
     * within the filter is AND'd together.
     */
    private void buildMetaFilter(AuditLogFetchAPIRequest request, BoolQueryBuilder boolQuery) {
        if (CollectionUtils.isNotEmpty(request.getMetaFilters())) {
            BoolQueryBuilder nestedBuilder = QueryBuilders.boolQuery();

            request.getMetaFilters().stream()
                    .map(metaFilter -> QueryBuilders.boolQuery()
                            .filter(QueryBuilders.termQuery(KEY_EXTRAS_KEY, metaFilter.getKey()))
                            .filter(QueryBuilders.termQuery(KEY_EXTRAS_VAL, metaFilter.getVal()))
                    )
                    .forEach(nestedBuilder::should);

            boolQuery.filter(QueryBuilders.nestedQuery(KEY_EXTRAS, nestedBuilder, ScoreMode.None));
        }
    }

    /**
     * Systemic filter can be applied both as the "only" and "inclusive" (normal + systemic)
     * Documents that have no previous audit id are assumed to be the first audit entry.
     */
    private void buildSystemicFilter(AuditLogFetchAPIRequest request, BoolQueryBuilder boolQuery) {
        if (request.isOnlySystemic()) {
            boolQuery.filter(QueryBuilders.termQuery(KEY_SYSTEMIC, true));

        } else if (!request.isIncludeSystemic()) {
            boolQuery.filter(QueryBuilders.termQuery(KEY_SYSTEMIC, request.isIncludeSystemic()));
        }
        if (!request.isIncludeFirst()) {
            boolQuery.filter(QueryBuilders.existsQuery(KEY_AUDIT_PREV_ID));
        }
    }

    private SearchSourceBuilder buildSearchSourceBuilder(SearchRequest searchRequest, BoolQueryBuilder boolQuery) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);
        sourceBuilder.timeout(new TimeValue(config.getQueryTimeout().toSeconds(), TimeUnit.SECONDS));
        return sourceBuilder;
    }

    private void buildPagination(Pagination pagination, SearchSourceBuilder sourceBuilder) {
        int offset = (pagination.getPageNumber() - 1) * pagination.getLimit();
        SortOrder sortOrder = pagination.isAscending() ? SortOrder.ASC : SortOrder.DESC;

        sourceBuilder.from(offset);
        sourceBuilder.size(pagination.getLimit());
        sourceBuilder.sort(new FieldSortBuilder(pagination.getSortOn()).order(sortOrder));
    }

    private void buildDateFilter(Filter filter, BoolQueryBuilder boolQuery) {
        if (filter != null) {
            boolQuery.filter(QueryBuilders.rangeQuery(KEY_CHANGED_AT)
                    .from(filter.getFromDate(), true)
                    .to(filter.getToDate(), true)
            );
        }
    }
}
