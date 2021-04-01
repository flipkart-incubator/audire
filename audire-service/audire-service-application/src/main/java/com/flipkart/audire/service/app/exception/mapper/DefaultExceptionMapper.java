package com.flipkart.audire.service.app.exception.mapper;

import com.flipkart.audire.service.app.exception.ErrorCode;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;

@Slf4j
@Singleton
public class DefaultExceptionMapper extends CustomExceptionMapper<Exception> {

    public DefaultExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Override
    protected int getErrorCode(Exception ex) {
        return ErrorCode.UNKNOWN_ERROR.getCode();
    }
}
