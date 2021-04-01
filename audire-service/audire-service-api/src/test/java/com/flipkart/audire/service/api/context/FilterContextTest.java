package com.flipkart.audire.service.api.context;

import com.flipkart.audire.service.model.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterContextTest {

    @Test
    void testGetFilter() {
        FilterContext context = FilterContext.builder()
                .fromDate("2020-12-27T11:59:20Z")
                .toDate("2021-12-27T11:59:20Z").build();

        Filter filter = context.getFilter();
        assertEquals("2020-12-27T11:59:20Z", filter.getFromDate());
        assertEquals("2021-12-27T11:59:20Z", filter.getToDate());
    }
}
