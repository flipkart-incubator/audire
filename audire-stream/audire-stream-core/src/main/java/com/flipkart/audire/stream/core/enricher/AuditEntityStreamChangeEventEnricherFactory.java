package com.flipkart.audire.stream.core.enricher;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@AllArgsConstructor
public class AuditEntityStreamChangeEventEnricherFactory {

    private final Map<String, AuditEntityStreamChangeEventEnricher> transformerMap;

    public AuditEntityStreamChangeEventEnricher get(String entityType) {
        if (MapUtils.isNotEmpty(transformerMap) && transformerMap.containsKey(entityType)) {
            return transformerMap.get(entityType);
        }
        throw new IllegalArgumentException(String.format("No Audit Entity Change Event Enricher available for entity " +
                "type [%s] Please install the required entity enricher in the factory.", entityType));
    }
}
