package com.flipkart.audire.stream.core.deserializer;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@AllArgsConstructor
public class AuditEntityStreamChangeEventDeserializerFactory {

    private final Map<String, AuditEntityStreamChangeEventDeserializer> transformerMap;

    public AuditEntityStreamChangeEventDeserializer get(String topicName) {
        if (MapUtils.isNotEmpty(transformerMap) && transformerMap.containsKey(topicName)) {
            return transformerMap.get(topicName);
        }
        throw new IllegalArgumentException(String.format("No Audit Entity Stream Change Event Deserializer configured for topic " +
                " [%s] Please install the required entity deserializer in the factory.", topicName));
    }
}
