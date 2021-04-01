package com.flipkart.audire.stream.commons;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonNodeUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testUnpackNestedKeysThrowsExceptionWhenNestedFieldKeyCannotBeNested() {
        ObjectNode node = getTestObjectNode();
        List<String> fields = Lists.newArrayList("name", "name.type");

        JsonParseException exception = assertThrows(JsonParseException.class, () -> JsonNodeUtils.unpackNestedKeys(node, fields));
        assertTrue(exception.getMessage().contains("Error unpacking key [name.type]"));
    }

    @Test
    void testUnpackNestedKeysForAllNestedFieldsIncludingUnknownKeys() throws JsonParseException {
        ObjectNode node = getTestObjectNode();
        List<String> fields = Lists.newArrayList(
                "name",
                "name.data",
                "random"
        );
        JsonNode jsonNode = JsonNodeUtils.unpackNestedKeys(node, fields);
        assertAll(
                () -> assertEquals(5, jsonNode.size()),
                () -> assertEquals(12, jsonNode.get("id").asInt()),
                () -> assertEquals("T1", jsonNode.get("name.type").asText()),
                () -> assertEquals("New", jsonNode.get("name.data.text").asText()),
                () -> assertTrue(jsonNode.get("name.data.valid").asBoolean())
        );
    }

    @Test
    void testUnpackNestedKeysWhenOneOfTheNodeHasAnEscapedJsonObjectValue() throws JsonParseException {
        ObjectNode node = MAPPER.createObjectNode();
        node.set("log", new TextNode(
                "{\"key\": \"Test\", \"val\": 123.0}"
        ));
        List<String> fields = Collections.singletonList("log");
        JsonNode jsonNode = JsonNodeUtils.unpackNestedKeys(node, fields);

        assertAll(
                () -> assertEquals(2, jsonNode.size()),
                () -> assertEquals("Test", jsonNode.get("log.key").asText()),
                () -> assertEquals(123, jsonNode.get("log.val").asDouble())
        );
    }

    @Test
    void testUnpackNestedKeysWhenOneOfTheNodeHasAnArrayValue() throws JsonParseException {
        ObjectNode node = MAPPER.createObjectNode();
        ArrayNode array = MAPPER.createArrayNode();
        array.add(new TextNode("1"));
        array.add(new TextNode("2"));
        node.set("log", array);
        List<String> fields = Collections.singletonList("log");
        JsonNode jsonNode = JsonNodeUtils.unpackNestedKeys(node, fields);

        assertAll(
                () -> assertEquals(2, jsonNode.size()),
                () -> assertEquals("1", jsonNode.get("log.0").asText()),
                () -> assertEquals("2", jsonNode.get("log.1").asText())
        );
    }

    @Test
    void testUnpackNestedKeysWhenOneOfTheNodeHasAnEscapedArrayValue() throws JsonProcessingException {
        ObjectNode node = MAPPER.createObjectNode();
        ArrayNode array = MAPPER.createArrayNode();
        array.add(new TextNode("1"));
        array.add(new TextNode("2"));
        array.add(new TextNode("3"));
        node.set("log", new TextNode(MAPPER.writeValueAsString(array)));
        List<String> fields = Collections.singletonList("log");
        JsonNode jsonNode = JsonNodeUtils.unpackNestedKeys(node, fields);

        assertAll(
                () -> assertEquals(3, jsonNode.size()),
                () -> assertEquals("1", jsonNode.get("log.0").asText()),
                () -> assertEquals("2", jsonNode.get("log.1").asText()),
                () -> assertEquals("3", jsonNode.get("log.2").asText())
        );
    }

    private ObjectNode getTestObjectNode() {
        ObjectNode node = MAPPER.createObjectNode();
        node.set("id", new IntNode(12));
        node.set("sid", new TextNode("TN"));
        node.set("name", MAPPER.createObjectNode()
                .put("type", "T1")
                .set("data", MAPPER.createObjectNode()
                        .put("text", "New")
                        .put("valid", true)
                )
        );
        return node;
    }
}
