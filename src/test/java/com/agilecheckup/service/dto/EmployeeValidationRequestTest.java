package com.agilecheckup.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeValidationRequestTest {

  @Test
  void shouldCreateEmptyRequest() {
    // When
    EmployeeValidationRequest request = new EmployeeValidationRequest();

    // Then
    assertThat(request).isNotNull();
    assertThat(request.getEmail()).isNull();
    assertThat(request.getAssessmentMatrixId()).isNull();
    assertThat(request.getTenantId()).isNull();
  }

  @Test
  void shouldCreateRequestWithAllArgsConstructor() {
    // Given
    String email = "john.doe@example.com";
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-456";

    // When
    EmployeeValidationRequest request = new EmployeeValidationRequest(email, assessmentMatrixId, tenantId);

    // Then
    assertThat(request).isNotNull();
    assertThat(request.getEmail()).isEqualTo(email);
    assertThat(request.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(request.getTenantId()).isEqualTo(tenantId);
  }

  @Test
  void shouldCreateRequestWithBuilder() {
    // Given
    String email = "jane.smith@example.com";
    String assessmentMatrixId = "matrix-789";
    String tenantId = "tenant-012";

    // When
    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
                                                                 .email(email)
                                                                 .assessmentMatrixId(assessmentMatrixId)
                                                                 .tenantId(tenantId)
                                                                 .build();

    // Then
    assertThat(request).isNotNull();
    assertThat(request.getEmail()).isEqualTo(email);
    assertThat(request.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(request.getTenantId()).isEqualTo(tenantId);
  }

  @Test
  void shouldSetFieldsWithSetters() {
    // Given
    EmployeeValidationRequest request = new EmployeeValidationRequest();
    String email = "test@example.com";
    String assessmentMatrixId = "matrix-test";
    String tenantId = "tenant-test";

    // When
    request.setEmail(email);
    request.setAssessmentMatrixId(assessmentMatrixId);
    request.setTenantId(tenantId);

    // Then
    assertThat(request.getEmail()).isEqualTo(email);
    assertThat(request.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(request.getTenantId()).isEqualTo(tenantId);
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    // Given
    EmployeeValidationRequest request1 = EmployeeValidationRequest.builder()
                                                                  .email("test@example.com")
                                                                  .assessmentMatrixId("matrix-123")
                                                                  .tenantId("tenant-456")
                                                                  .build();

    EmployeeValidationRequest request2 = EmployeeValidationRequest.builder()
                                                                  .email("test@example.com")
                                                                  .assessmentMatrixId("matrix-123")
                                                                  .tenantId("tenant-456")
                                                                  .build();

    EmployeeValidationRequest request3 = EmployeeValidationRequest.builder()
                                                                  .email("different@example.com")
                                                                  .assessmentMatrixId("matrix-123")
                                                                  .tenantId("tenant-456")
                                                                  .build();

    // Then
    assertThat(request1).isEqualTo(request2);
    assertThat(request1).isNotEqualTo(request3);
    assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
  }

  @Test
  void shouldImplementToString() {
    // Given
    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
                                                                 .email("test@example.com")
                                                                 .assessmentMatrixId("matrix-123")
                                                                 .tenantId("tenant-456")
                                                                 .build();

    // When
    String toString = request.toString();

    // Then
    assertThat(toString).contains("EmployeeValidationRequest");
    assertThat(toString).contains("test@example.com");
    assertThat(toString).contains("matrix-123");
    assertThat(toString).contains("tenant-456");
  }
}