package com.agilecheckup.security;

import lombok.Getter;

public enum TokenField {

  TENANT_ID("tenantId"), ASSESSMENT_MATRIX_ID("assessmentMatrixId");

  @Getter
  private final String fieldName;

  TokenField(String fieldName) {
    this.fieldName = fieldName;
  }
}