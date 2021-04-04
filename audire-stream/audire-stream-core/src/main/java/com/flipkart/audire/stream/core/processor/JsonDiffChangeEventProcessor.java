package com.flipkart.audire.stream.core.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.flipkart.audire.stream.model.ChangelogRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.DELIMITER_PATH;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.KEY_FROM_VALUE;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.KEY_PATH;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.KEY_VALUE;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.OP;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.Op.ADD;
import static com.flipkart.audire.stream.core.processor.JsonNodeDiffProvider.Op.REMOVE;

@Slf4j
public class JsonDiffChangeEventProcessor implements AuditEntityStreamChangeEventProcessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AuditEntityStreamConfiguration config;

    JsonDiffChangeEventProcessor(AuditEntityStreamConfiguration config) {
        this.config = config;
    }

    @Override
    public ChangelogRecord process(AuditStreamEntityChangeEvent changeEvent) {
        try {
            if (changeEvent != null) {
                log.info("Receiving enriched change event {}", changeEvent);
                String entityType = config.getFieldConfig().getEntityType();
                JsonNode beforeNode = changeEvent.getBeforeNode();
                JsonNode afterNode = changeEvent.getAfterNode();

                if (beforeNode == null) {
                    log.info("No previous {} audit found. This could be the first node and will be indexed as such", entityType);
                    List<ChangelogRecord.Changelog> records = processNewRecord(changeEvent);

                    if (CollectionUtils.isNotEmpty(records)) {
                        return buildChangelogRecord(changeEvent, records);
                    }
                }
                JsonNode payloadDiffNodes = JsonNodeDiffProvider.diff(beforeNode, afterNode);
                if (null == payloadDiffNodes) {
                    log.info("No diff in the {} audit change event", entityType);
                    return null;
                }
                log.info("Found diff for {} audit with entity id {}:  {}", entityType, changeEvent.getEntityId(), payloadDiffNodes);
                List<ChangelogRecord.Changelog> records = processExistingRecord(entityType, payloadDiffNodes);

                if (CollectionUtils.isNotEmpty(records)) {
                    return buildChangelogRecord(changeEvent, records);
                } else {
                    log.info("Found no tangible diff in the {} change event. Returning a null change log", entityType);
                    return null;
                }
            }
            log.info("Null change event received. No processing required");
            return null;

        } catch (Exception ex) {
            log.error("Error processing {} change event", changeEvent, ex);
            throw new AuditEntityStreamChangeEventProcessorException("Error while processing change event. ", ex);
        }
    }

    private List<ChangelogRecord.Changelog> processExistingRecord(String entityType, JsonNode payloadDiffNodes) {
        Map<String, ArrayNode> groupedAddFieldNodes = new HashMap<>();
        Map<String, ArrayNode> groupedRemoveFieldNodes = new HashMap<>();
        List<ChangelogRecord.Changelog> records = new ArrayList<>();

        for (JsonNode diffNode : payloadDiffNodes) {
            JsonNodeDiffProvider.Op op = JsonNodeDiffProvider.Op.of(diffNode.get(OP).textValue());
            JsonNode oldVal = diffNode.get(KEY_FROM_VALUE);
            JsonNode newVal = diffNode.get(KEY_VALUE);

            Set<String> whitelistedFieldIds = extractFieldIds(config.getFieldConfig().getWhitelistedFields());
            String field = diffNode.get(KEY_PATH).textValue().split(DELIMITER_PATH)[1];

            if (!whitelistedFieldIds.contains(field)) {
                log.info("Skipping field {} as it is not a part of the whitelisted {} set {}", field, entityType, config.getFieldConfig().getWhitelistedFields());
                continue;
            }
            log.info("Including field {} as it is not a part of the whitelisted {} set {}", field, entityType, config.getFieldConfig().getWhitelistedFields());
            if (CollectionUtils.emptyIfNull(config.getFieldConfig().getImmutableFields()).contains(field)) {
                groupedAddFieldNodes.putIfAbsent(field, MAPPER.createArrayNode());
                groupedRemoveFieldNodes.putIfAbsent(field, MAPPER.createArrayNode());

                switch (op) {
                    case REPLACE:
                        groupedAddFieldNodes.get(field).addPOJO(newVal);
                        groupedRemoveFieldNodes.get(field).addPOJO(oldVal);
                        break;
                    case ADD:
                        groupedAddFieldNodes.get(field).addPOJO(newVal);
                        break;
                    case REMOVE:
                        groupedRemoveFieldNodes.get(field).addPOJO(newVal);
                        break;
                    default:
                        log.info("Incompatible Op received : [{}]. Only REPLACE, ADD and REMOVE diff flags are supported.", op);
                }
            } else {
                records.add(getChangeLog(op, field, oldVal, newVal));
            }
        }
        records.addAll(getChangelogListByOp(groupedAddFieldNodes, ADD));
        records.addAll(getChangelogListByOp(groupedRemoveFieldNodes, JsonNodeDiffProvider.Op.REMOVE));
        return records;
    }

    private List<ChangelogRecord.Changelog> getChangelogListByOp(Map<String, ArrayNode> fieldNodes, JsonNodeDiffProvider.Op op) {
        return fieldNodes.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(group -> getChangeLog(op, group.getKey(),
                        op == ADD ? null : group.getValue(),
                        op == REMOVE ? null : group.getValue()))
                .collect(Collectors.toList());
    }

    private List<ChangelogRecord.Changelog> processNewRecord(AuditStreamEntityChangeEvent changeEvent) {
        return config.getFieldConfig().getWhitelistedFields()
                .stream()
                .map(AuditEntityStreamConfiguration.FieldConfiguration.Field::getId)
                .filter(id -> changeEvent.getAfterNode().get(id) != null)
                .map(id -> getChangeLog(ADD, id, null, changeEvent.getAfterNode().get(id)))
                .collect(Collectors.toList());
    }

    private ChangelogRecord buildChangelogRecord(AuditStreamEntityChangeEvent changeEvent, List<ChangelogRecord.Changelog> changes) {
        return ChangelogRecord.builder()
                ._auditId(changeEvent.getId())
                ._auditPrevId(changeEvent.getPrevId())
                ._eventTraceId(changeEvent.getEventTraceId())
                ._eventType(changeEvent.getEventType().getValue())
                .entityType(config.getFieldConfig().getEntityType())
                .entityId(changeEvent.getEntityId())
                .changedAt(changeEvent.getTimestamp())
                .actor(changeEvent.getActor())
                .version(changeEvent.getVersion())
                .ownerId(changeEvent.getOwnerId())
                .systemic(isChangeEventSystemic(changeEvent.getActor()))
                .changes(changes)
                .extras(getExtras(changeEvent))
                .build();
    }

    private List<ChangelogRecord.Extra> getExtras(AuditStreamEntityChangeEvent changeEvent) {
        Set<String> extrasField = extractFieldIds(config.getFieldConfig().getExtrasFields());

        JsonNode afterNode = changeEvent.getAfterNode();
        return CollectionUtils.emptyIfNull(extrasField).stream()
                .filter(afterNode::has)
                .map(extraField -> new ChangelogRecord.Extra(extraField, afterNode.get(extraField)))
                .collect(Collectors.toList());
    }

    private Set<String> extractFieldIds(Set<AuditEntityStreamConfiguration.FieldConfiguration.Field> fieldList) {
        return fieldList.stream()
                .map(AuditEntityStreamConfiguration.FieldConfiguration.Field::getId)
                .collect(Collectors.toSet());
    }

    private boolean isChangeEventSystemic(String actor) {
        return SetUtils.emptyIfNull(config.getSystemicActors()).contains(actor.toLowerCase());
    }

    private ChangelogRecord.Changelog getChangeLog(JsonNodeDiffProvider.Op operation, String changedField, JsonNode before, JsonNode after) {
        return ChangelogRecord.Changelog.builder()
                ._changedField(changedField)
                ._operation(operation.getText())
                ._before(before)
                ._after(after)
                .build();
    }
}
