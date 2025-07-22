package com.agilecheckup.persistency.entity.base;

public interface TenantableV2 {
    String getTenantId();
    void setTenantId(String tenantId);
}