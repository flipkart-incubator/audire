package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.flipkart.audire.service.app.exception.ExceptionalResponse;
import com.flipkart.audire.service.core.exception.AuditLogFetchException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogFetchExceptionMapperTest {

    private AuditLogFetchExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AuditLogFetchExceptionMapper();
    }

    @Test
    void testAppropriateResponseIsReturnedOnException() {
        ErrorCode code = ErrorCode.AUDIT_FETCH_FAILED;
        AuditLogFetchException exception = new AuditLogFetchException(new Exception("Expected error"));
        Response response = mapper.toResponse(exception);

        ExceptionalResponse exceptionalResponse = (ExceptionalResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(code.getCode(), exceptionalResponse.getErrorCode());
        assertTrue(StringUtils.isNotEmpty(exceptionalResponse.getDetailMessage()));
    }
}
