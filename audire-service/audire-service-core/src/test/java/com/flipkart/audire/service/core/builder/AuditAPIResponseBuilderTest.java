package com.flipkart.audire.service.core.builder;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.audire.service.api.response.AuditLogFetchAPIResponse;
import com.flipkart.audire.service.core.BaseResourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditAPIResponseBuilderTest {

    private AuditAPIResponseBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = new AuditAPIResponseBuilder();
    }

    @Test
    void testBuildAuditResponse() throws IOException {
        Map<String, Object> sourceMap = BaseResourceHelper.getElasticSearchMapResponse();
        AuditLogFetchAPIResponse.Audit audit = builder.buildAuditResponse(sourceMap);

        assertEntityAttributes(audit);
        assertSystemicAttributes(audit);
        assertChangeAttributes(audit);
        assertEquals("2020-12-27T11:59:20Z", audit.getChangedAt());
    }

    private void assertChangeAttributes(AuditLogFetchAPIResponse.Audit audit) {
        assertEquals(1, audit.getChanges().size());
        assertEquals("status", audit.getChanges().get(0).getField());
        assertEquals("replace", audit.getChanges().get(0).getOperation());

        assertEquals("DRAFT", ((TextNode) audit.getChanges().get(0).getBefore()).asText());
        assertEquals("SERVICEABLE", ((TextNode) audit.getChanges().get(0).getAfter()).asText());

        assertEquals(1, audit.getMeta().size());
        assertEquals("K", audit.getMeta().get(0).getKey());
        assertTrue(((BooleanNode) audit.getMeta().get(0).getVal()).asBoolean());
    }

    private void assertEntityAttributes(AuditLogFetchAPIResponse.Audit audit) {
        assertEquals("E", audit.getEntityId());
        assertEquals("DUMMY_ENTITY_AUDIT", audit.getEntityType());
        assertEquals(12, audit.getVersion());
        assertEquals("I1", audit.getAuditId());
        assertEquals("P1", audit.getAuditPrevId());
        assertEquals("actor", audit.getActor());
        assertEquals("O1", audit.getOwnerId());
    }

    private void assertSystemicAttributes(AuditLogFetchAPIResponse.Audit audit) {
        assertFalse(audit.isFirst());
        assertFalse(audit.isSystemic());
        assertEquals("ET1", audit.getEventTraceId());
    }
}
