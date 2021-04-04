package com.flipkart.audire.stream.core.processor;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@AllArgsConstructor
public class AuditEntityStreamChangeEventProcessorFactory {

    private final Map<String, AuditEntityStreamChangeEventProcessor> transformerMap;

    public AuditEntityStreamChangeEventProcessor get(String entityType) {
        if (MapUtils.isNotEmpty(transformerMap) && transformerMap.containsKey(entityType)) {
            return transformerMap.get(entityType);
        }
        throw new IllegalArgumentException(String.format("No Audit Entity Change Event Processor available for entity " +
                "type [%s] Please install the required entity processor in the factory.", entityType));
    }
}
