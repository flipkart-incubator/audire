package com.flipkart.audire.service.app.config;

import com.flipkart.audire.service.client.config.ESClientConfiguration;
import io.dropwizard.Configuration;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
public class AudireServiceAppConfiguration extends Configuration {

    @NotNull
    @Valid
    private ESClientConfiguration esClientConfig;
}
