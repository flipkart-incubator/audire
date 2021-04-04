package com.flipkart.audire.stream.core.topology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

@Getter
@AllArgsConstructor
@Builder
class TopologyTestArgument {

    private final String ingressTopic;
    private final String inputTopicEventFilePath;
    private final String entityType;
    private final String deserializerClass;

    public static class TopologyTestArgumentAggregator implements ArgumentsAggregator {

        @Override
        public TopologyTestArgument aggregateArguments(ArgumentsAccessor arg, ParameterContext context) throws ArgumentsAggregationException {
            try {
                return builder()
                        .ingressTopic(arg.getString(0))
                        .inputTopicEventFilePath(arg.getString(1))
                        .entityType(arg.getString(2))
                        .deserializerClass(arg.getString(3))
                        .build();

            } catch (Exception ex) {
                throw new ArgumentsAggregationException(arg + " is not a valid topology test argument that can be aggregated");
            }
        }
    }
}
