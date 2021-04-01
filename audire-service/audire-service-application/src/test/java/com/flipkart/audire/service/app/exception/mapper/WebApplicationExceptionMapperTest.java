package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.flipkart.audire.service.app.exception.ExceptionalResponse;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebApplicationExceptionMapperTest {

    @Test
    void testAppropriateResponseIsReturnedOnWebApplicationException() {
        WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
        String exMessage = "Expected Error";
        Response response = mapper.toResponse(new WebApplicationException(exMessage, Response.Status.CONFLICT));
        ExceptionalResponse exceptionalResponse = (ExceptionalResponse) response.getEntity();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(ErrorCode.WEB_APPLICATION_EXCEPTION.getCode(), exceptionalResponse.getErrorCode());
    }
}
