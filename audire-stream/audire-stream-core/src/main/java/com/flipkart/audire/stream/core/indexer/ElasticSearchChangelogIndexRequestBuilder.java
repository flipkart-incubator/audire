package com.flipkart.audire.stream.core.indexer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.audire.stream.model.ChangelogRecord;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

@Slf4j
@Singleton
public class ElasticSearchChangelogIndexRequestBuilder implements ChangelogIndexRequestBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public IndexRequest buildRequest(ChangelogRecord record) {
        if (record != null) {
            try {
                log.info("Indexing change log {}", record);
                String key = getChangeLogRecordKey(record);
                String indexJson = getIndexDocumentJson(record);
                return new IndexRequest().id(key).source(indexJson, XContentType.JSON);

            } catch (Exception ex) {
                throw new ChangelogIndexRequestBuilderException("Error building change log index record " + ex.getMessage(), ex);
            }
        }
        return null;
    }

    private String getIndexDocumentJson(ChangelogRecord record) throws JsonProcessingException {
        return MAPPER.writeValueAsString(record);
    }

    private String getChangeLogRecordKey(ChangelogRecord record) {
        return String.format("%s-%s-%s", record.getEntityType(), record.getEntityId(), record.getVersion());
    }
}
