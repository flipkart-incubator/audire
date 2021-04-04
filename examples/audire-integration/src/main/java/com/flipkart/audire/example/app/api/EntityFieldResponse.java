package com.flipkart.audire.example.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EntityFieldResponse {

    @JsonProperty("entityFields")
    private Map<String, Type> entityFields;

    @JsonProperty("fieldTypes")
    private Set<String> fieldTypes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Type {

        @JsonProperty("changedFields")
        private Set<Field> changedFields;

        @JsonProperty("metaFields")
        private Set<Field> metaFields;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @ToString
        @EqualsAndHashCode()
        public static class Field {

            @JsonProperty("id")
            private String id;

            @JsonProperty("displayName")
            private String displayName;

            @JsonProperty("type")
            private String type;

            @JsonProperty("list")
            private boolean list;
        }
    }
}
