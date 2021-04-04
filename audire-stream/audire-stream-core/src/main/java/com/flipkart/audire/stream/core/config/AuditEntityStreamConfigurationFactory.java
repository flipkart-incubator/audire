package com.flipkart.audire.stream.core.config;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@AllArgsConstructor
public class AuditEntityStreamConfigurationFactory {

    private final Map<String, AuditEntityStreamConfiguration> transformerMap;

    public AuditEntityStreamConfiguration get(String entityType) {
        if (MapUtils.isNotEmpty(transformerMap) && transformerMap.containsKey(entityType)) {
            return transformerMap.get(entityType);
        }
        throw new IllegalArgumentException(String.format("No Audit Entity Stream Configuration available for entity " +
                "type [%s] Please install the required entity configuration in the factory.", entityType));
    }
}
