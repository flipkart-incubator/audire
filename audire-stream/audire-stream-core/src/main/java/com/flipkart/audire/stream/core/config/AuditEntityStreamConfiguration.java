package com.flipkart.audire.stream.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuditEntityStreamConfiguration {

    @Valid
    @NotNull
    private FieldConfiguration fieldConfig;

    @NotBlank
    private String ingressTopic;

    /**
     * Skips the processing of audits for this particular entity.
     */
    @Builder.Default
    private boolean skip = false;

    /**
     * Provide a set of actors which are identified to be systemic.
     * A change event, if associated with a systemic actor is marked as such. This can be
     * helpful to group systemic audits vs user facing audits.
     */
    @Size(min = 1)
    private Set<String> systemicActors;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    @ToString
    public static class FieldConfiguration {

        /**
         * A unique entity type of an audit entity. Varying entity types haves different schema
         * and enrichment flows. However, in general, an audit entity is expected to abide by
         * the contracts.
         */
        @NotNull
        private String entityType;

        /**
         * Provide a unique id that represents this audit entry uniquely. This could be a primary
         * key of the audit entity, for example. Note that this cannot be null
         */
        @NotBlank
        private String auditIdField;

        /**
         * The field where the payload needs to be deconstructed from. Essentially, the data corresponding
         * to the payload field is where the "after" snapshot of this entity exists. The difference
         * in the payload between two such entries constitutes the diff for this entity.
         * <p>
         * If the payload field is null, this hints that the audit itself is the payload. This can
         * happen in some systems which keep a de-normalized audit structure.
         */
        private String payloadField;

        /**
         * Every audit entity must correspond to a valid entity with a unique representation. There may be
         * multiple audits for an entity, however, the entity ID should be unique.
         */
        @NotBlank
        private String entityIdField;

        /**
         * Field that specifies when the audit action was performed. Note that this is mandatory as an
         * audit system needs to know when the event has exactly happened.
         */
        @NotBlank
        private String timestampField;

        /**
         * Provide a set of field ids that contain a string representation of a valid JSON which needs to
         * be indexed as feature attributes. A lot of times the data is stored in a json format. Fields
         * specified here will be flattened so that the difference can be easily ascertained.
         * <p>
         * Note: Such fields needs to contain a valid json for its lifetime. Use cases include meta columns.
         * Note: Order of field matters.
         */
        @Size(min = 1)
        private List<String> nestedFields;

        /**
         * Provide a set of fields which are white listed from a change event point of view.
         * A lot of times we're only interested in the subset of changes for a particular event.
         * <p>
         * Note: If listening on nested fields, include the flattened fields in the whitelisted set.
         */
        @NotEmpty
        private Set<Field> whitelistedFields;

        /**
         * Provide a set of field ids for which there will be separate remove and add change events respectively
         * for an "edit" change event. Effectively, fields that are immutable have no "replace" and
         * hence cannot be changed even though the diff assumes it to be.
         * <p>
         * If an audit entity field comprises of a list of values and in a newer audit entry, the last
         * element is replaced with a new one, then it will generate an event of type edit by default.
         * Making the field immutable will generate two events, where in one, the last element is deleted
         * and one where a newer element is inserted.
         */
        @Size(min = 1)
        private Set<String> immutableFields;

        /**
         * Fields that are meant to be stored along side each audit record. Typically, this corresponds
         * to attributes that represent some unique characteristic of an entity besides the usual identifiers.
         * Usually, such fields are immutable. Their values may not be analyzed.
         */
        @Size(min = 1)
        private Set<Field> extrasFields;

        /**
         * The field that specifies the creator of the change event. Note that this is mandatory as an
         * audit system needs to know who triggered a particular event.
         */
        @NotBlank
        private String actorField;

        /**
         * The field that specifies the owner of the entity. Note that this is mandatory as an
         * audit system needs to know who owns this particular entity. This can be used to power
         * queries on diff index by owner.
         */
        @NotBlank
        private String ownerField;

        /**
         * The field that carries the event trace for this particular audit. A trace is an identifier
         * that is used to track updates to several entities in response to an orchestrator request.
         * Traces are extremely useful to group related entity changes by timestamp.
         * <p>
         * Note, this can be null for entities that are always updated standalone.
         */
        private String eventTraceField;

        @Getter
        @NoArgsConstructor
        @Builder
        @ToString
        @AllArgsConstructor
        public static class Field {

            /**
             * Non blank identifier for the field. This has to be unique per entity
             */
            @NotBlank
            private String id;

            /**
             * Human readable description of the field id
             */
            @NotBlank
            private String displayName;

            /**
             * Default type of the field. Field type is used to handle varied formatting use cases
             * post enrichment of audits
             */
            @Builder.Default
            private final FieldType type = FieldType.STRING;

            @Builder.Default
            private final boolean list = false;

            @Builder.Default
            private final boolean systemic = false;

            public enum FieldType {
                STRING,
                NUMERIC,
                BOOLEAN,
                EPOCH_MILLIS,
                DATE;

                public static Set<String> FIELD_TYPES_ALL = Stream.of(FieldType.values())
                        .map(FieldType::name)
                        .collect(Collectors.toSet());
            }
        }
    }
}
