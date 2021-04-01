package com.flipkart.audire.stream.core.topology;

import org.junit.jupiter.params.aggregator.AggregateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AggregateWith(TopologyTestArgument.TopologyTestArgumentAggregator.class)
@interface TopologyTestArgumentMaker {

}
