package com.flipkart.audire.service.app;

import com.flipkart.audire.service.app.config.AudireServiceAppConfiguration;
import com.flipkart.audire.service.app.exception.mapper.AuditLogFetchExceptionMapper;
import com.flipkart.audire.service.app.exception.mapper.DefaultExceptionMapper;
import com.flipkart.audire.service.app.exception.mapper.WebApplicationExceptionMapper;
import com.flipkart.audire.service.app.module.AudireServiceModule;
import com.flipkart.audire.service.app.resource.AudireServiceResource;
import com.flipkart.audire.service.client.ManagedElasticSearchClient;
import com.google.inject.Stage;
import io.dropwizard.Application;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import static com.flipkart.audire.service.app.constants.AudireServiceAppConstants.APPLICATION_NAME;
import static com.flipkart.audire.service.app.constants.AudireServiceAppConstants.FLIPKART_DSP_LIBRARIES;

public class AudireServiceApplication extends Application<AudireServiceAppConfiguration> {

    @Override
    public String getName() {
        return APPLICATION_NAME;
    }

    @Override
    public void initialize(Bootstrap<AudireServiceAppConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .modules(new AudireServiceModule())
                .enableAutoConfig(getClass().getPackage().getName(), ManagedElasticSearchClient.class.getPackage().getName(), FLIPKART_DSP_LIBRARIES)
                .build(Stage.PRODUCTION));
    }

    @Override
    public void run(AudireServiceAppConfiguration config, Environment environment) {
        registerResource(environment);
        registerExceptionMapper(environment);
    }

    private void registerResource(Environment environment) {
        environment.jersey().register(AudireServiceResource.class);
    }

    private void registerExceptionMapper(Environment environment) {
        environment.jersey().register(new JsonProcessingExceptionMapper(true));
        environment.jersey().register(new WebApplicationExceptionMapper());
        environment.jersey().register(new DefaultExceptionMapper());
        environment.jersey().register(new AuditLogFetchExceptionMapper());
    }

    public static void main(String[] args) throws Exception {
        new AudireServiceApplication().run(args);
    }
}
