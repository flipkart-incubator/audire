package com.flipkart.audire.service.app.exception;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionalResponseTest {

    @Test
    public void testBuilderPopulatesRequiredMethods() {
        ExceptionalResponse response = ExceptionalResponse.builder().errorCode(23).build();
        Assertions.assertEquals(23, response.getErrorCode());
        Assertions.assertFalse(Strings.isNullOrEmpty(response.getTimestamp()));
    }
}
