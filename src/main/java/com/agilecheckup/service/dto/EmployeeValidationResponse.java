package com.agilecheckup.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeValidationResponse {
  private String status;
  private String message;
  private String employeeAssessmentId;
  private String name;
  private String assessmentStatus;

  public static EmployeeValidationResponse error(String message) {
    return EmployeeValidationResponse.builder().status("ERROR").message(message).build();
  }

  public static EmployeeValidationResponse success(String message, String employeeAssessmentId, String name, String assessmentStatus) {
    return EmployeeValidationResponse.builder().status("SUCCESS").message(message).employeeAssessmentId(employeeAssessmentId).name(name).assessmentStatus(assessmentStatus).build();
  }

  public static EmployeeValidationResponse info(String message, String employeeAssessmentId, String name, String assessmentStatus) {
    return EmployeeValidationResponse.builder().status("INFO").message(message).employeeAssessmentId(employeeAssessmentId).name(name).assessmentStatus(assessmentStatus).build();
  }
}