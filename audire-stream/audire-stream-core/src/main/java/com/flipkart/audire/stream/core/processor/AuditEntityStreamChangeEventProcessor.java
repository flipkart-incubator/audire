package com.flipkart.audire.stream.core.processor;

import com.flipkart.audire.stream.model.AuditStreamEntityChangeEvent;
import com.flipkart.audire.stream.model.ChangelogRecord;

public interface AuditEntityStreamChangeEventProcessor {

    /**
     * Process an audit entity change event and generate a change log record. The change log
     * record can be null in which case it is assumed that no valid processing has occurred.
     *
     * @throws AuditEntityStreamChangeEventProcessorException if an exception is raised during event processing
     */
    ChangelogRecord process(AuditStreamEntityChangeEvent changeEvent);
}
