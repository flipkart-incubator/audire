package com.flipkart.audire.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class Pagination {

    private String sortOn;
    private int pageNumber;
    private int offset;
    private int limit;
    private boolean ascending;
}
