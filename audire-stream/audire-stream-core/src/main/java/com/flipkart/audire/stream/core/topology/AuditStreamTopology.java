package com.flipkart.audire.stream.core.topology;

import com.flipkart.audire.stream.core.config.AudireStreamAppConfiguration;
import com.flipkart.audire.stream.core.config.AuditEntityStreamConfiguration;
import com.flipkart.audire.stream.core.config.KafkaStreamConfiguration;
import com.flipkart.audire.stream.core.enricher.AuditEntityStreamChangeEventEnricherFactory;
import com.flipkart.audire.stream.core.indexer.ElasticSearchChangelogIndexRequestBuilder;
import com.flipkart.audire.stream.core.processor.AuditEntityStreamChangeEventProcessorFactory;
import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.flipkart.audire.stream.model.ChangelogRecord;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.elasticsearch.action.index.IndexRequest;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AuditStreamTopology implements Managed {

    private final AudireStreamAppConfiguration streamConfig;
    private final AuditEntityStreamChangeEventEnricherFactory eventEnricherFactory;
    private final AuditEntityStreamChangeEventProcessorFactory eventProcessorFactory;
    private final Serde<AuditStreamEntityChangeEvent> auditSerde;
    private final Serde<IndexRequest> indexRequestKeySerde;
    private final Serde<IndexRequest> indexRequestValueSerde;
    private final ElasticSearchChangelogIndexRequestBuilder indexRequestBuilder;

    private KafkaStreams streams;

    @Inject
    public AuditStreamTopology(AudireStreamAppConfiguration streamConfig,
                               AuditEntityStreamChangeEventEnricherFactory eventEnricherFactory,
                               AuditEntityStreamChangeEventProcessorFactory eventProcessorFactory,
                               Serde<AuditStreamEntityChangeEvent> auditSerde,
                               @Named("key") Serde<IndexRequest> indexRequestKeySerde,
                               @Named("value") Serde<IndexRequest> indexRequestValueSerde,
                               ElasticSearchChangelogIndexRequestBuilder indexRequestBuilder) {
        this.streamConfig = streamConfig;
        this.eventEnricherFactory = eventEnricherFactory;
        this.eventProcessorFactory = eventProcessorFactory;
        this.auditSerde = auditSerde;
        this.indexRequestKeySerde = indexRequestKeySerde;
        this.indexRequestValueSerde = indexRequestValueSerde;
        this.indexRequestBuilder = indexRequestBuilder;
    }

    @Override
    public void start() {
        this.streams = buildStreams(getProperties());
        this.streams.start();
    }

    @Override
    public void stop() {
        if (this.streams != null) {
            log.info("Shutting down the stream application");
            this.streams.close();
        }
    }

    @VisibleForTesting
    void withBuilder(StreamsBuilder builder) {
        builder.stream(getTopics(), Consumed.with(Serdes.String(), auditSerde))
                .filter((key, changeEvent) -> changeEvent != null)
                .mapValues((key, changeEvent) -> {
                    String entityType = changeEvent.getEntityType();
                    AuditStreamEntityChangeEvent enrichedChangeEvent = eventEnricherFactory.get(entityType).enrich(changeEvent);
                    ChangelogRecord processedChangeEvent = eventProcessorFactory.get(entityType).process(enrichedChangeEvent);
                    return indexRequestBuilder.buildRequest(processedChangeEvent);
                })
                .filter((key, request) -> request != null)
                .selectKey((key, request) -> request)
                .to(streamConfig.getKafkaStreamConfig().getEgressTopic(), Produced.with(indexRequestKeySerde, indexRequestValueSerde));
    }

    @VisibleForTesting
    KafkaStreams buildStreams(Properties properties) {
        StreamsBuilder builder = new StreamsBuilder();
        this.withBuilder(builder);
        return new KafkaStreams(builder.build(), properties);
    }

    private Set<String> getTopics() {
        return streamConfig.getAuditStreamEntityConfig().values().stream()
                .filter(config -> !config.isSkip())
                .map(AuditEntityStreamConfiguration::getIngressTopic)
                .collect(Collectors.toSet());
    }

    private Properties getProperties() {
        KafkaStreamConfiguration conf = streamConfig.getKafkaStreamConfig();
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, conf.getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getBootstrapServers());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, conf.getCacheMaxBytesBuffering());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, conf.getCommitIntervalConfig().toMilliseconds());
        props.put(CommonClientConfigs.METADATA_MAX_AGE_CONFIG, conf.getMetadataMaxAgeConfig().toMilliseconds());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, conf.getAutoOffsetResetConfig());
        return props;
    }
}
