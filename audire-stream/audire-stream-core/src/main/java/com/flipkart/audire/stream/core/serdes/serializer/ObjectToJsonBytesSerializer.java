package com.flipkart.audire.stream.core.serdes.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

@Singleton
public class ObjectToJsonBytesSerializer<T> implements Serializer<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return new byte[0];
        }
        try {
            return MAPPER.writeValueAsBytes(data);

        } catch (Exception ex) {
            throw new SerializationException("Error serializing JSON message", ex);
        }
    }
}
