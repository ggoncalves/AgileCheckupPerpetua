package com.agilecheckup.service.exception;

/**
 * Exception thrown when attempting to create an employee assessment 
 * that already exists for the same assessment matrix (based on employee email).
 */
public class EmployeeAssessmentAlreadyExistsException extends ServiceException {
  
  private final String employeeEmail;
  private final String assessmentMatrixId;
  
  public EmployeeAssessmentAlreadyExistsException(String employeeEmail, String assessmentMatrixId) {
    super(String.format("Employee assessment for '%s' already exists in assessment matrix '%s'", employeeEmail, assessmentMatrixId));
    this.employeeEmail = employeeEmail;
    this.assessmentMatrixId = assessmentMatrixId;
  }
  
  public String getEmployeeEmail() {
    return employeeEmail;
  }
  
  public String getAssessmentMatrixId() {
    return assessmentMatrixId;
  }
}