package com.flipkart.audire.stream.core.topology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.flipkart.audire.stream.core.config.AudireStreamAppConfiguration;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.testing.FixtureHelpers;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.stream.Stream;

@UtilityClass
class TopologyTestArgumentSource {

    AudireStreamAppConfiguration readAuditStreamAppYaml() {
        try {
            ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
            YAML_MAPPER.setSubtypeResolver(new DiscoverableSubtypeResolver());
            JsonNode node = YAML_MAPPER.readTree(FixtureHelpers.fixture("config/audire_stream_app_config.yaml"));
            return YAML_MAPPER.readValue(new TreeTraversingParser(node), AudireStreamAppConfiguration.class);

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    Stream<Arguments> source() {
        return Stream.of(
                Arguments.of("env.database.dummy_entity_audit",
                        "input_events/dummy_entity_audit_event.json",
                        "Dummy Entity",
                        "com.flipkart.audire.stream.core.topology.dummy.DummyEntityChangeEventDeserializer")
        );
    }
}
