package com.flipkart.audire.stream.core.processor;

import com.flipkart.audire.stream.model.EntityType;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@AllArgsConstructor
public class AuditEntityStreamChangeEventProcessorFactory {

    private final Map<EntityType, AuditEntityStreamChangeEventProcessor> transformerMap;

    public AuditEntityStreamChangeEventProcessor get(EntityType entityType) {
        if (MapUtils.isNotEmpty(transformerMap) && transformerMap.containsKey(entityType)) {
            return transformerMap.get(entityType);
        }
        throw new IllegalArgumentException(String.format("No Audit Entity Change Event Processor available for entity " +
                "type [%s] Please install the required entity processor in the factory.", entityType));
    }
}
