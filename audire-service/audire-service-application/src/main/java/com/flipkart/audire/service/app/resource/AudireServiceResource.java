package com.flipkart.audire.service.app.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.flipkart.audire.service.api.context.FilterContext;
import com.flipkart.audire.service.api.context.PaginationContext;
import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.service.core.manager.AudireServiceManager;
import com.google.inject.Inject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/audits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AudireServiceResource {

    private final AudireServiceManager manager;

    @Inject
    public AudireServiceResource(AudireServiceManager manager) {
        this.manager = manager;
    }

    @POST
    @Timed
    @ExceptionMetered
    public AuditLogFetchAPIResponse getAudits(@NotNull @Valid AuditLogFetchAPIRequest request,
                                              @NotNull @Valid @BeanParam PaginationContext paginationContext,
                                              @Valid @BeanParam FilterContext filterContext) {
        return manager.getAudits(request, paginationContext.getPagination(), filterContext.getFilter());
    }
}
