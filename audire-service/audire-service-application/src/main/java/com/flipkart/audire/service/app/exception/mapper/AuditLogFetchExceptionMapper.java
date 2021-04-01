package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.flipkart.audire.service.core.exception.AuditLogFetchException;

import javax.ws.rs.core.Response;

public class AuditLogFetchExceptionMapper extends CustomExceptionMapper<AuditLogFetchException> {

    public AuditLogFetchExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Override
    protected String getMessage(AuditLogFetchException ex) {
        return String.format("Failed to fetch audits %s: %s", ErrorCode.AUDIT_FETCH_FAILED.getMessage(), ex.getMessage());
    }

    @Override
    protected int getErrorCode(AuditLogFetchException ex) {
        return ErrorCode.AUDIT_FETCH_FAILED.getCode();
    }
}
