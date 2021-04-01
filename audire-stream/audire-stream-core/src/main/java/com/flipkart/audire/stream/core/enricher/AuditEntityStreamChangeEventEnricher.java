package com.flipkart.audire.stream.core.enricher;

import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;

public interface AuditEntityStreamChangeEventEnricher {

    /**
     * Enriches an audit stream entity change event through various data sources.
     * The original event should be treated as immutable. Instead, a new copy of the
     * original event with the enriched attributes should be returned.
     * <p>
     * This may or may not be a pure function and multiple invocations might result
     * in different enrichment candidates.
     *
     * @throws AuditEntityStreamChangeEventEnrichmentException if event cannot be enriched
     */
    AuditStreamEntityChangeEvent enrich(AuditStreamEntityChangeEvent event);
}
