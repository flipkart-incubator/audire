package com.flipkart.audire.stream.core.topology;

import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.audire.stream.core.config.AudireStreamAppConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfigurationFactory;
import com.flipkart.audire.stream.core.config.AuditStoreSinkConfiguration;
import com.flipkart.audire.stream.core.config.KafkaStreamConfiguration;
import com.flipkart.audire.stream.core.deserializer.AuditEntityStreamChangeEventDeserializer;
import com.flipkart.audire.stream.core.deserializer.AuditEntityStreamChangeEventDeserializerFactory;
import com.flipkart.audire.stream.core.deserializer.BaseAuditEntityChangeEventDeserializer;
import com.flipkart.audire.stream.core.enricher.AuditEntityStreamChangeEventEnricher;
import com.flipkart.audire.stream.core.enricher.AuditEntityStreamChangeEventEnricherFactory;
import com.flipkart.audire.stream.core.indexer.ElasticSearchChangelogIndexRequestBuilder;
import com.flipkart.audire.stream.core.processor.AuditEntityStreamChangeEventProcessor;
import com.flipkart.audire.stream.core.processor.AuditEntityStreamChangeEventProcessorFactory;
import com.flipkart.audire.stream.core.serdes.deserializer.IndexRequestDeserializer;
import com.flipkart.audire.stream.core.serdes.serializer.IndexRequestKeySerializer;
import com.flipkart.audire.stream.core.serdes.serializer.IndexRequestValueSerializer;
import com.flipkart.audire.stream.core.serdes.serializer.ObjectToJsonBytesSerializer;
import com.flipkart.audire.stream.model.ChangelogRecord;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.util.Duration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.test.TestRecord;
import org.elasticsearch.action.index.IndexRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.DefaultArgumentsAccessor;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AuditStreamTopologyTest {

    @Mock
    private AuditEntityStreamChangeEventEnricher eventEnricher;

    @Mock
    private AuditEntityStreamChangeEventProcessor eventProcessor;

    private TestOutputTopic<String, IndexRequest> outputTopic;

    private AuditStreamTopology streamTopology;

    private TopologyTestDriver testDriver;

    private static final Properties STREAM_PROPERTIES = getStreamProperties();
    private static final String TOPIC_EGRESS = "processed_audit_test";
    private static final String PREFIX_DOCUMENT_ID = "Test-Doc-Prefix";

    @BeforeEach
    void setUp() throws Exception {
        StreamsBuilder builder = new StreamsBuilder();
        this.streamTopology = Mockito.spy(buildStreamTopology());
        this.streamTopology.withBuilder(builder);
        this.testDriver = new TopologyTestDriver(builder.build(), STREAM_PROPERTIES);
        this.outputTopic = this.testDriver.createOutputTopic(TOPIC_EGRESS, new StringDeserializer(), new IndexRequestDeserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void testMockedStreamStartsAndClosesProperlyInAManagedTopology() {
        KafkaStreams mockStreams = mock(KafkaStreams.class);
        doReturn(mockStreams).when(streamTopology).buildStreams(any());

        streamTopology.start();
        verify(mockStreams, times(1)).start();

        streamTopology.stop();
        verify(mockStreams, times(1)).close();
    }

    @ParameterizedTest
    @MethodSource("com.flipkart.audire.stream.core.topology.TopologyTestArgumentSource#source")
    void testThatTopologyProcessesInputEventWhenDeserializationFails(@TopologyTestArgumentMaker TopologyTestArgument argument) {
        TestInputTopic<String, String> inputTopic = this.testDriver.createInputTopic(argument.getIngressTopic(), new StringSerializer(), new StringSerializer());
        assertThrows(StreamsException.class, () -> inputTopic.pipeInput("This text should fail deserialization"));
    }

    @ParameterizedTest
    @MethodSource("com.flipkart.audire.stream.core.topology.TopologyTestArgumentSource#source")
    void testThatTopologyProcessesInputEventWithNoOutputWhenEnricherReturnsNull(@TopologyTestArgumentMaker TopologyTestArgument argument) {
        when(eventEnricher.enrich(any())).thenReturn(null);
        when(eventProcessor.process(null)).thenReturn(null);

        TestInputTopic<String, String> inputTopic = this.testDriver.createInputTopic(argument.getIngressTopic(), new StringSerializer(), new StringSerializer());
        inputTopic.pipeInput(FixtureHelpers.fixture(argument.getInputTopicEventFilePath()));

        assertEquals(0, outputTopic.getQueueSize());
        verify(eventEnricher, times(1)).enrich(any());
        verify(eventProcessor, times(1)).process(any());
    }

    @ParameterizedTest
    @MethodSource("com.flipkart.audire.stream.core.topology.TopologyTestArgumentSource#source")
    void testThatTopologyProcessesInputEventWithNoOutputWhenProcessorReturnsNull(@TopologyTestArgumentMaker TopologyTestArgument argument) {
        when(eventEnricher.enrich(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(eventProcessor.process(any())).thenReturn(null);

        TestInputTopic<String, String> inputTopic = this.testDriver.createInputTopic(argument.getIngressTopic(), new StringSerializer(), new StringSerializer());
        inputTopic.pipeInput(FixtureHelpers.fixture(argument.getInputTopicEventFilePath()));

        assertEquals(0, outputTopic.getQueueSize());
        verify(eventEnricher, times(1)).enrich(any());
        verify(eventProcessor, times(1)).process(any());
    }

    @ParameterizedTest
    @MethodSource("com.flipkart.audire.stream.core.topology.TopologyTestArgumentSource#source")
    void testThatTopologyProcessesInputEventIntoProcessedEventEndToEnd(@TopologyTestArgumentMaker TopologyTestArgument argument) {
        when(eventEnricher.enrich(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(eventProcessor.process(any())).thenReturn(stubChangelogRecord(argument.getEntityType()));

        TestInputTopic<String, String> inputTopic = this.testDriver.createInputTopic(argument.getIngressTopic(), new StringSerializer(), new StringSerializer());
        inputTopic.pipeInput(FixtureHelpers.fixture(argument.getInputTopicEventFilePath()));
        assertEquals(1, outputTopic.getQueueSize());

        TestRecord<String, IndexRequest> outputRecord = outputTopic.readRecord();
        assertNotNull(outputRecord.timestamp());
        assertEquals(0, outputTopic.getQueueSize());
        assertEquals(String.format("{\"Test-Doc-Prefix\":\"%s-C1-12\"}", argument.getEntityType()), outputRecord.getKey());

        Map<String, Object> indexedDocument = outputRecord.getValue().sourceAsMap();
        Map<String, String> expectedChange = ImmutableMap.of(
                "_operation", "replace",
                "_changedField", "F1",
                "_before", "Old",
                "_after", "New"
        );
        assertEquals(expectedChange, ((List) indexedDocument.get("changes")).get(0));
        assertEquals("Test-Actor", indexedDocument.get("actor"));

        verify(eventEnricher, times(1)).enrich(any());
        verify(eventProcessor, times(1)).process(any());
    }

    private AuditStreamTopology buildStreamTopology() throws Exception {
        TopologyTestArgument.TopologyTestArgumentAggregator argAggregator = new TopologyTestArgument.TopologyTestArgumentAggregator();
        List<TopologyTestArgument> arguments = TopologyTestArgumentSource.source().map(arg ->
                argAggregator.aggregateArguments(new DefaultArgumentsAccessor(arg.get()), null))
                .collect(Collectors.toList());

        AudireStreamAppConfiguration streamConfig = getAppConfig(arguments);
        AuditEntityStreamChangeEventEnricherFactory enricherFactory = getEventEnricherFactory(arguments);
        AuditEntityStreamChangeEventProcessorFactory processorFactory = getEventProcessorFactory(arguments);
        BaseAuditEntityChangeEventDeserializer deserializer = getBaseDeserializer(arguments, streamConfig);

        return new AuditStreamTopology(streamConfig, enricherFactory, processorFactory,
                Serdes.serdeFrom(new ObjectToJsonBytesSerializer<>(), deserializer),
                Serdes.serdeFrom(new IndexRequestKeySerializer(new AuditStoreSinkConfiguration(PREFIX_DOCUMENT_ID)), new IndexRequestDeserializer()),
                Serdes.serdeFrom(new IndexRequestValueSerializer(), new IndexRequestDeserializer()),
                new ElasticSearchChangelogIndexRequestBuilder()
        );
    }

    private BaseAuditEntityChangeEventDeserializer getBaseDeserializer(List<TopologyTestArgument> arguments, AudireStreamAppConfiguration streamConfig) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Map<String, AuditEntityStreamChangeEventDeserializer> topicDeserializerMap = new HashMap<>();
        for (TopologyTestArgument arg : arguments) {
            AuditEntityStreamConfigurationFactory streamConfigFactory = new AuditEntityStreamConfigurationFactory(streamConfig.getAuditStreamEntityConfig());
            Class<AuditEntityStreamChangeEventDeserializer> _deserializerClass = (Class<AuditEntityStreamChangeEventDeserializer>) Class.forName(arg.getDeserializerClass());
            Constructor<AuditEntityStreamChangeEventDeserializer> ctor = _deserializerClass.getDeclaredConstructor(AuditEntityStreamConfigurationFactory.class);
            AuditEntityStreamChangeEventDeserializer deserializer = ctor.newInstance(streamConfigFactory);
            topicDeserializerMap.put(arg.getIngressTopic(), deserializer);
        }
        return new BaseAuditEntityChangeEventDeserializer(
                new AuditEntityStreamChangeEventDeserializerFactory(topicDeserializerMap)
        );
    }

    private AuditEntityStreamChangeEventEnricherFactory getEventEnricherFactory(List<TopologyTestArgument> arguments) {
        Map<String, AuditEntityStreamChangeEventEnricher> entityEnricherMap = arguments.stream().collect(Collectors.toMap(
                TopologyTestArgument::getEntityType, arg -> eventEnricher));

        return new AuditEntityStreamChangeEventEnricherFactory(entityEnricherMap);
    }

    private AuditEntityStreamChangeEventProcessorFactory getEventProcessorFactory(List<TopologyTestArgument> arguments) {
        Map<String, AuditEntityStreamChangeEventProcessor> entityEnricherMap = arguments.stream().collect(Collectors.toMap(
                TopologyTestArgument::getEntityType, arg -> eventProcessor));

        return new AuditEntityStreamChangeEventProcessorFactory(entityEnricherMap);
    }

    private AudireStreamAppConfiguration getAppConfig(List<TopologyTestArgument> arguments) {
        AudireStreamAppConfiguration appConfig = TopologyTestArgumentSource.readAuditStreamAppYaml();

        Map<String, AuditEntityStreamConfiguration> entityStreamConfigMap = arguments.stream()
                .collect(Collectors.toMap(TopologyTestArgument::getEntityType,
                        arg -> appConfig.getAuditStreamEntityConfig().get(arg.getEntityType())));

        return AudireStreamAppConfiguration.builder()
                .auditStreamEntityConfig(entityStreamConfigMap)
                .kafkaStreamConfig(KafkaStreamConfiguration.builder()
                        .applicationId(STREAM_PROPERTIES.getProperty(StreamsConfig.APPLICATION_ID_CONFIG))
                        .bootstrapServers(STREAM_PROPERTIES.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG))
                        .metadataMaxAgeConfig(Duration.milliseconds(Long.parseLong(STREAM_PROPERTIES.getProperty(CommonClientConfigs.METADATA_MAX_AGE_CONFIG))))
                        .commitIntervalConfig(Duration.milliseconds(Long.parseLong(STREAM_PROPERTIES.getProperty(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG))))
                        .autoOffsetResetConfig(STREAM_PROPERTIES.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG))
                        .egressTopic(TOPIC_EGRESS).build())
                .build();
    }

    private ChangelogRecord stubChangelogRecord(String entityType) {
        return ChangelogRecord.builder()
                .entityType(entityType)
                .entityId("C1").version(12L)
                .actor("Test-Actor")
                .changes(Lists.newArrayList(
                        ChangelogRecord.Changelog.builder()
                                ._before(new TextNode("Old"))
                                ._after(new TextNode("New"))
                                ._changedField("F1")
                                ._operation("replace")
                                .build()
                ))
                .build();
    }

    private static Properties getStreamProperties() {
        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "Test-Application-ID");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(CommonClientConfigs.METADATA_MAX_AGE_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return props;
    }
}
