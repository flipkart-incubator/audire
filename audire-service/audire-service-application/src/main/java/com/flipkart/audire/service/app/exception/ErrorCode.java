package com.flipkart.audire.service.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    UNKNOWN_ERROR(12500, "Unknown error occurred"),
    JERSEY_VIOLATION_ERROR(12501, "Jersey violation error for the client request"),
    WEB_APPLICATION_EXCEPTION(12502, "Web application exception has occurred"),

    AUDIT_FETCH_FAILED(12503, "Failed to fetch audits for the given request");

    private final int code;
    private final String message;
}
