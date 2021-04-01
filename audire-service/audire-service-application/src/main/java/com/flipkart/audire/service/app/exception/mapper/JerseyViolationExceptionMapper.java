package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.google.inject.Singleton;
import io.dropwizard.jersey.validation.JerseyViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class JerseyViolationExceptionMapper extends CustomExceptionMapper<JerseyViolationException> {

    public JerseyViolationExceptionMapper() {
        super(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Override
    public String getCause(JerseyViolationException ex) {
        return Optional.ofNullable(ex.getConstraintViolations())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(this::getErrorMessageDescription)
                .collect(Collectors.joining("; "));
    }

    @Override
    protected int getErrorCode(JerseyViolationException e) {
        return ErrorCode.JERSEY_VIOLATION_ERROR.getCode();
    }

    private String getErrorMessageDescription(ConstraintViolation violation) {
        String attr = ((PathImpl) violation.getPropertyPath()).getLeafNode().asString();
        return String.format("Violation on [%s] : %s", attr, violation.getMessage());
    }
}
