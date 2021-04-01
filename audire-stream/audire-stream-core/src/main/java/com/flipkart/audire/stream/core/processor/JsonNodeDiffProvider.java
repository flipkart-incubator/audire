package com.flipkart.audire.stream.core.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class JsonNodeDiffProvider {

    static final String OP = "op";
    static final String KEY_PATH = "path";
    static final String KEY_VALUE = "value";
    static final String KEY_FROM_VALUE = "fromValue";
    static final String DELIMITER_PATH = "/";

    static JsonNode diff(JsonNode beforeNode, JsonNode afterNode) {
        JsonNode diffNode = JsonDiff.asJson(beforeNode, afterNode, EnumSet.of(
                DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE,
                DiffFlags.OMIT_MOVE_OPERATION
        ));
        JsonNode filteredDiff = filterIrrelevantDiffs(diffNode);
        if (!filteredDiff.elements().hasNext()) {
            return null;
        }
        return filteredDiff;
    }

    /**
     * This method helps filter irrelevant diff being returned in the json node. Diff library
     * does not have any context of data types. Hence, this utility method helps filter diffs
     * which are not correct in the case of an audit trail.
     */
    private static JsonNode filterIrrelevantDiffs(JsonNode diffNode) {
        Iterator<JsonNode> nodes = diffNode.elements();
        while (nodes.hasNext()) {
            JsonNode node = nodes.next();
            if (isValidPrimitiveDiffNode(node) && semanticallyEqualDoubleNodes(node)) {
                nodes.remove();
            }
        }
        return diffNode;
    }

    /**
     * A diff node is valid if it contains non null "before" and "after" values that are neither
     * objects nor arrays.
     */
    private boolean isValidPrimitiveDiffNode(JsonNode node) {
        return node.get(KEY_FROM_VALUE) != null && node.get(KEY_VALUE) != null
                && !node.get(KEY_FROM_VALUE).isObject() && !node.get(KEY_VALUE).isObject()
                && !node.get(KEY_FROM_VALUE).isArray() && !node.get(KEY_VALUE).isArray();
    }

    /**
     * Returns true where a Json diff node contain similar value of a double field but with a different
     * representation such as a double and an integer.
     * Note that this implementation does not cover all cases because it relies on the asText()
     * conversion. For chars, it is their ascii code (not the string value).
     * As a result, {@code semanticallyEqualDoubleNodes(115.0, 's') = true } while it should be false
     * <p>
     * This is not a problem as we know the data type would not magically change unless manually modified.
     * The trade off is maintainability and performance as every event needs to go through the filter.
     * <p>
     * {@code semanticallyEqualDoubleNodes(10.0, 10) = true }
     * {@code semanticallyEqualDoubleNodes(10, 10) = true }
     * {@code semanticallyEqualDoubleNodes(10, 10.01) = false }
     */
    private static boolean semanticallyEqualDoubleNodes(JsonNode node) {
        String textFrom = node.get(KEY_FROM_VALUE).asText();
        String textTo = node.get(KEY_VALUE).asText();

        if (NumberUtils.isCreatable(textFrom) && NumberUtils.isCreatable(textTo)) {
            Double doubleFrom = NumberUtils.toDouble(textFrom);
            Double doubleTo = NumberUtils.toDouble(textTo);

            if (doubleFrom.compareTo(doubleTo) == 0) {
                log.info("Inconsistency in text repr of before [{}] and after [{}]. Their double repr is same {}. " +
                        "Discarding this particular diff {}", textFrom, textTo, doubleFrom, node);
                return true;
            }
        }
        return false;
    }

    @AllArgsConstructor
    @Getter
    public enum Op {

        REPLACE("replace"),
        ADD("add"),
        REMOVE("remove"),
        MOVE("move"),
        COPY("copy");

        private final String text;

        public static Op of(String text) {
            return Stream.of(Op.values())
                    .filter(op -> op.text.equals(text))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown op " + text));
        }
    }
}
