package com.flipkart.audire.service.api.response;

import com.flipkart.audire.service.api.BaseResourceHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.flipkart.audire.stream.model.EntityType.BANNER_GROUP_AUDIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuditLogFetchAPIResponseTest {

    @Test
    void testDeserializationWorksAsExpected() throws IOException {
        AuditLogFetchAPIResponse response = BaseResourceHelper.getAuditLogFetchAPIResponse();
        assertEquals(4641, response.getTotal());
        assertEquals(1, response.getPageCount());

        AuditLogFetchAPIResponse.Audit audit = response.getAudits().get(0);
        assertFalse(audit.isFirst());
        assertFalse(audit.isSystemic());
        assertEquals("E1", audit.getEntityId());
        assertEquals(BANNER_GROUP_AUDIT, audit.getEntityType());
        assertEquals("ET", audit.getEventTraceId());

        assertEquals("I1", audit.getAuditId());
        assertEquals("IP1", audit.getAuditPrevId());
        assertEquals(1, audit.getChanges().size());
        assertEquals("F1", audit.getChanges().get(0).getField());
        assertEquals("replace", audit.getChanges().get(0).getOperation());
        assertEquals(true, audit.getChanges().get(0).getBefore());
        assertEquals("N2", audit.getChanges().get(0).getAfter());

        assertEquals(1, audit.getMeta().size());
        assertEquals("K1", audit.getMeta().get(0).getKey());
        assertEquals(2, audit.getMeta().get(0).getVal());

        assertEquals(1609070360000L, audit.getVersion());
        assertEquals("2020-12-27T11:59:20Z", audit.getChangedAt());
        assertEquals("actor", audit.getActor());
        assertEquals("1019", audit.getOwnerId());
    }
}
