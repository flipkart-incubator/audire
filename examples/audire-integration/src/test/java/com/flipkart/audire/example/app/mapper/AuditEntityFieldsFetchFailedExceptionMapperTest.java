package com.flipkart.audire.example.app.mapper;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditEntityFieldsFetchFailedExceptionMapperTest {

    private AuditEntityFieldsFetchFailedExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AuditEntityFieldsFetchFailedExceptionMapper();
    }

    @Test
    void testAppropriateResponseIsReturnedOnException() {
        ErrorCode code = ErrorCode.AUDIT_ENTITY_FIELDS_FETCH_FAILED;
        AuditEntityFieldsFetchFailedException exception = new AuditEntityFieldsFetchFailedException(new Exception("Expected error"));
        Response response = mapper.toResponse(exception);

        ExceptionalResponse exceptionalResponse = (ExceptionalResponse) response.getEntity();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(code.getCode(), exceptionalResponse.getErrorCode());
        assertTrue(StringUtils.isNotEmpty(exceptionalResponse.getDetailMessage()));
    }
}
