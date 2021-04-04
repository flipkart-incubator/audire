package com.flipkart.audire.example.app.config;

import com.flipkart.audire.stream.core.config.AudireStreamAppConfiguration;
import io.dropwizard.Configuration;
import lombok.Getter;

@Getter
public class AudireIntegrationAppConfiguration extends Configuration {

    private AudireStreamAppConfiguration streamAppConfig;
}
