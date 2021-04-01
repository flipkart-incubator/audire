package com.flipkart.audire.service.client.config;

import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@ToString
public class ESClientConfiguration {

    @NotEmpty
    private String host;

    @Min(1)
    private int port;

    @NotEmpty
    private String scheme;

    @NotEmpty
    private String index;

    /**
     * Type is mandatory in older elastic versions. Once the cluster is migrated to a newer
     * version (>=7.8), the type should be defaulted to _doc_ or be removed altogether
     */
    @NotEmpty
    private String type;

    @NotNull
    private Duration queryTimeout;
}
