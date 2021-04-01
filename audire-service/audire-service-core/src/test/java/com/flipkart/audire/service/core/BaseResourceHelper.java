package com.flipkart.audire.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.FixtureHelpers;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Map;

@UtilityClass
@SuppressWarnings("unchecked")
public class BaseResourceHelper {

    private final ObjectMapper MAPPER = new ObjectMapper();

    public Map<String, Object> getElasticSearchMapResponse() throws IOException {
        return MAPPER.readValue(
                FixtureHelpers.fixture("fixtures/elastic_search_source_response.json"),
                Map.class);
    }
}
