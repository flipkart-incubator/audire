package com.flipkart.audire.stream.core.indexer;

import com.flipkart.audire.stream.model.ChangelogRecord;
import org.elasticsearch.action.index.IndexRequest;

public interface ChangelogIndexRequestBuilder {

    /**
     * Builds an index request for a given non null change log record. If the change log is null,
     * this method should returns null as well.
     *
     * @throws ChangelogIndexRequestBuilderException if there's an exception building the index request.
     */
    IndexRequest buildRequest(ChangelogRecord record);
}
