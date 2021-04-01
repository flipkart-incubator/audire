package com.flipkart.audire.stream.core.serdes.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.audire.stream.core.config.AuditStoreSinkConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.elasticsearch.action.index.IndexRequest;

@Singleton
public class IndexRequestKeySerializer implements Serializer<IndexRequest> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String documentKey;

    @Inject
    public IndexRequestKeySerializer(AuditStoreSinkConfiguration config) {
        this.documentKey = config.getDocumentKey();
    }

    @Override
    public byte[] serialize(String topic, IndexRequest request) {
        try {
            if (request != null) {
                ObjectNode node = MAPPER.createObjectNode();
                String documentId = request.id();
                node.set(documentKey, new TextNode(documentId));

                return MAPPER.writeValueAsBytes(node);
            }
            return new byte[0];

        } catch (Exception ex) {
            throw new SerializationException(String.format("Failed to serialize [%s]", request), ex);
        }
    }
}
