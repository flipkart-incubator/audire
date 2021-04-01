package com.flipkart.audire.stream.core.deserializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseAuditEntityChangeEventDeserializerTest {

    @Mock
    private AuditEntityStreamChangeEventDeserializerFactory mockFactory;

    @Mock
    private AuditEntityStreamChangeEventDeserializer baseDeserializer;

    private BaseAuditEntityChangeEventDeserializer deserializer;

    @BeforeEach
    void setUp() {
        when(mockFactory.get("Topic")).thenReturn(baseDeserializer);
        this.deserializer = new BaseAuditEntityChangeEventDeserializer(mockFactory);
    }

    @Test
    void testDeserializeDelegatesToBase() {
        deserializer.deserialize("Topic", new byte[100]);
        verify(baseDeserializer, times(1)).deserialize(any());
    }
}
