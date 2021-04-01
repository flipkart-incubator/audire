package com.flipkart.audire.service.api.context;

import com.flipkart.audire.service.model.Pagination;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationContextTest {

    @Test
    void testGetPagination() {
        PaginationContext context = PaginationContext.builder().ascending(true)
                .limit(10).offset(2).pageNumber(1).sortOn("S1").build();

        Pagination pagination = context.getPagination();
        assertTrue(pagination.isAscending());
        assertEquals("S1", pagination.getSortOn());
        assertEquals(10, pagination.getLimit());
        assertEquals(1, pagination.getPageNumber());
        assertEquals(2, pagination.getOffset());
    }
}
