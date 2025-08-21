package com.agilecheckup.service.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantAccessViolationException extends RuntimeException {

  private static final String message = "Access denied: Invalid or unauthorized tenant ID '%s'";

  public TenantAccessViolationException(String invalidTenantId) {
    super(String.format(message, invalidTenantId));
  }
}
