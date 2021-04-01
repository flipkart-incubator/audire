package com.flipkart.audire.stream.core.serdes.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.Map;

@Singleton
@Slf4j
public class IndexRequestDeserializer implements Deserializer<IndexRequest> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public IndexRequest deserialize(String topic, byte[] data) {
        try {
            Map map = MAPPER.readValue(data, Map.class);
            return new IndexRequest().source(map, XContentType.JSON);

        } catch (Exception ex) {
            log.error("Failed to deserialize {} in the topic [{}]", data, topic, ex);
            return null;
        }
    }
}
