package com.flipkart.audire.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class Filter {

    private String fromDate;
    private String toDate;
}
