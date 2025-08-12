package com.agilecheckup.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeValidationResponseTest {

  @Test
  void shouldCreateEmptyResponse() {
    // When
    EmployeeValidationResponse response = new EmployeeValidationResponse();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getEmployeeAssessmentId()).isNull();
    assertThat(response.getName()).isNull();
    assertThat(response.getAssessmentStatus()).isNull();
  }

  @Test
  void shouldCreateResponseWithAllArgsConstructor() {
    // Given
    String status = "SUCCESS";
    String message = "Welcome!";
    String employeeAssessmentId = "ea-123";
    String name = "John Doe";
    String assessmentStatus = "CONFIRMED";

    // When
    EmployeeValidationResponse response = new EmployeeValidationResponse(
        status, message, employeeAssessmentId, name, assessmentStatus);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(status);
    assertThat(response.getMessage()).isEqualTo(message);
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
    assertThat(response.getName()).isEqualTo(name);
    assertThat(response.getAssessmentStatus()).isEqualTo(assessmentStatus);
  }

  @Test
  void shouldCreateResponseWithBuilder() {
    // Given
    String status = "INFO";
    String message = "Welcome back!";
    String employeeAssessmentId = "ea-456";
    String name = "Jane Smith";
    String assessmentStatus = "IN_PROGRESS";

    // When
    EmployeeValidationResponse response = EmployeeValidationResponse.builder().status(status).message(message).employeeAssessmentId(employeeAssessmentId).name(name).assessmentStatus(assessmentStatus).build();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(status);
    assertThat(response.getMessage()).isEqualTo(message);
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
    assertThat(response.getName()).isEqualTo(name);
    assertThat(response.getAssessmentStatus()).isEqualTo(assessmentStatus);
  }

  @Test
  void shouldCreateErrorResponseWithStaticMethod() {
    // Given
    String errorMessage = "Employee not found";

    // When
    EmployeeValidationResponse response = EmployeeValidationResponse.error(errorMessage);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo("ERROR");
    assertThat(response.getMessage()).isEqualTo(errorMessage);
    assertThat(response.getEmployeeAssessmentId()).isNull();
    assertThat(response.getName()).isNull();
    assertThat(response.getAssessmentStatus()).isNull();
  }

  @Test
  void shouldCreateSuccessResponseWithStaticMethod() {
    // Given
    String message = "Welcome! Your assessment access has been confirmed.";
    String employeeAssessmentId = "ea-789";
    String name = "Alice Johnson";
    String assessmentStatus = "CONFIRMED";

    // When
    EmployeeValidationResponse response = EmployeeValidationResponse.success(
        message, employeeAssessmentId, name, assessmentStatus);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getMessage()).isEqualTo(message);
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
    assertThat(response.getName()).isEqualTo(name);
    assertThat(response.getAssessmentStatus()).isEqualTo(assessmentStatus);
  }

  @Test
  void shouldCreateInfoResponseWithStaticMethod() {
    // Given
    String message = "Welcome back! You can continue your assessment.";
    String employeeAssessmentId = "ea-012";
    String name = "Bob Wilson";
    String assessmentStatus = "IN_PROGRESS";

    // When
    EmployeeValidationResponse response = EmployeeValidationResponse.info(
        message, employeeAssessmentId, name, assessmentStatus);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo("INFO");
    assertThat(response.getMessage()).isEqualTo(message);
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
    assertThat(response.getName()).isEqualTo(name);
    assertThat(response.getAssessmentStatus()).isEqualTo(assessmentStatus);
  }

  @Test
  void shouldSetFieldsWithSetters() {
    // Given
    EmployeeValidationResponse response = new EmployeeValidationResponse();
    String status = "SUCCESS";
    String message = "Test message";
    String employeeAssessmentId = "ea-test";
    String name = "Test User";
    String assessmentStatus = "CONFIRMED";

    // When
    response.setStatus(status);
    response.setMessage(message);
    response.setEmployeeAssessmentId(employeeAssessmentId);
    response.setName(name);
    response.setAssessmentStatus(assessmentStatus);

    // Then
    assertThat(response.getStatus()).isEqualTo(status);
    assertThat(response.getMessage()).isEqualTo(message);
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
    assertThat(response.getName()).isEqualTo(name);
    assertThat(response.getAssessmentStatus()).isEqualTo(assessmentStatus);
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    // Given
    EmployeeValidationResponse response1 = EmployeeValidationResponse.success(
        "Welcome!", "ea-123", "John Doe", "CONFIRMED");

    EmployeeValidationResponse response2 = EmployeeValidationResponse.success(
        "Welcome!", "ea-123", "John Doe", "CONFIRMED");

    EmployeeValidationResponse response3 = EmployeeValidationResponse.error("Not found");

    // Then
    assertThat(response1).isEqualTo(response2);
    assertThat(response1).isNotEqualTo(response3);
    assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
  }

  @Test
  void shouldImplementToString() {
    // Given
    EmployeeValidationResponse response = EmployeeValidationResponse.success(
        "Welcome!", "ea-123", "John Doe", "CONFIRMED");

    // When
    String toString = response.toString();

    // Then
    assertThat(toString).contains("EmployeeValidationResponse");
    assertThat(toString).contains("SUCCESS");
    assertThat(toString).contains("Welcome!");
    assertThat(toString).contains("ea-123");
    assertThat(toString).contains("John Doe");
    assertThat(toString).contains("CONFIRMED");
  }
}