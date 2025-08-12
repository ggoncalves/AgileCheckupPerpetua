package com.agilecheckup.service.exception;

public class EmployeeAssessmentAlreadyExistsException extends RuntimeException {

  public EmployeeAssessmentAlreadyExistsException(String employeeEmail, String assessmentMatrixId) {
    super("Employee assessment already exists for employee: " + employeeEmail + " and matrix: " + assessmentMatrixId);
  }

  public EmployeeAssessmentAlreadyExistsException(String message) {
    super(message);
  }

  public EmployeeAssessmentAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}