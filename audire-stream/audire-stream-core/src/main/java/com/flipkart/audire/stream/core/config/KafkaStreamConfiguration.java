package com.flipkart.audire.stream.core.config;

import io.dropwizard.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaStreamConfiguration {

    @NotEmpty
    private String egressTopic;

    @NotEmpty
    private String applicationId;

    @NotEmpty
    private String bootstrapServers;

    @Min(100)
    private int cacheMaxBytesBuffering;

    @NotNull
    private Duration commitIntervalConfig;

    @NotNull
    private Duration metadataMaxAgeConfig;

    @NotEmpty
    private String autoOffsetResetConfig;
}
