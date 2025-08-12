package com.agilecheckup.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeValidationRequest {
  private String email;
  private String assessmentMatrixId;
  private String tenantId;
}