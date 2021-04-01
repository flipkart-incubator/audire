package com.flipkart.audire.service.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.audire.stream.model.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class AuditLogFetchAPIResponse {

    @JsonProperty("total")
    private long total;

    @JsonProperty("pageCount")
    private long pageCount;

    @JsonProperty("hasNextPage")
    private boolean hasNextPage;

    @JsonProperty("audits")
    private List<Audit> audits;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    @Getter
    public static class Audit {

        @JsonProperty("entityId")
        private String entityId;

        @JsonProperty("entityType")
        private EntityType entityType;

        @JsonProperty("eventTraceId")
        private String eventTraceId;

        @JsonProperty("auditId")
        private String auditId;

        @JsonProperty("auditPrevId")
        private String auditPrevId;

        @JsonProperty("version")
        private long version;

        @JsonProperty("changes")
        private List<Changelog> changes;

        @JsonProperty("meta")
        private List<Meta> meta;

        @JsonProperty("changedAt")
        private String changedAt;

        @JsonProperty("actor")
        private String actor;

        @JsonProperty("ownerId")
        private String ownerId;

        @Builder.Default
        @JsonProperty("systemic")
        private boolean isSystemic = false;

        @Builder.Default
        @JsonProperty("first")
        private boolean isFirst = false;

        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        @ToString
        @Builder
        public static class Changelog {

            @JsonProperty("operation")
            private String operation;

            @JsonProperty("field")
            private String field;

            @JsonProperty("before")
            private Object before;

            @JsonProperty("after")
            private Object after;
        }

        @AllArgsConstructor
        @Getter
        @ToString
        @NoArgsConstructor
        @Builder
        public static class Meta {

            @JsonProperty("key")
            private Object key;

            @JsonProperty("val")
            private Object val;
        }
    }
}
