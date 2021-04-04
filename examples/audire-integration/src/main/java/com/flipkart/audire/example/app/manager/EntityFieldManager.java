package com.flipkart.audire.example.app.manager;

import com.flipkart.audire.example.app.api.EntityFieldResponse;
import com.flipkart.audire.example.app.manager.exception.AuditEntityFieldsFetchFailedException;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration.FieldConfiguration.Field.FieldType.FIELD_TYPES_ALL;

@Singleton
public class EntityFieldManager {

    private final AuditEntityStreamConfigurationFactory configFactory;

    @Inject
    public EntityFieldManager(AuditEntityStreamConfigurationFactory configFactory) {
        this.configFactory = configFactory;
    }

    public EntityFieldResponse getEntityFields(Set<String> entityTypes, boolean includeSystemic) {
        try {
            Map<String, EntityFieldResponse.Type> entityFieldsMap = entityTypes.stream()
                    .map(configFactory::get)
                    .map(AuditEntityStreamConfiguration::getFieldConfig)
                    .collect(Collectors.toMap(AuditEntityStreamConfiguration.FieldConfiguration::getEntityType,
                            config -> getEntityFieldType(includeSystemic, config)));

            return EntityFieldResponse.builder()
                    .entityFields(entityFieldsMap)
                    .fieldTypes(FIELD_TYPES_ALL)
                    .build();

        } catch (Exception ex) {
            throw new AuditEntityFieldsFetchFailedException(ex.getMessage(), ex);
        }
    }

    private EntityFieldResponse.Type getEntityFieldType(boolean includeSystemic, AuditEntityStreamConfiguration.FieldConfiguration fieldConfig) {
        Set<EntityFieldResponse.Type.Field> changedFields = buildFieldTypeResponse(fieldConfig.getWhitelistedFields(), includeSystemic);
        Set<EntityFieldResponse.Type.Field> metaFields = buildFieldTypeResponse(fieldConfig.getExtrasFields(), includeSystemic);

        return EntityFieldResponse.Type.builder()
                .changedFields(changedFields)
                .metaFields(metaFields)
                .build();
    }

    private Set<EntityFieldResponse.Type.Field> buildFieldTypeResponse(Set<AuditEntityStreamConfiguration.FieldConfiguration.Field> fieldSet, boolean includeSystemic) {
        return fieldSet.stream()
                .filter(field -> includeSystemic || !field.isSystemic())
                .map(this::buildField)
                .collect(Collectors.toSet());
    }

    private EntityFieldResponse.Type.Field buildField(AuditEntityStreamConfiguration.FieldConfiguration.Field field) {
        return EntityFieldResponse.Type.Field.builder()
                .id(field.getId())
                .displayName(field.getDisplayName())
                .type(field.getType().name())
                .list(field.isList())
                .build();
    }
}
