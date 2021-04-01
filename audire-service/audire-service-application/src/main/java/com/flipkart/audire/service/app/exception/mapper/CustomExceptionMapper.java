package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ExceptionalResponse;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.time.Instant;

@Slf4j
@SuppressWarnings("WeakerAccess")
public class CustomExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private static final String DEFAULT_ERROR_MESSAGE = "Unknown error";
    private final int errorStatus;

    public CustomExceptionMapper(int errorStatus) {
        this.errorStatus = errorStatus;
    }

    /**
     * Provide a detailed message string from the caught exception. Default
     * implementation returns the exception message.
     * <p>
     * Implementers should provide logic to construct a detailed error message
     * deriving from the caught exception
     *
     * @param t the caught exception
     * @return String with a custom exception message
     */
    protected String getCause(final T t) {
        return Throwables.getRootCause(t).getMessage();
    }

    /**
     * Provide a human readable message string for the caught exception.
     * <p>
     * Implementers should provide logic to construct a human readable error message
     * deriving from the caught exception as this could be a renderable entity
     *
     * @param t the caught exception
     * @return String with a custom exception message
     */
    protected String getMessage(final T t) {
        if (Strings.isNullOrEmpty(t.getMessage())) {
            return DEFAULT_ERROR_MESSAGE;
        }
        return t.getMessage();
    }

    /**
     * Provide a custom error code to override the default status code.
     * <p>
     * Default error code is the one that was used during the construction
     * of this exception mapper as a final parameter
     */
    protected int getErrorCode(final T t) {
        return errorStatus;
    }

    @Override
    public final Response toResponse(T t) {
        final String message = getMessage(t);
        final int errorCode = getErrorCode(t);
        final String cause = getCause(t);
        log.error(message, t);

        return Response.status(errorStatus)
                .entity(ExceptionalResponse.builder()
                        .errorCode(errorCode)
                        .errorType(t.getClass().getName())
                        .timestamp(Instant.now().toString())
                        .errorMessage(message)
                        .detailMessage(cause)
                        .build()
                ).build();
    }
}
