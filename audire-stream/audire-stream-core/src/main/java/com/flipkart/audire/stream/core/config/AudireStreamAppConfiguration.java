package com.flipkart.audire.stream.core.config;

import com.flipkart.audire.stream.model.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudireStreamAppConfiguration {

    @Valid
    @NotEmpty
    private Map<EntityType, AuditEntityStreamConfiguration> auditStreamEntityConfig;

    @Valid
    @NotNull
    private AuditStoreSinkConfiguration auditStoreSinkConfig;

    @Valid
    @NotNull
    private KafkaStreamConfiguration kafkaStreamConfig;
}
