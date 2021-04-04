package com.flipkart.audire.service.app.resource;

import com.flipkart.audire.service.api.request.AuditLogFetchAPIRequest;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.service.core.manager.AudireServiceManager;
import com.google.common.collect.Sets;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class AudireServiceResourceTest {

    private final AudireServiceManager appManager = mock(AudireServiceManager.class);

    private static final String PATH_API = "/audits";

    private final ResourceExtension resource = ResourceExtension.builder()
            .addResource(new AudireServiceResource(appManager))
            .build();

    @Test
    void testGetEntityFieldsReturnsSuccessWhenManagerReturnsSuccess() {
        when(appManager.getAudits(any(), any(), any()))
                .thenReturn(AuditLogFetchAPIResponse.builder()
                        .pageCount(10).hasNextPage(false)
                        .build());

        AuditLogFetchAPIResponse response = resource.target(PATH_API).request()
                .post(Entity.json(stubFetchRequest()), AuditLogFetchAPIResponse.class);

        assertEquals(10, response.getPageCount());
        verify(appManager, times(1)).getAudits(any(), any(), any());
    }

    private AuditLogFetchAPIRequest stubFetchRequest() {
        return AuditLogFetchAPIRequest.builder()
                .entityType("E")
                .entityId(Sets.newHashSet("C1", "C2"))
                .build();
    }
}
