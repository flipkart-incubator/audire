package com.flipkart.audire.stream.core.serdes.serializer;

import com.google.inject.Singleton;
import org.apache.kafka.common.serialization.Serializer;
import org.elasticsearch.action.index.IndexRequest;

@Singleton
public class IndexRequestValueSerializer implements Serializer<IndexRequest> {

    @Override
    public byte[] serialize(String topic, IndexRequest data) {
        if (data == null) {
            return new byte[0];
        }
        return data.source().toBytesRef().bytes;
    }
}
