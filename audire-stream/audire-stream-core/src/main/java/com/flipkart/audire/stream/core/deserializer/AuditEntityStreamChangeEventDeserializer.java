package com.flipkart.audire.stream.core.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.audire.stream.commons.DateUtils;
import com.flipkart.audire.stream.commons.JsonNodeUtils;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.flipkart.audire.stream.model.EventType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Getter
@Slf4j
@SuppressWarnings("unchecked")
public abstract class AuditEntityStreamChangeEventDeserializer {

    private static final String CHANGE_OP_CODE = "__op";
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuditEntityStreamConfiguration.FieldConfiguration fieldConfig;

    protected AuditEntityStreamChangeEventDeserializer(String entityType, AuditEntityStreamConfigurationFactory factory) {
        this.fieldConfig = factory.get(entityType).getFieldConfig();
    }

    /**
     * Provide a way to generate an audit entity change event from a byte stream
     */
    AuditStreamEntityChangeEvent deserialize(byte[] bytes) {
        try {
            Map<String, Object> payload = MAPPER.readValue(new String(bytes), Map.class);
            JsonNode snapshotNode = extractSnapshotFromPayload(payload);

            if (snapshotNode != null) {
                EventType eventType = EventType.fromOpCode(payload.get(CHANGE_OP_CODE).toString().charAt(0));
                String id = payload.get(fieldConfig.getAuditIdField()).toString();
                Object entityId = payload.get(fieldConfig.getEntityIdField());
                String timestampField = fieldConfig.getTimestampField();

                long version = extractVersion(payload);
                String timestamp = extractTimestampField(payload.get(timestampField));
                String ownerId = snapshotNode.get(fieldConfig.getOwnerField()).asText();
                String actor = extractActor(payload, fieldConfig.getActorField());
                String eventTraceId = getEventTraceId(payload);

                return AuditStreamEntityChangeEvent.builder()
                        .id(id)
                        .eventType(eventType)
                        .entityType(fieldConfig.getEntityType())
                        .eventTraceId(eventTraceId)
                        .entityId(entityId.toString())
                        .ownerId(ownerId)
                        .version(version)
                        .timestamp(timestamp)
                        .actor(actor)
                        .afterNode(snapshotNode)
                        .build();
            }

        } catch (Exception ex) {
            log.error("Received error while trying to deserialize {}", fieldConfig.getEntityType(), ex);
            throw new AuditEntityChangeStreamEventDeserializationException(ex);
        }
        return null;
    }

    /**
     * Provide a way to obtain an entity snapshot from an object payload.
     * This can be customized based on the use case
     */
    public JsonNode extractSnapshotFromPayload(Map<String, Object> payload) {
        try {
            JsonNode snapshotNode = null;
            if (MapUtils.isNotEmpty(payload)) {
                if (StringUtils.isBlank(fieldConfig.getPayloadField())) {
                    snapshotNode = MAPPER.convertValue(payload, JsonNode.class);

                } else {
                    Object entityId = payload.get(fieldConfig.getEntityIdField());
                    if (entityId == null || StringUtils.isBlank(entityId.toString())) {
                        log.info("Empty id passed for {}. Ignoring event.", fieldConfig.getEntityType());
                        return null;
                    }
                    Object logPayload = payload.get(fieldConfig.getPayloadField());
                    if (logPayload == null || StringUtils.isBlank(logPayload.toString())) {
                        log.info("Empty audit log received for {}. Ignoring event.", fieldConfig.getEntityType());
                        return null;
                    }
                    snapshotNode = MAPPER.readTree(logPayload.toString());
                }
            }
            if (snapshotNode != null) {
                snapshotNode = JsonNodeUtils.unpackNestedKeys(snapshotNode, fieldConfig.getNestedFields());
                snapshotNode = configureLogNode(snapshotNode);
            }
            return snapshotNode;

        } catch (JsonProcessingException ex) {
            log.error("Error extracting {} node snapshot for payload {}", fieldConfig.getEntityType(), payload, ex);
            return null;
        }
    }

    /**
     * Provide a way to configure the json node that represents the log of an audit.
     * Here, the node can be modified before it is processed further.
     */
    protected JsonNode configureLogNode(JsonNode logNode) {
        return logNode;
    }

    /**
     * Provide a way to extract human readable timestamp from the payload timestamp.
     * Default implementation assumes a timestamp field which can be converted to a
     * zoned date time instance.
     */
    protected String extractTimestampField(Object timestamp) {
        return timestamp.toString();
    }

    /**
     * Provide a way to extract the actor for the audit. The default implementation
     * expects a non null value attribute for the key specified by the actorField
     */
    protected String extractActor(Map<String, Object> payload, String actorField) {
        return payload.get(actorField).toString();
    }

    /**
     * Provide a way to extract a unique entity version. Default implementation is to
     * extract the version from the timestamp field. In versioned entities, this
     * should correspond to the actual entity version
     */
    protected long extractVersion(Map<String, Object> payload) {
        String timestamp = (String) (payload.get(fieldConfig.getTimestampField()));
        return DateUtils.getUTCInstantFromUTCTimestamp(timestamp).toEpochMilli();
    }

    private String getEventTraceId(Map<String, Object> payload) {
        String field = fieldConfig.getEventTraceField();
        if (StringUtils.isNotBlank(field) && null != payload.get(field) && StringUtils.isNotBlank(payload.get(field).toString())) {
            return payload.get(field).toString();
        }
        return null;
    }
}
