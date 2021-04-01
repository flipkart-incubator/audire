package com.flipkart.audire.stream.commons;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JsonNodeUtils {

    public final char DELIMITER = '.';
    private final ObjectMapper MAPPER = new ObjectMapper();

    public JsonNode unpackNestedKeys(JsonNode node, List<String> nestedFields) throws JsonParseException {
        for (String key : CollectionUtils.emptyIfNull(nestedFields)) {
            unpackJsonStringOrNestedObject(node, key);
        }
        return node.deepCopy();
    }

    /**
     * Given a valid key that points to a node within a json node, this method flattens it.
     * If the value of the key is a json string (text node), then it is read into a tree and flattened
     * according to the delimiter. The json string (if any) must be valid.
     * <p>
     * On the other hand, if the value of the key node is not a text, then it is assumed to be a
     * object node initially. If the value is an array node, then the fields are marked by its index.
     * <p>
     * Note: The original node is removed after flattening and the node is modified in place.
     */
    private void unpackJsonStringOrNestedObject(JsonNode node, String key) throws JsonParseException {
        try {
            ObjectNode objNode = (ObjectNode) node;
            JsonNode keyNode = node.get(key);

            if (keyNode != null) {
                JsonNode newNode = keyNode;
                if (keyNode instanceof TextNode) {
                    newNode = MAPPER.readTree(keyNode.asText());
                }
                Iterator<Map.Entry<String, JsonNode>> nodes = newNode.fields();
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> entry = nodes.next();
                    String newKey = key + DELIMITER + entry.getKey();
                    objNode.set(newKey, entry.getValue());
                }
                if (newNode instanceof ArrayNode) {
                    int index = 0;
                    for (JsonNode arrayNode : newNode) {
                        String newKey = key + DELIMITER + index;
                        objNode.set(newKey, arrayNode);
                        index += 1;
                    }
                }
                objNode.remove(key);
            }
        } catch (Exception ex) {
            throw new JsonParseException(null, String.format("Error unpacking key [%s] for node %s", key, node), ex);
        }
    }
}
