package com.agilecheckup.persistency.entity.base;

import java.time.Instant;

public interface AuditableV2 {
    Instant getCreatedDate();
    void setCreatedDate(Instant createdDate);
    
    Instant getLastUpdatedDate();
    void setLastUpdatedDate(Instant lastUpdatedDate);
}