package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;

class EmployeeAssessmentTest {

  @Test
  void testEmployeeAssessmentConstruction() {
    NaturalPerson employee = createCompleteTestEmployee();
    EmployeeAssessmentScore score = EmployeeAssessmentScore.builder()
                                                           .score(85.5)
                                                           .build();
    Date lastActivity = new Date();

    EmployeeAssessment assessment = EmployeeAssessment.builder()
                                                      .id("assessment-123")
                                                      .assessmentMatrixId("matrix-123")
                                                      .employee(employee)
                                                      .teamId("team-123")
                                                      .employeeAssessmentScore(score)
                                                      .assessmentStatus(AssessmentStatus.IN_PROGRESS)
                                                      .answeredQuestionCount(5)
                                                      .employeeEmailNormalized("john.doe@example.com")
                                                      .lastActivityDate(lastActivity)
                                                      .build();
    assessment.setTenantId("tenant-123");

    assertThat(assessment.getId()).isEqualTo("assessment-123");
    assertThat(assessment.getTenantId()).isEqualTo("tenant-123");
    assertThat(assessment.getAssessmentMatrixId()).isEqualTo("matrix-123");
    assertThat(assessment.getEmployee()).isEqualTo(employee);
    assertThat(assessment.getTeamId()).isEqualTo("team-123");
    assertThat(assessment.getEmployeeAssessmentScore()).isEqualTo(score);
    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(5);
    assertThat(assessment.getEmployeeEmailNormalized()).isEqualTo("john.doe@example.com");
    assertThat(assessment.getLastActivityDate()).isEqualTo(lastActivity);
  }

  @Test
  void testSetEmployeeUpdatesNormalizedEmail() {
    NaturalPerson initialEmployee = createBasicTestEmployee("person-initial", "Initial Name", "initial@example.com");
    EmployeeAssessment assessment = createTestEmployeeAssessmentWithEmployee(initialEmployee);

    NaturalPerson employee = createBasicTestEmployee("person-123", "Jane Smith", "JANE.SMITH@EXAMPLE.COM");
    assessment.setEmployee(employee);

    assertThat(assessment.getEmployee()).isEqualTo(employee);
    assertThat(assessment.getEmployeeEmailNormalized()).isEqualTo("jane.smith@example.com");
  }

  @Test
  void testSetEmployeeWithBlankEmailSetsNormalizedEmailToNull() {
    NaturalPerson initialEmployee = createBasicTestEmployee("person-initial", "Initial Name", "initial@example.com");
    EmployeeAssessment assessment = createTestEmployeeAssessmentWithEmployee(initialEmployee);

    NaturalPerson employee = createBasicTestEmployee("person-123", "Jane Smith", "   ");
    assessment.setEmployee(employee);

    assertThat(assessment.getEmployee()).isEqualTo(employee);
    assertThat(assessment.getEmployeeEmailNormalized()).isNull();
  }

  @Test
  void testDefaultAnsweredQuestionCountIsZero() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(0);
  }

  @Test
  void testDefaultAssessmentStatusIsInvited() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.INVITED);
  }

  @Test
  @DisplayName("Should increment answered question count from zero to one")
  void shouldIncrementAnsweredQuestionCountFromZeroToOne() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(0);

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should increment answered question count from existing value")
  void shouldIncrementAnsweredQuestionCountFromExistingValue() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(5);

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(6);
  }

  @Test
  @DisplayName("Should increment answered question count when null")
  void shouldIncrementAnsweredQuestionCountWhenNull() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(null);

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should increment answered question count multiple times correctly")
  void shouldIncrementAnsweredQuestionCountMultipleTimesCorrectly() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(0);

    // When
    assessment.incrementAnswersCount();
    assessment.incrementAnswersCount();
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should handle large answered question count values")
  void shouldHandleLargeAnsweredQuestionCountValues() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(Integer.MAX_VALUE - 1);

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  @DisplayName("Should increment from zero when count is explicitly zero")
  void shouldIncrementFromZeroWhenCountIsExplicitlyZero() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(0);

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should preserve other field values when incrementing count")
  void shouldPreserveOtherFieldValuesWhenIncrementingCount() {
    // Given
    EmployeeAssessment assessment = createTestEmployeeAssessment();
    assessment.setAnsweredQuestionCount(2);
    String originalAssessmentMatrixId = assessment.getAssessmentMatrixId();
    AssessmentStatus originalStatus = assessment.getAssessmentStatus();
    String originalTenantId = assessment.getTenantId();

    // When
    assessment.incrementAnswersCount();

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(3);
    assertThat(assessment.getAssessmentMatrixId()).isEqualTo(originalAssessmentMatrixId);
    assertThat(assessment.getAssessmentStatus()).isEqualTo(originalStatus);
    assertThat(assessment.getTenantId()).isEqualTo(originalTenantId);
  }


  @Test
  void testIsCompletedIsFalseByDefault() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assertThat(assessment.isCompleted()).isFalse();
  }

  @Test
  void testIsCompletedIsTrueWhenStatusIsCompleted() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);

    assertThat(assessment.isCompleted()).isTrue();
  }

  @Test
  void testIsNotCompletedIsFalseWhenStatusIsCompleted() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);

    assertThat(assessment.isNotCompleted()).isFalse();
  }

  @Test
  void testIsNotCompletedIsTrueWhenStatusIsNotCompleted() {
    EmployeeAssessment assessment = createTestEmployeeAssessment();

    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    assertThat(assessment.isNotCompleted()).isTrue();
  }

  private EmployeeAssessment createTestEmployeeAssessment() {
    NaturalPerson employee = createBasicTestEmployee();
    return createTestEmployeeAssessmentWithEmployee(employee);
  }

  private EmployeeAssessment createTestEmployeeAssessmentWithEmployee(NaturalPerson employee) {
    return EmployeeAssessment.builder()
                             .id("assessment-123")
                             .tenantId("tenant-123")
                             .assessmentMatrixId("matrix-123")
                             .teamId("team-123")
                             .employee(employee)
                             .build();
  }

  private NaturalPerson createBasicTestEmployee() {
    return createBasicTestEmployee("person-123", "Test Employee", "test@example.com");
  }

  private NaturalPerson createBasicTestEmployee(String id, String name, String email) {
    return NaturalPerson.builder()
                        .id(id)
                        .name(name)
                        .email(email)
                        .build();
  }

  private NaturalPerson createCompleteTestEmployee() {
    return NaturalPerson.builder()
                        .id("person-123")
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .documentNumber("123456789")
                        .personDocumentType(PersonDocumentType.CPF)
                        .gender(Gender.MALE)
                        .genderPronoun(GenderPronoun.HE)
                        .build();
  }
}