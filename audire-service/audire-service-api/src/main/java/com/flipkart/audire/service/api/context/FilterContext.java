package com.flipkart.audire.service.api.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flipkart.audire.service.model.Filter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.ws.rs.QueryParam;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterContext {

    @QueryParam("fromDate")
    private String fromDate;

    @QueryParam("toDate")
    private String toDate;

    @JsonIgnore
    public Filter getFilter() {
        return Filter.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }
}
