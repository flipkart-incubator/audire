package com.flipkart.audire.service.app.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public final class ExceptionalResponse {

    private final int errorCode;
    private final String errorMessage;
    private final String detailMessage;
    private final String errorType;
    private String timestamp;

    public static ExceptionalResponseBuilder builder() {
        return new ExceptionalResponseWithStatusCodeBuilder();
    }

    private static class ExceptionalResponseWithStatusCodeBuilder extends ExceptionalResponseBuilder {

        @Override
        public ExceptionalResponse build() {
            super.timestamp = Instant.now().toString();
            return super.build();
        }
    }
}
