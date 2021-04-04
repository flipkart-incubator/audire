package com.flipkart.audire.example.app.mapper;

import com.flipkart.audire.stream.application.exception.ErrorCode;
import com.flipkart.m3.jerseylogutils.exception.ExceptionalResponse;
import io.dropwizard.jersey.validation.JerseyViolationException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Response;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JerseyViolationExceptionMapperTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testAppropriateResponseIsReturnedOnJerseyViolation() {
        ValidatorTest validatorTest = new ValidatorTest(StringUtils.EMPTY);
        Set<ConstraintViolation<ValidatorTest>> violations = this.validator.validate(validatorTest);

        JerseyViolationExceptionMapper mapper = new JerseyViolationExceptionMapper();
        Response response = mapper.toResponse(new JerseyViolationException(violations, null));

        ExceptionalResponse exceptionalResponse = (ExceptionalResponse) response.getEntity();
        assertNotNull(exceptionalResponse);
        assertEquals(1, violations.size());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Violation on [id] : value should be <= 3", exceptionalResponse.getDetailMessage());
        assertEquals(ErrorCode.JERSEY_VIOLATION_ERROR.getCode(), exceptionalResponse.getErrorCode());
    }

    @AllArgsConstructor
    private static class ValidatorTest {
        @Size(min = 3, message = "value should be <= 3")
        String id;
    }
}
