package com.flipkart.audire.stream.core.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.flipkart.audire.stream.model.EntityType;
import com.flipkart.audire.stream.model.EventType;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static com.flipkart.audire.stream.model.EntityType.BANNER_GROUP_AUDIT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEntityStreamChangeEventDeserializerTest {

    @Mock
    private AuditEntityStreamConfiguration.FieldConfiguration fieldConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AuditEntityStreamConfigurationFactory mockFactory;

    private AuditEntityStreamChangeEventDeserializer deserializer;

    @BeforeEach
    void setUp() {
        when(mockFactory.get(EntityType.AD_PRODUCT_AUDIT).getFieldConfig()).thenReturn(fieldConfig);
        this.deserializer = spy(new AuditEntityStreamChangeEventDeserializerImpl(mockFactory));
    }

    @Test
    void testDeserializeThrowsExceptionWhenBytesCannotBeReadToAMap() {
        assertThrows(AuditEntityChangeStreamEventDeserializationException.class, () -> deserializer.deserialize(null));
    }

    @Test
    void testDeserializeReturnsNullWhenAfterNodeIsNull() throws JsonProcessingException {
        String payload = "{" +
                "\"__op\": \"create\"," +
                "\"AuditId\": \"AID\"," +
                "\"EntityId\": \"EID\"," +
                "\"Timestamp\": \"Time\"," +
                "\"Actor\": \"A1\"," +
                "\"Owner\": \"O1\"," +
                "\"EventTrace\": \"Trace\"" +
                "}";
        JsonNode snapshotNode = new ObjectMapper().readTree(payload);
        doReturn(snapshotNode).when(deserializer).extractSnapshotFromPayload(any());
        doReturn(3L).when(deserializer).extractVersion(any());

        when(fieldConfig.getAuditIdField()).thenReturn("AuditId");
        when(fieldConfig.getEntityIdField()).thenReturn("EntityId");
        when(fieldConfig.getTimestampField()).thenReturn("Timestamp");
        when(fieldConfig.getOwnerField()).thenReturn("Owner");
        when(fieldConfig.getActorField()).thenReturn("Actor");
        when(fieldConfig.getEventTraceField()).thenReturn("EventTrace");
        when(fieldConfig.getEntityType()).thenReturn(BANNER_GROUP_AUDIT);

        AuditStreamEntityChangeEvent event = deserializer.deserialize(payload.getBytes());
        assertAll(
                () -> assertEquals("AID", event.getId()),
                () -> assertEquals(EventType.CREATE, event.getEventType()),
                () -> assertEquals(BANNER_GROUP_AUDIT, event.getEntityType()),
                () -> assertEquals("Trace", event.getEventTraceId()),
                () -> assertEquals("EID", event.getEntityId()),
                () -> assertEquals("O1", event.getOwnerId()),
                () -> assertEquals(3L, event.getVersion()),
                () -> assertEquals("Time", event.getTimestamp()),
                () -> assertEquals("A1", event.getActor()),
                () -> assertEquals(snapshotNode, event.getAfterNode())
        );
    }

    @Test
    void testDeserializeWhenAfterNodeIsNull() {
        String payload = "{\"a\": \"Test\"}";
        doReturn(null).when(deserializer).extractSnapshotFromPayload(any());
        assertNull(deserializer.deserialize(payload.getBytes()));
    }

    @Test
    void testExtractSnapshotFromPayloadReturnsNullWhenPayloadIsEmpty() {
        assertNull(deserializer.extractSnapshotFromPayload(null));
    }

    @Test
    void testExtractSnapshotReturnsTheWholePayloadFromWhenPayloadFieldIsNull() {
        Map<String, Object> payload = ImmutableMap.of("A", 10, "B", "C");
        JsonNode snapshotNode = deserializer.extractSnapshotFromPayload(payload);

        assertNotNull(snapshotNode);
        assertEquals(new IntNode(10), snapshotNode.get("A"));
        assertEquals(2, snapshotNode.size());
    }

    @Test
    void testExtractSnapshotFromWhenPayloadEntityFieldIsNull() {
        Map<String, Object> payload = Collections.singletonMap("A", 10);
        when(fieldConfig.getPayloadField()).thenReturn("PAYLOAD");
        assertNull(deserializer.extractSnapshotFromPayload(payload));
    }

    @Test
    void testExtractSnapshotFromWhenAuditLogIsNull() {
        Map<String, Object> payload = ImmutableMap.of("A", 10, "E", 21);
        when(fieldConfig.getPayloadField()).thenReturn("PAYLOAD");

        when(fieldConfig.getEntityIdField()).thenReturn("E");
        assertNull(deserializer.extractSnapshotFromPayload(payload));
    }

    @Test
    void testExtractSnapshotFromPayloadThrowsExceptionWhenPayloadCannotBeJsonParsed() {
        Map<String, Object> payload = ImmutableMap.of("A", 10, "E", 21,
                "PAYLOAD", "{Not a valid json[]");

        when(fieldConfig.getPayloadField()).thenReturn("PAYLOAD");
        when(fieldConfig.getEntityIdField()).thenReturn("E");
        assertNull(deserializer.extractSnapshotFromPayload(payload));
    }

    @Test
    void testExtractSnapshotFromPayload() {
        Map<String, Object> payload = ImmutableMap.of("A", 10, "E", 21,
                "PAYLOAD", "{\"a\": \"Test\"}");

        when(fieldConfig.getPayloadField()).thenReturn("PAYLOAD");
        when(fieldConfig.getEntityIdField()).thenReturn("E");
        when(fieldConfig.getNestedFields()).thenReturn(Collections.emptyList());

        JsonNode snapshotNode = deserializer.extractSnapshotFromPayload(payload);
        assertNotNull(snapshotNode);
        assertEquals(new TextNode("Test"), snapshotNode.get("a"));
        assertEquals(1, snapshotNode.size());
    }

    @Test
    void testExtractVersion() {
        when(fieldConfig.getTimestampField()).thenReturn("T");
        Map<String, Object> payload = Collections.singletonMap("T", "2022-03-08T08:14:29Z");
        assertEquals(1646727269 * 1e3, deserializer.extractVersion(payload));
    }

    private static class AuditEntityStreamChangeEventDeserializerImpl extends AuditEntityStreamChangeEventDeserializer {

        AuditEntityStreamChangeEventDeserializerImpl(AuditEntityStreamConfigurationFactory factory) {
            super(EntityType.AD_PRODUCT_AUDIT, factory);
        }
    }
}
