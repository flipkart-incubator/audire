package com.flipkart.audire.stream.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class AuditStreamEntityChangeEvent {

    private EventType eventType;
    private String id;
    private String prevId;
    private String eventTraceId;
    private EntityType entityType;
    private String entityId;
    private JsonNode afterNode;
    private JsonNode beforeNode;
    private long version;
    private String timestamp;
    private String actor;
    private String ownerId;
}
