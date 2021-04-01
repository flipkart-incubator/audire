package com.flipkart.audire.stream.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class EventTypeTest {

    @Test
    void testFromOpCode() {
        assertAll(
                () -> assertEquals(EventType.CREATE, EventType.fromOpCode('c')),
                () -> assertEquals(EventType.DELETE, EventType.fromOpCode('d')),
                () -> assertEquals(EventType.UPDATE, EventType.fromOpCode('u')),
                () -> assertEquals(EventType.READ, EventType.fromOpCode('r'))
        );
        assertThrows(IllegalArgumentException.class, () -> EventType.fromOpCode('x'));
    }
}
