package com.flipkart.audire.example.app.resource;

import com.flipkart.audire.example.app.api.EntityFieldResponse;
import com.flipkart.audire.example.app.manager.EntityFieldManager;
import com.google.common.collect.Sets;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(DropwizardExtensionsSupport.class)
class AudireIntegrationResourceTest {

    private final EntityFieldManager fieldManager = mock(EntityFieldManager.class);

    private static final String PATH_API = "/audits/stream";
    private static final String PATH_ENTITY_FIELDS = "/entity_fields";

    private final ResourceExtension resource = ResourceExtension.builder()
            .addResource(new AudireIntegrationResource(fieldManager))
            .build();

    @Test
    void testGetEntityFieldsReturns400WhenEntityTypeQueryParamIsEmpty() {
        Response response = resource.target(PATH_API + PATH_ENTITY_FIELDS).request().get();
        assertEquals(400, response.getStatus());
    }

    @Test
    void testGetEntityFieldsReturnsSuccessWhenManagerReturnsSuccess() {
        when(fieldManager.getEntityFields(Sets.newHashSet("E1", "E2"), false))
                .thenReturn(EntityFieldResponse.builder()
                        .fieldTypes(Sets.newHashSet("F1", "F2"))
                        .build());

        EntityFieldResponse response = resource.target(PATH_API + PATH_ENTITY_FIELDS)
                .queryParam("entityType", "E1")
                .queryParam("entityType", "E2")
                .request()
                .get(EntityFieldResponse.class);

        assertEquals(2, response.getFieldTypes().size());
        verify(fieldManager, times(1)).getEntityFields(Sets.newHashSet("E1", "E2"), false);
    }
}
