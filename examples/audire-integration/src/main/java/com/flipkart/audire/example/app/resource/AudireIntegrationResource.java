package com.flipkart.audire.example.app.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.flipkart.audire.example.app.api.EntityFieldResponse;
import com.flipkart.audire.example.app.manager.EntityFieldManager;
import com.google.inject.Inject;

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/audits/stream")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AudireIntegrationResource {

    private final EntityFieldManager entityFieldManager;

    @Inject
    public AudireIntegrationResource(EntityFieldManager entityFieldManager) {
        this.entityFieldManager = entityFieldManager;
    }

    @GET
    @Path("/entity_fields")
    @Timed
    @ExceptionMetered
    public EntityFieldResponse getEntityFields(@QueryParam("entityType") @NotEmpty Set<String> entityTypes,
                                               @QueryParam("includeSystemic") @DefaultValue("false") boolean isSystemic) {
        return entityFieldManager.getEntityFields(entityTypes, isSystemic);
    }
}
