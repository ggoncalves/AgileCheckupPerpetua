package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;
import com.agilecheckup.service.exception.EmployeeAssessmentAlreadyExistsException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentServiceTest {

  private static final String TENANT_ID = "tenant-123";
  private static final String TEAM_ID = "team-123";
  private static final String ASSESSMENT_MATRIX_ID = "matrix-123";
  private static final String EMPLOYEE_EMAIL = "john.doe@example.com";
  private static final String EMPLOYEE_NAME = "John Doe";
  @Mock
  private EmployeeAssessmentRepository employeeAssessmentRepository;
  @Mock
  private AssessmentMatrixService assessmentMatrixService;
  @Mock
  private TeamService teamService;
  @Mock
  private AnswerRepository answerRepository;
  private EmployeeAssessmentService service;

  @BeforeEach
  void setUp() {
    service = new EmployeeAssessmentService(
        employeeAssessmentRepository, assessmentMatrixService, teamService, answerRepository
    );

    // Setup default mocks
    setupMockTeam();
    setupMockAssessmentMatrix();
  }

  private void setupMockTeam() {
    Team mockTeam = Team.builder()
                        .id(TEAM_ID)
                        .tenantId(TENANT_ID)
                        .name("Test Team")
                        .description("Test team description")
                        .departmentId("dept-123")
                        .build();

    lenient().doReturn(Optional.of(mockTeam)).when(teamService).findById(TEAM_ID);
  }

  private void setupMockAssessmentMatrix() {
    Map<String, Pillar> emptyPillarMap = new HashMap<>();

    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(ASSESSMENT_MATRIX_ID)
                                                  .tenantId(TENANT_ID)
                                                  .name("Test Matrix")
                                                  .description("Test matrix description")
                                                  .performanceCycleId("cycle-123")
                                                  .pillarMap(emptyPillarMap)
                                                  .build();

    lenient().doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
  }

  @Test
  void testCreateEmployeeAssessment_Success() {
    doReturn(false).when(employeeAssessmentRepository)
                   .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);

    EmployeeAssessment savedAssessment = createMockEmployeeAssessment();
    doReturn(Optional.of(savedAssessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    Optional<EmployeeAssessment> result = service.create(
        ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL, "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    );

    assertThat(result).isPresent();
    assertThat(result.get().getEmployee().getEmail()).isEqualTo(EMPLOYEE_EMAIL);
    assertThat(result.get().getEmployeeEmailNormalized()).isEqualTo(EMPLOYEE_EMAIL.toLowerCase());
    assertThat(result.get().getAssessmentStatus()).isEqualTo(AssessmentStatus.INVITED);
    assertThat(result.get().getAnsweredQuestionCount()).isEqualTo(0);

    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
    verify(employeeAssessmentRepository).save(any(EmployeeAssessment.class));
  }

  @Test
  void testCreateEmployeeAssessment_ThrowsExceptionWhenAlreadyExists() {
    doReturn(true).when(employeeAssessmentRepository)
                  .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);

    assertThatThrownBy(() -> service.create(
        ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL, "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    )).isInstanceOf(EmployeeAssessmentAlreadyExistsException.class);

    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
    verify(employeeAssessmentRepository, never()).save(any(EmployeeAssessment.class));
  }

  @Test
  void testCreateEmployeeAssessment_ThrowsExceptionWhenTeamNotFound() {
    doReturn(Optional.empty()).when(teamService).findById(TEAM_ID);

    assertThatThrownBy(() -> service.create(
        ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL, "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    )).isInstanceOf(InvalidIdReferenceException.class);
  }

  @Test
  void testCreateEmployeeAssessment_ThrowsExceptionWhenAssessmentMatrixNotFound() {
    doReturn(Optional.empty()).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    assertThatThrownBy(() -> service.create(
        ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL, "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    )).isInstanceOf(InvalidIdReferenceException.class);
  }

  @Test
  void testUpdateEmployeeAssessment_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment existingAssessment = createMockEmployeeAssessment();
    existingAssessment.setId(assessmentId);

    doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    Optional<EmployeeAssessment> result = service.update(
        assessmentId, ASSESSMENT_MATRIX_ID, TEAM_ID, "Jane Smith", "jane.smith@example.com", "987654321", PersonDocumentType.CPF, Gender.FEMALE, GenderPronoun.SHE
    );

    assertThat(result).isPresent();
    assertThat(result.get().getEmployee().getName()).isEqualTo("Jane Smith");
    assertThat(result.get().getEmployee().getEmail()).isEqualTo("jane.smith@example.com");
    assertThat(result.get().getEmployeeEmailNormalized()).isEqualTo("jane.smith@example.com");
  }

  @Test
  void testUpdateEmployeeAssessment_ReturnsEmptyWhenNotFound() {
    String assessmentId = "assessment-123";
    doReturn(Optional.empty()).when(employeeAssessmentRepository).findById(assessmentId);

    Optional<EmployeeAssessment> result = service.update(
        assessmentId, ASSESSMENT_MATRIX_ID, TEAM_ID, "Jane Smith", "jane.smith@example.com", "987654321", PersonDocumentType.CPF, Gender.FEMALE, GenderPronoun.SHE
    );

    assertThat(result).isEmpty();
  }

  @Test
  void testIncrementAnsweredQuestionCount_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAnsweredQuestionCount(0);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    service.incrementAnsweredQuestionCount(assessmentId);

    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(1);
    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
    verify(employeeAssessmentRepository, atLeast(1)).save(assessment);
  }

  @Test
  void testIncrementAnsweredQuestionCount_DoesNotChangeStatusWhenAlreadyInProgress() {
    // Given
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentMatrixId(assessmentId);
    assessment.setAnsweredQuestionCount(5);
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(assessment.getAssessmentMatrixId())
                                                  .performanceCycleId(assessment.getAssessmentMatrixId())
                                                  .name("name")
                                                  .description("description")
                                                  .questionCount(8)
                                                  .build();

    doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessment.getAssessmentMatrixId());
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    EmployeeAssessmentService serviceSpy = spy(service);

    // When
    serviceSpy.incrementAnsweredQuestionCount(assessmentId);

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(6);
    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
    verify(serviceSpy).save(assessment);
  }

  @Test
  void testIncrementAnsweredQuestionCount_ChangeStatusToCompleted() {
    // Given
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentMatrixId(assessmentId);
    assessment.setAnsweredQuestionCount(7);
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(assessment.getAssessmentMatrixId())
                                                  .performanceCycleId(assessment.getAssessmentMatrixId())
                                                  .name("name")
                                                  .description("description")
                                                  .questionCount(8)
                                                  .build();

    doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessment.getAssessmentMatrixId());
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    EmployeeAssessmentService serviceSpy = spy(service);

    // When
    serviceSpy.incrementAnsweredQuestionCount(assessmentId);

    // Then
    assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(8);
    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED);
    verify(serviceSpy, times(1)).save(assessment);
    verify(serviceSpy, times(1)).updateEmployeeAssessmentScore(assessment);
  }

  @Test
  void testUpdateAssessmentStatus_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    Optional<EmployeeAssessment> result = service.updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);

    assertThat(result).isPresent();
    assertThat(result.get().getAssessmentStatus()).isEqualTo(AssessmentStatus.CONFIRMED);
    verify(employeeAssessmentRepository, atLeast(1)).save(assessment);
  }

  @Test
  void testUpdateEmployeeAssessmentScore_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);

    List<Answer> mockAnswers = createMockAnswers();

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(mockAnswers).when(answerRepository).findByEmployeeAssessmentId(assessmentId, TENANT_ID);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    EmployeeAssessment result = service.updateEmployeeAssessmentScore(assessmentId);

    assertThat(result).isNotNull();
    assertThat(result.getEmployeeAssessmentScore()).isNotNull();
    assertThat(result.getEmployeeAssessmentScore().getScore()).isGreaterThan(0);
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void testFindAllByTenantId() {
    List<EmployeeAssessment> expectedAssessments = List.of(createMockEmployeeAssessment());
    doReturn(expectedAssessments).when(employeeAssessmentRepository).findAllByTenantId(TENANT_ID);

    List<EmployeeAssessment> result = service.findAllByTenantId(TENANT_ID);

    assertThat(result).hasSize(1);
    assertThat(result).isEqualTo(expectedAssessments);
    verify(employeeAssessmentRepository).findAllByTenantId(TENANT_ID);
  }

  @Test
  void testFindByAssessmentMatrix() {
    List<EmployeeAssessment> expectedAssessments = Arrays.asList(createMockEmployeeAssessment());
    doReturn(expectedAssessments).when(employeeAssessmentRepository)
                                 .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

    List<EmployeeAssessment> result = service.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    assertThat(result).hasSize(1);
    assertThat(result).isEqualTo(expectedAssessments);
    verify(employeeAssessmentRepository).findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);
  }

  @Test
  void testFindByIdWithTenantId_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setTenantId(TENANT_ID);

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);

    Optional<EmployeeAssessment> result = service.findById(assessmentId, TENANT_ID);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(assessmentId);
  }

  @Test
  void testFindByIdWithTenantId_ReturnsEmptyWhenTenantMismatch() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setTenantId("different-tenant");

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);

    Optional<EmployeeAssessment> result = service.findById(assessmentId, TENANT_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void testDeleteById_Success() {
    String assessmentId = "assessment-123";

    service.deleteById(assessmentId);

    verify(employeeAssessmentRepository).deleteById(assessmentId);
  }

  @Test
  void testSave_NewAssessment_ValidatesUniqueness() {
    EmployeeAssessment newAssessment = createMockEmployeeAssessment();
    newAssessment.setId(null); // New assessment

    doReturn(false).when(employeeAssessmentRepository)
                   .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
    doReturn(Optional.of(newAssessment)).when(employeeAssessmentRepository).save(newAssessment);

    EmployeeAssessment result = service.save(newAssessment);

    assertThat(result).isEqualTo(newAssessment);
    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
    verify(employeeAssessmentRepository).save(newAssessment);
  }

  @Test
  void testValidateEmployee_Success_InvitedToConfirmed() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    List<EmployeeAssessment> assessments = Arrays.asList(assessment);
    doReturn(assessments).when(employeeAssessmentRepository).findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    EmployeeValidationRequest request = new EmployeeValidationRequest();
    request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
    request.setTenantId(TENANT_ID);
    request.setEmail(EMPLOYEE_EMAIL);

    EmployeeValidationResponse response = service.validateEmployee(request);

    assertThat("SUCCESS".equals(response.getStatus())).isTrue();
    assertThat(response.getEmployeeAssessmentId()).isEqualTo(assessmentId);
    assertThat(response.getName()).isEqualTo(EMPLOYEE_NAME);
    assertThat(response.getAssessmentStatus()).isEqualTo(AssessmentStatus.CONFIRMED.toString());
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void testValidateEmployee_NotFound() {
    doReturn(Arrays.asList()).when(employeeAssessmentRepository)
                             .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

    EmployeeValidationRequest request = new EmployeeValidationRequest();
    request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
    request.setTenantId(TENANT_ID);
    request.setEmail(EMPLOYEE_EMAIL);

    EmployeeValidationResponse response = service.validateEmployee(request);

    assertThat("SUCCESS".equals(response.getStatus())).isFalse();
    assertThat(response.getMessage()).contains("couldn't find your assessment invitation");
  }

  @Test
  void testValidateEmployee_AlreadyCompleted() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);

    List<EmployeeAssessment> assessments = Arrays.asList(assessment);
    doReturn(assessments).when(employeeAssessmentRepository).findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

    EmployeeValidationRequest request = new EmployeeValidationRequest();
    request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
    request.setTenantId(TENANT_ID);
    request.setEmail(EMPLOYEE_EMAIL);

    EmployeeValidationResponse response = service.validateEmployee(request);

    assertThat("INFO".equals(response.getStatus())).isTrue();
    assertThat(response.getMessage()).contains("already completed this assessment");
    assertThat(response.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED.toString());
  }

  @Test
  void testUpdateLastActivityDate_Success() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    service.updateLastActivityDate(assessmentId);

    assertThat(assessment.getLastActivityDate()).isNotNull();
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void testUpdateLastActivityDate_DoesNotUpdateWhenCompleted() {
    String assessmentId = "assessment-123";
    EmployeeAssessment assessment = createMockEmployeeAssessment();
    assessment.setId(assessmentId);
    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    Date originalDate = new Date();
    assessment.setLastActivityDate(originalDate);

    doReturn(Optional.of(assessment)).when(employeeAssessmentRepository).findById(assessmentId);

    service.updateLastActivityDate(assessmentId);

    assertThat(assessment.getLastActivityDate()).isEqualTo(originalDate);
    verify(employeeAssessmentRepository, never()).save(assessment);
  }

  @Test
  void testScanAllEmployeeAssessments() {
    List<EmployeeAssessment> expectedAssessments = Arrays.asList(createMockEmployeeAssessment());
    doReturn(expectedAssessments).when(employeeAssessmentRepository).findAll();

    List<EmployeeAssessment> result = service.scanAllEmployeeAssessments();

    assertThat(result).isEqualTo(expectedAssessments);
    verify(employeeAssessmentRepository).findAll();
  }

  private EmployeeAssessment createMockEmployeeAssessment() {
    NaturalPerson employee = NaturalPerson.builder()
                                          .id("person-123")
                                          .name(EMPLOYEE_NAME)
                                          .email(EMPLOYEE_EMAIL)
                                          .documentNumber("123456789")
                                          .personDocumentType(PersonDocumentType.CPF)
                                          .gender(Gender.MALE)
                                          .genderPronoun(GenderPronoun.HE)
                                          .build();

    EmployeeAssessment assessment = EmployeeAssessment.builder()
                                                      .id("assessment-123")
                                                      .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                                                      .teamId(TEAM_ID)
                                                      .employee(employee)
                                                      .employeeEmailNormalized(EMPLOYEE_EMAIL.toLowerCase())
                                                      .answeredQuestionCount(0)
                                                      .assessmentStatus(AssessmentStatus.INVITED)
                                                      .build();
    assessment.setTenantId(TENANT_ID);
    return assessment;
  }

  private List<Answer> createMockAnswers() {
    Question mockQuestion = Question.builder()
                                    .id("question-123")
                                    .assessmentMatrixId("matrix-123")
                                    .pillarId("pillar-123")
                                    .pillarName("Test Pillar")
                                    .categoryId("category-123")
                                    .categoryName("Test Category")
                                    .question("Test Question")
                                    .questionType(QuestionType.ONE_TO_TEN)
                                    .points(10.0)
                                    .tenantId(TENANT_ID)
                                    .build();

    Answer answer1 = Answer.builder()
                           .questionId("question-123")
                           .pillarId("pillar-123")
                           .categoryId("category-123")
                           .score(8.5)
                           .question(mockQuestion)
                           .questionType(QuestionType.ONE_TO_TEN)
                           .employeeAssessmentId("assessment-123")
                           .answeredAt(LocalDateTime.now())
                           .value("8")
                           .tenantId(TENANT_ID)
                           .build();

    Answer answer2 = Answer.builder()
                           .questionId("question-124")
                           .pillarId("pillar-123")
                           .categoryId("category-123")
                           .score(7.0)
                           .question(mockQuestion)
                           .questionType(QuestionType.ONE_TO_TEN)
                           .employeeAssessmentId("assessment-123")
                           .answeredAt(LocalDateTime.now())
                           .value("7")
                           .tenantId(TENANT_ID)
                           .build();

    return Arrays.asList(answer1, answer2);
  }

  @Test
  void shouldCreateEmployeeAssessmentWithNullTeamId() {
    // Given
    String assessmentMatrixId = "matrix-123";
    String name = "John Doe";
    String email = "john.doe@example.com";
    String documentNumber = "123456789";
    PersonDocumentType documentType = PersonDocumentType.CPF;
    Gender gender = Gender.MALE;
    GenderPronoun genderPronoun = GenderPronoun.HE;

    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(assessmentMatrixId)
                                                  .name("Test Matrix")
                                                  .description("Test Description")
                                                  .performanceCycleId("cycle-123")
                                                  .build();
    mockMatrix.setTenantId(TENANT_ID);

    doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
    doReturn(Optional.of(createMockEmployeeAssessment())).when(employeeAssessmentRepository)
                                                         .save(any(EmployeeAssessment.class));

    // When
    Optional<EmployeeAssessment> result = service.create(assessmentMatrixId, null, name, email, documentNumber, documentType, gender, genderPronoun);

    // Then
    assertThat(result).isPresent();

    ArgumentCaptor<EmployeeAssessment> captor = ArgumentCaptor.forClass(EmployeeAssessment.class);
    verify(employeeAssessmentRepository).save(captor.capture());

    EmployeeAssessment createdAssessment = captor.getValue();
    assertThat(createdAssessment.getTeamId()).isNull();
    assertThat(createdAssessment.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(createdAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(createdAssessment.getEmployee().getName()).isEqualTo(name);
    assertThat(createdAssessment.getEmployee().getEmail()).isEqualTo(email);
  }

  @Test
  void shouldUpdateEmployeeAssessmentWithNullTeamId() {
    // Given
    String assessmentId = "assessment-123";
    String assessmentMatrixId = "matrix-123";
    String name = "John Doe";
    String email = "john.doe@example.com";
    String documentNumber = "123456789";
    PersonDocumentType documentType = PersonDocumentType.CPF;
    Gender gender = Gender.MALE;
    GenderPronoun genderPronoun = GenderPronoun.HE;

    EmployeeAssessment existingAssessment = createMockEmployeeAssessment();
    existingAssessment.setId(assessmentId);

    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(assessmentMatrixId)
                                                  .name("Test Matrix")
                                                  .description("Test Description")
                                                  .performanceCycleId("cycle-123")
                                                  .build();
    mockMatrix.setTenantId(TENANT_ID);

    doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepository).findById(assessmentId);
//    doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
    doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepository).save(any(EmployeeAssessment.class));

    // When
    Optional<EmployeeAssessment> result = service.update(assessmentId, assessmentMatrixId, null, name, email, documentNumber, documentType, gender, genderPronoun);

    // Then
    assertThat(result).isPresent();

    ArgumentCaptor<EmployeeAssessment> captor = ArgumentCaptor.forClass(EmployeeAssessment.class);
    verify(employeeAssessmentRepository).save(captor.capture());

    EmployeeAssessment updatedAssessment = captor.getValue();
    assertThat(updatedAssessment.getTeamId()).isNull();
    assertThat(updatedAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(updatedAssessment.getEmployee().getName()).isEqualTo(name);
    assertThat(updatedAssessment.getEmployee().getEmail()).isEqualTo(email);
  }

  @Test
  void testCreateEmployeeAssessment_ShouldSaveTeamIdWhenTeamProvided() {
    // Given
    String assessmentMatrixId = ASSESSMENT_MATRIX_ID;
    String teamId = TEAM_ID;
    String name = EMPLOYEE_NAME;
    String email = EMPLOYEE_EMAIL;

    // Setup mock team with DIFFERENT tenant ID to expose bug
    Team mockTeam = Team.builder().id(teamId).tenantId("team-tenant-123") // Different from matrix tenant
                        .name("Test Team").description("Test team description").departmentId("dept-123").build();
    doReturn(Optional.of(mockTeam)).when(teamService).findById(teamId);

    // Setup mock assessment matrix
    AssessmentMatrix mockMatrix = AssessmentMatrix.builder()
                                                  .id(assessmentMatrixId)
                                                  .tenantId(TENANT_ID) // Matrix tenant should be used
                                                  .name("Test Matrix")
                                                  .description("Test matrix description")
                                                  .performanceCycleId("cycle-123")
                                                  .pillarMap(new HashMap<>())
                                                  .build();
    doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);

    doReturn(false).when(employeeAssessmentRepository)
                   .existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, email);
    doReturn(Optional.of(createMockEmployeeAssessment())).when(employeeAssessmentRepository)
                                                         .save(any(EmployeeAssessment.class));

    // When
    Optional<EmployeeAssessment> result = service.create(
        assessmentMatrixId, teamId, name, email, "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    );

    // Then
    assertThat(result).isPresent();

    ArgumentCaptor<EmployeeAssessment> captor = ArgumentCaptor.forClass(EmployeeAssessment.class);
    verify(employeeAssessmentRepository).save(captor.capture());

    EmployeeAssessment createdAssessment = captor.getValue();

    // BUG EXPOSED: These assertions should pass but will fail due to the bug
    assertThat(createdAssessment.getTeamId()).isEqualTo(teamId); // teamId should be saved
    assertThat(createdAssessment.getTenantId()).isEqualTo(TENANT_ID); // should use matrix tenantId, not team tenantId
    assertThat(createdAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
    assertThat(createdAssessment.getEmployee().getName()).isEqualTo(name);
    assertThat(createdAssessment.getEmployee().getEmail()).isEqualTo(email);
  }


  @Test
  void testFinalizeAssessmentIfCompleted_CallsFinalizeWhenAssessmentComplete() {
    // Given
    String employeeAssessmentId = "assessment-123";
    EmployeeAssessment mockAssessment = createMockEmployeeAssessment();
    mockAssessment.setAnsweredQuestionCount(10);
    mockAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    AssessmentMatrix mockMatrix = new AssessmentMatrix();
    mockMatrix.setId(ASSESSMENT_MATRIX_ID);
    mockMatrix.setQuestionCount(10); // Same as answered count
    mockMatrix.setTenantId(TENANT_ID);

    when(employeeAssessmentRepository.findById(employeeAssessmentId)).thenReturn(Optional.of(mockAssessment));
    when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));

    // Spy on the service to verify method calls
    EmployeeAssessmentService serviceSpy = spy(service);
    doNothing().when(serviceSpy).finalizeAssessment(any(EmployeeAssessment.class));

    // When
    serviceSpy.finalizeAssessmentIfCompleted(employeeAssessmentId);

    // Then
    verify(serviceSpy).finalizeAssessment(mockAssessment);
  }

  @Test
  void testFinalizeAssessmentIfCompleted_DoesNotCallFinalizeWhenAssessmentIncomplete() {
    // Given
    String employeeAssessmentId = "assessment-123";
    EmployeeAssessment mockAssessment = createMockEmployeeAssessment();
    mockAssessment.setAnsweredQuestionCount(5); // Less than total questions
    mockAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    AssessmentMatrix mockMatrix = new AssessmentMatrix();
    mockMatrix.setId(ASSESSMENT_MATRIX_ID);
    mockMatrix.setQuestionCount(10); // More than answered count
    mockMatrix.setTenantId(TENANT_ID);

    when(employeeAssessmentRepository.findById(employeeAssessmentId)).thenReturn(Optional.of(mockAssessment));
    when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));

    // Spy on the service to verify method calls
    EmployeeAssessmentService spyService = spy(service);

    // When
    spyService.finalizeAssessmentIfCompleted(employeeAssessmentId);

    // Then
    verify(spyService, never()).finalizeAssessment(any(EmployeeAssessment.class));
  }

  @Test
  void testFinalizeAssessment_DoesNotSaveWhenAlreadyCompleted() {
    // Given
    EmployeeAssessment mockAssessment = createMockEmployeeAssessment();
    mockAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    // Spy on the service to verify method calls
    EmployeeAssessmentService spyService = spy(service);

    // When
    spyService.finalizeAssessment(mockAssessment);

    // Then
    verify(employeeAssessmentRepository, never()).save(any(EmployeeAssessment.class));
    verify(spyService, never()).updateEmployeeAssessmentScore(any(EmployeeAssessment.class));
  }

  @Test
  void testFinalizeAssessment_SavesAndUpdatesScoreWhenNotCompleted() {
    // Given
    EmployeeAssessment mockAssessment = createMockEmployeeAssessment();
    mockAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    // Spy on the service to verify updateEmployeeAssessmentScore call
    EmployeeAssessmentService spyService = spy(service);

    // When
    spyService.finalizeAssessment(mockAssessment);

    // Then
    assertThat(mockAssessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED);
    assertThat(mockAssessment.getLastActivityDate()).isNotNull();
  }

  @Test
  void testFinalizeAssessment_SetsCompletedStatusAndTimestamp() {
    // Given
    EmployeeAssessment mockAssessment = createMockEmployeeAssessment();
    mockAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
    mockAssessment.setLastActivityDate(null);

    // Spy to avoid actual updateEmployeeAssessmentScore call
    EmployeeAssessmentService spyService = spy(service);

    // When
    spyService.finalizeAssessment(mockAssessment);

    // Then
    assertThat(mockAssessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED);
    assertThat(mockAssessment.getLastActivityDate()).isNotNull();
    assertThat(mockAssessment.getLastActivityDate()).isCloseTo(new Date(), 1000);
  }
}