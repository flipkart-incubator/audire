package com.flipkart.audire.stream.model;

import com.fasterxml.jackson.databind.JsonNode;
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
public class ChangelogRecord {

    private String _auditId;
    private String _auditPrevId;
    private String _eventTraceId;
    private char _eventType;
    private String entityId;
    private String entityType;
    private long version;
    private List<Changelog> changes;
    private List<Extra> extras;
    private String changedAt;
    private String actor;
    private String ownerId;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @ToString
    public static class Changelog {

        private String _operation;
        private String _changedField;
        private JsonNode _before;
        private JsonNode _after;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @NoArgsConstructor
    public static class Extra {

        private String key;
        private JsonNode val;
    }

    @Builder.Default
    private boolean systemic = false;
}
