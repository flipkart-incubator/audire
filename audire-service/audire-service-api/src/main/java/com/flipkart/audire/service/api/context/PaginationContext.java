package com.flipkart.audire.service.api.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flipkart.audire.service.model.Pagination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationContext {

    @QueryParam("sortOn")
    @DefaultValue("changedAt")
    @Builder.Default
    private String sortOn = "id";

    @QueryParam("page")
    @DefaultValue(value = "1")
    @Min(1)
    private int pageNumber;

    @QueryParam("offset")
    @DefaultValue(value = "0")
    @Min(0)
    private int offset;

    @QueryParam("limit")
    @DefaultValue(value = "20")
    @Min(1)
    private int limit;

    @QueryParam("ascending")
    @DefaultValue("false")
    @Builder.Default
    private boolean ascending = false;

    @JsonIgnore
    public Pagination getPagination() {
        return Pagination.builder()
                .sortOn(this.sortOn)
                .pageNumber(this.pageNumber)
                .offset(this.offset)
                .limit(this.limit)
                .ascending(this.ascending)
                .build();
    }
}
