package com.flipkart.audire.stream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventType {

    CREATE('c'),
    UPDATE('u'),
    DELETE('d'),
    READ('r');

    private char value;

    public static EventType fromOpCode(char opcode) {
        switch (opcode) {
            case 'c':
                return CREATE;
            case 'r':
                return READ;
            case 'u':
                return UPDATE;
            case 'd':
                return DELETE;
            default:
                throw new IllegalArgumentException(String.format("No change event defined with op code [%s]", opcode));
        }
    }
}
