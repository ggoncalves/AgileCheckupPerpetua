package com.agilecheckup.security;

import com.agilecheckup.persistency.entity.base.TenantableEntity;
import com.agilecheckup.service.exception.TenantAccessViolationException;

import lombok.NonNull;

public class TenantAccessValidator {

  public static void validateTenantAccess(@NonNull String tenantId, @NonNull TenantableEntity entity) {
    if (!tenantId.equals(entity.getTenantId())) {
      throw new TenantAccessViolationException(tenantId);
    }
  }
}
