package com.flipkart.audire.service.core.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.stream.model.ChangelogRecord;
import com.google.inject.Singleton;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AuditAPIResponseBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public AuditLogFetchAPIResponse.Audit buildAuditResponse(Map<String, Object> sourceMap) {
        AuditLogFetchAPIResponse.Audit.AuditBuilder builder = AuditLogFetchAPIResponse.Audit.builder();
        ChangelogRecord record = MAPPER.convertValue(sourceMap, ChangelogRecord.class);

        buildEntity(builder, record);
        buildOwner(builder, record);
        buildSystemic(builder, record);
        buildChanges(builder, record);
        return builder.build();
    }

    private void buildChanges(AuditLogFetchAPIResponse.Audit.AuditBuilder builder, ChangelogRecord record) {
        builder.changes(getChangelog(record.getChanges()));
        builder.meta(getMeta(record.getExtras()));
    }

    private void buildSystemic(AuditLogFetchAPIResponse.Audit.AuditBuilder builder, ChangelogRecord record) {
        builder.isSystemic(record.isSystemic());
        builder.isFirst(record.get_auditPrevId() == null);
        builder.changedAt(record.getChangedAt());
    }

    private void buildOwner(AuditLogFetchAPIResponse.Audit.AuditBuilder builder, ChangelogRecord record) {
        builder.actor(record.getActor());
        builder.ownerId(record.getOwnerId());
    }

    private void buildEntity(AuditLogFetchAPIResponse.Audit.AuditBuilder builder, ChangelogRecord record) {
        builder.entityId(record.getEntityId());
        builder.entityType(record.getEntityType());
        builder.version(record.getVersion());
        builder.auditId(record.get_auditId());
        builder.auditPrevId(record.get_auditPrevId());
        builder.eventTraceId(record.get_eventTraceId());
    }

    private List<AuditLogFetchAPIResponse.Audit.Changelog> getChangelog(List<ChangelogRecord.Changelog> changes) {
        return CollectionUtils.emptyIfNull(changes).stream()
                .map(change -> AuditLogFetchAPIResponse.Audit.Changelog.builder()
                        .field(change.get_changedField())
                        .operation(change.get_operation())
                        .before(change.get_before())
                        .after(change.get_after())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<AuditLogFetchAPIResponse.Audit.Meta> getMeta(List<ChangelogRecord.Extra> meta) {
        return CollectionUtils.emptyIfNull(meta).stream()
                .map(change -> AuditLogFetchAPIResponse.Audit.Meta.builder()
                        .key(change.getKey())
                        .val(change.getVal())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
