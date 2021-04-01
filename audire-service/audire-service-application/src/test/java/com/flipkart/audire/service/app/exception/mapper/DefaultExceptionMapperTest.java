package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.flipkart.audire.service.app.exception.ExceptionalResponse;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultExceptionMapperTest {

    @Test
    void testAppropriateResponseIsReturnedOnUnknownException() {
        DefaultExceptionMapper mapper = new DefaultExceptionMapper();
        String exMessage = "Expected Error";
        Response response = mapper.toResponse(new IllegalStateException(exMessage));

        ExceptionalResponse ex = (ExceptionalResponse) response.getEntity();
        assertNotNull(response.getEntity());
        assertNotNull(((ExceptionalResponse) response.getEntity()).getTimestamp());
        assertEquals(INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(ErrorCode.UNKNOWN_ERROR.getCode(), ex.getErrorCode());
        assertEquals(IllegalStateException.class.getName(), ex.getErrorType());
    }
}
