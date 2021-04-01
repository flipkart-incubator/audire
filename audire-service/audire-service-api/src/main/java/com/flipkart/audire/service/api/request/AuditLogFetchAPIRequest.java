package com.flipkart.audire.service.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.audire.stream.model.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AuditLogFetchAPIRequest {

    @NotNull
    @JsonProperty("entityType")
    private EntityType entityType;

    @JsonProperty("ownerId")
    private Set<String> ownerId;

    @JsonProperty("actor")
    private Set<String> actor;

    @JsonProperty("entityId")
    private Set<String> entityId;

    @JsonProperty("eventTraceId")
    private String eventTraceId;

    @JsonProperty("changedFields")
    private List<Field> changedFields;

    @Valid
    @JsonProperty("metaFilters")
    private List<MetaFilter> metaFilters;

    @Builder.Default
    @JsonProperty("includeSystemic")
    private final boolean includeSystemic = false;

    @Builder.Default
    @JsonProperty("onlySystemic")
    private final boolean onlySystemic = false;

    @Builder.Default
    @JsonProperty("includeFirst")
    private final boolean includeFirst = true;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    @ToString
    public static class Field {

        @JsonProperty("field")
        private String field;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class MetaFilter {

        @NotEmpty
        @JsonProperty("key")
        private String key;

        @NotEmpty
        @JsonProperty("val")
        private String val;
    }
}
