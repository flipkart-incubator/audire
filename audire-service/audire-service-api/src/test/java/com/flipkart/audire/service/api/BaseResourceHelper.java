package com.flipkart.audire.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import io.dropwizard.testing.FixtureHelpers;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class BaseResourceHelper {

    private final ObjectMapper MAPPER = new ObjectMapper();

    public AuditLogFetchAPIResponse getAuditLogFetchAPIResponse() throws IOException {
        return MAPPER.readValue(
                FixtureHelpers.fixture("fixtures/audit_log_fetch_api_response.json"),
                AuditLogFetchAPIResponse.class);
    }
}
