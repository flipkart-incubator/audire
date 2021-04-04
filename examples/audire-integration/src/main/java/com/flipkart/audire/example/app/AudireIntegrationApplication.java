package com.flipkart.audire.example.app;

import com.flipkart.audire.example.app.config.AudireIntegrationAppConfiguration;
import com.flipkart.audire.example.app.module.AudireIntegrationAppModule;
import com.flipkart.audire.example.app.resource.AudireIntegrationResource;
import com.google.inject.Stage;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import static com.flipkart.audire.example.app.constants.AudireStreamAppConstants.APPLICATION_NAME;

public class AudireIntegrationApplication extends Application<AudireIntegrationAppConfiguration> {

    @Override
    public String getName() {
        return APPLICATION_NAME;
    }

    @Override
    public void initialize(Bootstrap<AudireIntegrationAppConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .modules(new AudireIntegrationAppModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .build(Stage.PRODUCTION));
    }

    @Override
    public void run(AudireIntegrationAppConfiguration configuration, Environment environment) {
        registerResource(environment);
    }

    private void registerResource(Environment environment) {
        environment.jersey().register(AudireIntegrationResource.class);
    }


    public static void main(String[] args) throws Exception {
        new AudireIntegrationApplication().run(args);
    }
}
