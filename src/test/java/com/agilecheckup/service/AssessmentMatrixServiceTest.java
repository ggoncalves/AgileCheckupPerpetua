package com.agilecheckup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import dagger.Lazy;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixServiceTest {

  @Mock
  private AssessmentMatrixRepository assessmentMatrixRepository;

  @Mock
  private PerformanceCycleService performanceCycleService;

  @Mock
  private Lazy<QuestionService> questionService;

  @Mock
  private Lazy<EmployeeAssessmentService> employeeAssessmentService;

  @Mock
  private Lazy<TeamService> teamService;

  @Mock
  private QuestionService mockQuestionService;

  @Mock
  private EmployeeAssessmentService mockEmployeeAssessmentService;

  @Mock
  private TeamService mockTeamService;

  private AssessmentMatrixService service;

  @BeforeEach
  void setUp() {
    // Set up lazy mocks
    lenient().doReturn(mockQuestionService).when(questionService).get();
    lenient().doReturn(mockEmployeeAssessmentService).when(employeeAssessmentService).get();
    lenient().doReturn(mockTeamService).when(teamService).get();

    service = new AssessmentMatrixService(
                                          assessmentMatrixRepository, performanceCycleService, questionService, employeeAssessmentService, teamService
    );
  }

  @Test
  void testCreate() {
    String name = "Test Matrix";
    String description = "Test Description";
    String tenantId = "tenant-123";
    String performanceCycleId = "cycle-123";
    Map<String, Pillar> pillarMap = new HashMap<>();

    PerformanceCycle mockCycle = PerformanceCycle.builder()
                                                 .id(performanceCycleId)
                                                 .name("Test Cycle")
                                                 .description("Test Cycle Description")
                                                 .tenantId(tenantId)
                                                 .companyId("company-123")
                                                 .isActive(true)
                                                 .isTimeSensitive(true)
                                                 .build();

    AssessmentMatrix savedMatrix = AssessmentMatrix.builder()
                                                   .id("matrix-123")
                                                   .name(name)
                                                   .description(description)
                                                   .tenantId(tenantId)
                                                   .performanceCycleId(performanceCycleId)
                                                   .pillarMap(pillarMap)
                                                   .questionCount(0)
                                                   .build();

    lenient().doReturn(Optional.of(mockCycle)).when(performanceCycleService).findById(performanceCycleId);
    doReturn(Optional.of(savedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    Optional<AssessmentMatrix> result = service.create(name, description, tenantId, performanceCycleId, pillarMap);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(name);
    assertThat(result.get().getDescription()).isEqualTo(description);
    assertThat(result.get().getTenantId()).isEqualTo(tenantId);
    assertThat(result.get().getPerformanceCycleId()).isEqualTo(performanceCycleId);
    assertThat(result.get().getPillarMap()).isEqualTo(pillarMap);
    verify(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  @Test
  void testCreateWithConfiguration() {
    String name = "Test Matrix";
    String description = "Test Description";
    String tenantId = "tenant-123";
    String performanceCycleId = "cycle-123";
    Map<String, Pillar> pillarMap = new HashMap<>();
    AssessmentConfiguration configuration = AssessmentConfiguration.builder()
                                                                   .allowQuestionReview(false)
                                                                   .requireAllQuestions(true)
                                                                   .build();

    PerformanceCycle mockCycle = PerformanceCycle.builder()
                                                 .id(performanceCycleId)
                                                 .name("Test Cycle")
                                                 .description("Test Cycle Description")
                                                 .tenantId(tenantId)
                                                 .companyId("company-123")
                                                 .isActive(true)
                                                 .isTimeSensitive(true)
                                                 .build();

    AssessmentMatrix savedMatrix = AssessmentMatrix.builder()
                                                   .id("matrix-123")
                                                   .name(name)
                                                   .description(description)
                                                   .tenantId(tenantId)
                                                   .performanceCycleId(performanceCycleId)
                                                   .pillarMap(pillarMap)
                                                   .configuration(configuration)
                                                   .questionCount(0)
                                                   .build();

    lenient().doReturn(Optional.of(mockCycle)).when(performanceCycleService).findById(performanceCycleId);
    doReturn(Optional.of(savedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    Optional<AssessmentMatrix> result = service.create(name, description, tenantId, performanceCycleId, pillarMap, configuration);

    assertThat(result).isPresent();
    assertThat(result.get().getConfiguration()).isEqualTo(configuration);
    verify(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  @Test
  void testFindAllByTenantId() {
    String tenantId = "tenant-123";
    List<AssessmentMatrix> expectedList = List.of(
                                                  AssessmentMatrix.builder()
                                                                  .id("matrix-1")
                                                                  .name("Matrix 1")
                                                                  .description("Matrix 1 Description")
                                                                  .tenantId(tenantId)
                                                                  .performanceCycleId("cycle-1")
                                                                  .build(), AssessmentMatrix.builder()
                                                                                            .id("matrix-2")
                                                                                            .name("Matrix 2")
                                                                                            .description("Matrix 2 Description")
                                                                                            .tenantId(tenantId)
                                                                                            .performanceCycleId("cycle-2")
                                                                                            .build()
    );

    doReturn(expectedList).when(assessmentMatrixRepository).findAllByTenantId(tenantId);

    List<AssessmentMatrix> result = service.findAllByTenantId(tenantId);

    assertThat(result).hasSize(2);
    assertThat(result).isEqualTo(expectedList);
    verify(assessmentMatrixRepository).findAllByTenantId(tenantId);
  }

  @Test
  void testCreateDefaultConfiguration() {
    AssessmentConfiguration config = service.createDefaultConfiguration();

    assertThat(config).isNotNull();
    assertThat(config.getAllowQuestionReview()).isTrue();
    assertThat(config.getRequireAllQuestions()).isTrue();
    assertThat(config.getAutoSave()).isTrue();
  }

  @Test
  void testIncrementQuestionCount() {
    String matrixId = "matrix-123";
    AssessmentMatrix existingMatrix = AssessmentMatrix.builder()
                                                      .id(matrixId)
                                                      .name("Test Matrix")
                                                      .description("Test Description")
                                                      .tenantId("tenant-123")
                                                      .performanceCycleId("cycle-123")
                                                      .questionCount(5)
                                                      .build();

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder()
                                                     .id(matrixId)
                                                     .name("Test Matrix")
                                                     .description("Test Description")
                                                     .tenantId("tenant-123")
                                                     .performanceCycleId("cycle-123")
                                                     .questionCount(6)
                                                     .build();

    doReturn(Optional.of(existingMatrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    AssessmentMatrix result = service.incrementQuestionCount(matrixId);

    assertThat(result.getQuestionCount()).isEqualTo(6);
    verify(assessmentMatrixRepository).findById(matrixId);
    verify(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  @Test
  void testDecrementQuestionCount() {
    String matrixId = "matrix-123";
    AssessmentMatrix existingMatrix = AssessmentMatrix.builder()
                                                      .id(matrixId)
                                                      .name("Test Matrix")
                                                      .description("Test Description")
                                                      .tenantId("tenant-123")
                                                      .performanceCycleId("cycle-123")
                                                      .questionCount(5)
                                                      .build();

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder()
                                                     .id(matrixId)
                                                     .name("Test Matrix")
                                                     .description("Test Description")
                                                     .tenantId("tenant-123")
                                                     .performanceCycleId("cycle-123")
                                                     .questionCount(4)
                                                     .build();

    doReturn(Optional.of(existingMatrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    AssessmentMatrix result = service.decrementQuestionCount(matrixId);

    assertThat(result.getQuestionCount()).isEqualTo(4);
    verify(assessmentMatrixRepository).findById(matrixId);
    verify(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  @Test
  void testGetAssessmentDashboard_Success() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Mock the assessment matrix
    PotentialScore potentialScore = PotentialScore.builder().score(100.0).build();

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(tenantId)
                                              .performanceCycleId("cycle-123")
                                              .potentialScore(potentialScore)
                                              .build();

    // Mock employee assessments
    NaturalPerson employee1 = NaturalPerson.builder().name("John Doe").email("john@example.com").build();

    NaturalPerson employee2 = NaturalPerson.builder().name("Jane Smith").email("jane@example.com").build();

    EmployeeAssessmentScore score1 = EmployeeAssessmentScore.builder().score(85.0).build();

    EmployeeAssessment assessment1 = EmployeeAssessment.builder()
                                                       .id("assessment-1")
                                                       .tenantId(tenantId)
                                                       .assessmentMatrixId(matrixId)
                                                       .employee(employee1)
                                                       .teamId("team-1")
                                                       .assessmentStatus(AssessmentStatus.COMPLETED)
                                                       .employeeAssessmentScore(score1)
                                                       .answeredQuestionCount(8)
                                                       .lastActivityDate(new Date())
                                                       .build();

    EmployeeAssessment assessment2 = EmployeeAssessment.builder()
                                                       .id("assessment-2")
                                                       .tenantId(tenantId)
                                                       .assessmentMatrixId(matrixId)
                                                       .employee(employee2)
                                                       .teamId("team-1")
                                                       .assessmentStatus(AssessmentStatus.IN_PROGRESS)
                                                       .answeredQuestionCount(5)
                                                       .lastActivityDate(new Date())
                                                       .build();

    List<EmployeeAssessment> employeeAssessments = Arrays.asList(assessment1, assessment2);

    // Mock team
    Team team = Team.builder()
                    .id("team-1")
                    .name("Engineering Team")
                    .description("Software Engineering Team")
                    .tenantId(tenantId)
                    .departmentId("department-1")
                    .build();

    // Set up mocks
    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);
    lenient().doReturn(Optional.of(team)).when(mockTeamService).findById("team-1");

    // Execute
    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    // Verify
    assertThat(result).isPresent();
    AssessmentDashboardData dashboardData = result.get();

    assertThat(dashboardData.getAssessmentMatrixId()).isEqualTo(matrixId);
    assertThat(dashboardData.getMatrixName()).isEqualTo("Test Matrix");
    assertThat(dashboardData.getPotentialScore()).isEqualTo(potentialScore);
    assertThat(dashboardData.getTotalEmployees()).isEqualTo(2);
    assertThat(dashboardData.getCompletedAssessments()).isEqualTo(1);

    // Employee and team summaries removed during V1 cleanup
    // Only basic assessment data is available after V1 DTO removal
    assertThat(dashboardData.getTotalEmployees()).isEqualTo(2);
    assertThat(dashboardData.getCompletedAssessments()).isEqualTo(1);
  }

  @Test
  void testGetAssessmentDashboard_MatrixNotFound() {
    String matrixId = "nonexistent-matrix";
    String tenantId = "tenant-123";

    doReturn(Optional.empty()).when(assessmentMatrixRepository).findById(matrixId);

    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    assertThat(result).isEmpty();
  }

  @Test
  void testGetAssessmentDashboard_TenantMismatch() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";
    String differentTenantId = "different-tenant";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(differentTenantId)  // Different tenant
                                              .performanceCycleId("cycle-123")
                                              .build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);

    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    assertThat(result).isEmpty();
  }

  @Test
  void testGetAssessmentDashboard_NoEmployeeAssessments() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(tenantId)
                                              .performanceCycleId("cycle-123")
                                              .build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(Collections.emptyList()).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    assertThat(result).isPresent();
    AssessmentDashboardData dashboardData = result.get();
    assertThat(dashboardData.getTotalEmployees()).isEqualTo(0);
    assertThat(dashboardData.getCompletedAssessments()).isEqualTo(0);
    // Employee and team summaries removed during V1 cleanup
  }

  @Test
  void testGetAssessmentDashboard_TeamNotFound() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(tenantId)
                                              .performanceCycleId("cycle-123")
                                              .build();

    NaturalPerson employee = NaturalPerson.builder().name("John Doe").email("john@example.com").build();

    EmployeeAssessment assessment = EmployeeAssessment.builder()
                                                      .id("assessment-1")
                                                      .tenantId(tenantId)
                                                      .assessmentMatrixId(matrixId)
                                                      .employee(employee)
                                                      .teamId("nonexistent-team")
                                                      .assessmentStatus(AssessmentStatus.COMPLETED)
                                                      .build();

    List<EmployeeAssessment> employeeAssessments = Arrays.asList(assessment);

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);
    lenient().doReturn(Optional.empty()).when(mockTeamService).findById("nonexistent-team");

    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    assertThat(result).isPresent();
    AssessmentDashboardData dashboardData = result.get();
    // Employee and team summaries removed during V1 cleanup
  }

  @Test
  void testUpdateCurrentPotentialScore_Success() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(tenantId)
                                              .performanceCycleId("cycle-123")
                                              .build();

    List<Question> questions = Arrays.asList(
                                             Question.builder()
                                                     .id("question-1")
                                                     .tenantId(tenantId)
                                                     .assessmentMatrixId(matrixId)
                                                     .pillarId("pillar-1")
                                                     .pillarName("Technical Skills")
                                                     .categoryId("category-1")
                                                     .categoryName("Programming")
                                                     .question("Test Question 1")
                                                     .questionType(QuestionType.ONE_TO_TEN)
                                                     .points(10.0)
                                                     .build(), Question.builder()
                                                                       .id("question-2")
                                                                       .tenantId(tenantId)
                                                                       .assessmentMatrixId(matrixId)
                                                                       .pillarId("pillar-1")
                                                                       .pillarName("Technical Skills")
                                                                       .categoryId("category-2")
                                                                       .categoryName("Architecture")
                                                                       .question("Test Question 2")
                                                                       .questionType(QuestionType.YES_NO)
                                                                       .points(5.0)
                                                                       .build()
    );

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder()
                                                     .id(matrixId)
                                                     .name("Test Matrix")
                                                     .description("Test Description")
                                                     .tenantId(tenantId)
                                                     .performanceCycleId("cycle-123")
                                                     .potentialScore(PotentialScore.builder().score(15.0).build())
                                                     .build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
    doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result).isNotNull();
    assertThat(result.getPotentialScore()).isNotNull();
    assertThat(result.getPotentialScore().getScore()).isEqualTo(15.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void testUpdateCurrentPotentialScore_MatrixNotFound() {
    String matrixId = "nonexistent-matrix";
    String tenantId = "tenant-123";

    doReturn(Optional.empty()).when(assessmentMatrixRepository).findById(matrixId);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateCurrentPotentialScore(matrixId, tenantId)
    );

    assertThat(exception.getMessage()).contains("Invalid - Not Found AssessmentMatrix id: " + matrixId);
  }

  @Test
  void testComputeQuestionMaxScore_OneToTen() {
    Question question = Question.builder()
                                .id("question-1")
                                .tenantId("tenant-123")
                                .assessmentMatrixId("matrix-123")
                                .pillarId("pillar-1")
                                .pillarName("Technical Skills")
                                .categoryId("category-1")
                                .categoryName("Programming")
                                .question("Test Question 1")
                                .questionType(QuestionType.ONE_TO_TEN)
                                .points(10.0)
                                .build();

    Double result = service.computeQuestionMaxScore(question);

    assertThat(result).isEqualTo(10.0);
  }

  @Test
  void testComputeQuestionMaxScore_YesNo() {
    Question question = Question.builder()
                                .id("question-2")
                                .tenantId("tenant-123")
                                .assessmentMatrixId("matrix-123")
                                .pillarId("pillar-1")
                                .pillarName("Technical Skills")
                                .categoryId("category-1")
                                .categoryName("Programming")
                                .question("Test Question 2")
                                .questionType(QuestionType.YES_NO)
                                .points(5.0)
                                .build();

    Double result = service.computeQuestionMaxScore(question);

    assertThat(result).isEqualTo(5.0);
  }

  @Test
  void shouldCalculateCorrectScoreWithSinglePillarMultipleCategories() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0), createQuestion("q2", "pillar1", "Technical", "category1", "Programming", QuestionType.YES_NO, 5.0), createQuestion("q3", "pillar1", "Technical", "category2", "Architecture", QuestionType.ONE_TO_TEN, 8.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore().getScore()).isEqualTo(23.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(1);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar1").getScore()).isEqualTo(23.0);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()).hasSize(2);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldCalculateCorrectScoreWithMultiplePillarsAndCategories() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0), createQuestion("q2", "pillar1", "Technical", "category2", "Architecture", QuestionType.YES_NO, 5.0), createQuestion("q3", "pillar2", "Leadership", "category3", "Communication", QuestionType.ONE_TO_TEN, 8.0), createQuestion("q4", "pillar2", "Leadership", "category3", "Communication", QuestionType.YES_NO, 3.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore().getScore()).isEqualTo(26.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(2);

    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar1").getScore()).isEqualTo(15.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar2").getScore()).isEqualTo(11.0);

    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()).hasSize(2);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar2")
                     .getCategoryIdToCategoryScoreMap()).hasSize(1);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldHandleEmptyQuestionsList() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Collections.emptyList();

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore().getScore()).isEqualTo(0.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).isEmpty();

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldHandleSingleQuestion() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore().getScore()).isEqualTo(10.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(1);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar1").getScore()).isEqualTo(10.0);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()).hasSize(1);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()
                     .get("category1")
                     .getScore()).isEqualTo(10.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldCreateNewPotentialScoreWhenMatrixHasNullPotentialScore() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(matrixId)
                                              .name("Test Matrix")
                                              .description("Test Description")
                                              .tenantId(tenantId)
                                              .performanceCycleId("cycle-123")
                                              .potentialScore(null)
                                              .build();

    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore()).isNotNull();
    assertThat(result.getPotentialScore().getScore()).isEqualTo(10.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldAggregateMultipleQuestionsInSameCategory() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0), createQuestion("q2", "pillar1", "Technical", "category1", "Programming", QuestionType.YES_NO, 5.0), createQuestion("q3", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 8.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result.getPotentialScore().getScore()).isEqualTo(23.0);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(1);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar1").getScore()).isEqualTo(23.0);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()).hasSize(1);
    assertThat(result.getPotentialScore()
                     .getPillarIdToPillarScoreMap()
                     .get("pillar1")
                     .getCategoryIdToCategoryScoreMap()
                     .get("category1")
                     .getScore()).isEqualTo(23.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldVerifyTotalScoreEqualsSumOfAllPillarScores() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0), createQuestion("q2", "pillar2", "Leadership", "category2", "Communication", QuestionType.YES_NO, 5.0), createQuestion("q3", "pillar3", "Strategy", "category3", "Planning", QuestionType.ONE_TO_TEN, 8.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    double totalFromPillars = result.getPotentialScore()
                                    .getPillarIdToPillarScoreMap()
                                    .values()
                                    .stream()
                                    .mapToDouble(pillar -> pillar.getScore())
                                    .sum();

    assertThat(result.getPotentialScore().getScore()).isEqualTo(totalFromPillars);
    assertThat(result.getPotentialScore().getScore()).isEqualTo(23.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldHandleLargeNumberOfQuestions() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = new java.util.ArrayList<>();

    for (int i = 1; i <= 50; i++) {
      questions.add(createQuestion(
                                   "q" + i, "pillar" + (i % 5 + 1), "Pillar " + (i % 5 + 1), "category" + (i % 10 + 1), "Category " + (i % 10 + 1), i % 2 == 0 ? QuestionType.YES_NO : QuestionType.ONE_TO_TEN, i % 2 == 0 ? 5.0 : 10.0
      ));
    }

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    double expectedTotal = questions.stream().mapToDouble(q -> q.getPoints()).sum();
    assertThat(result.getPotentialScore().getScore()).isEqualTo(expectedTotal);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(5);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldVerifyPillarScoreEqualsSumOfItsCategoryScores() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.ONE_TO_TEN, 10.0), createQuestion("q2", "pillar1", "Technical", "category1", "Programming", QuestionType.YES_NO, 5.0), createQuestion("q3", "pillar1", "Technical", "category2", "Architecture", QuestionType.ONE_TO_TEN, 8.0), createQuestion("q4", "pillar1", "Technical", "category3", "Testing", QuestionType.YES_NO, 3.0)
    );

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    double pillar1Score = result.getPotentialScore().getPillarIdToPillarScoreMap().get("pillar1").getScore();
    double sumOfCategories = result.getPotentialScore()
                                   .getPillarIdToPillarScoreMap()
                                   .get("pillar1")
                                   .getCategoryIdToCategoryScoreMap()
                                   .values()
                                   .stream()
                                   .mapToDouble(category -> category.getScore())
                                   .sum();

    assertThat(pillar1Score).isEqualTo(sumOfCategories);
    assertThat(pillar1Score).isEqualTo(26.0);

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  private AssessmentMatrix createTestMatrix(String matrixId, String tenantId) {
    return AssessmentMatrix.builder()
                           .id(matrixId)
                           .name("Test Matrix")
                           .description("Test Description")
                           .tenantId(tenantId)
                           .performanceCycleId("cycle-123")
                           .build();
  }

  private Question createQuestion(String id, String pillarId, String pillarName, String categoryId, String categoryName, QuestionType type, Double points) {
    return Question.builder()
                   .id(id)
                   .tenantId("tenant-123")
                   .assessmentMatrixId("matrix-123")
                   .pillarId(pillarId)
                   .pillarName(pillarName)
                   .categoryId(categoryId)
                   .categoryName(categoryName)
                   .question("Test Question " + id)
                   .questionType(type)
                   .points(points)
                   .build();
  }

  private void setupMocksForScoreCalculation(String matrixId, String tenantId, AssessmentMatrix matrix, List<Question> questions) {
    doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  private List<Question> createQuestionsInBulk(int totalQuestions, int pillarCount, int categoryCount) {
    List<Question> questions = new java.util.ArrayList<>();

    for (int i = 1; i <= totalQuestions; i++) {
      int pillarIndex = (i - 1) % pillarCount + 1;
      int categoryIndex = (i - 1) % categoryCount + 1;

      questions.add(createQuestion(
                                   "q" + i, "pillar" + pillarIndex, "Pillar " + pillarIndex, "category" + categoryIndex, "Category " + categoryIndex, i % 2 == 0 ? QuestionType.YES_NO : QuestionType.ONE_TO_TEN, i % 2 == 0 ? 5.0 : 10.0
      ));
    }

    return questions;
  }

  @Test
  void shouldHandle200QuestionsWithCorrectCalculationAndTiming() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = createTestMatrix(matrixId, tenantId);

    // Create 200 questions distributed across 10 pillars and 20 categories
    List<Question> questions = createQuestionsInBulk(200, 10, 20);

    setupMocksForScoreCalculation(matrixId, tenantId, matrix, questions);

    // Measure execution time
    long startTime = System.currentTimeMillis();
    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);
    long endTime = System.currentTimeMillis();
    long executionTime = endTime - startTime;

    // Calculate expected total manually
    double expectedTotal = questions.stream().mapToDouble(q -> q.getPoints()).sum();

    // Verify score calculations
    assertThat(result.getPotentialScore().getScore()).isEqualTo(expectedTotal);
    assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).hasSize(10);

    // Verify pillar distribution - each pillar should have 20 questions (200/10)
    for (int pillarIndex = 1; pillarIndex <= 10; pillarIndex++) {
      String pillarId = "pillar" + pillarIndex;
      assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap()).containsKey(pillarId);

      // Calculate actual questions for this pillar
      long questionsInPillar = questions.stream()
                                        .filter(q -> q.getPillarId().equals(pillarId))
                                        .count();

      double actualPillarScore = questions.stream()
                                          .filter(q -> q.getPillarId().equals(pillarId))
                                          .mapToDouble(Question::getPoints)
                                          .sum();

      assertThat(result.getPotentialScore().getPillarIdToPillarScoreMap().get(pillarId).getScore())
                                                                                                   .isEqualTo(actualPillarScore);
      assertThat(questionsInPillar).isEqualTo(20); // Each pillar should have exactly 20 questions
    }

    // Verify cross-calculation: total should equal sum of all pillar scores
    double totalFromPillars = result.getPotentialScore()
                                    .getPillarIdToPillarScoreMap()
                                    .values()
                                    .stream()
                                    .mapToDouble(pillar -> pillar.getScore())
                                    .sum();
    assertThat(result.getPotentialScore().getScore()).isEqualTo(totalFromPillars);

    // Performance assertions
    assertThat(executionTime).isLessThan(1000); // Should complete within 1 second

    // Log performance metrics for analysis
    System.out.println("=== 200 Questions Performance Test Results ===");
    System.out.println("Total Questions: " + questions.size());
    System.out.println("Pillars Created: " + result.getPotentialScore().getPillarIdToPillarScoreMap().size());
    System.out.println("Total Score Calculated: " + result.getPotentialScore().getScore());
    System.out.println("Execution Time: " + executionTime + " ms");
    System.out.println("Questions per millisecond: " + (200.0 / executionTime));
    System.out.println("===============================================");

    verify(assessmentMatrixRepository, times(1)).findById(matrixId);
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldGenerateInvitationTokenWithoutLockingMatrix() {
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";
    boolean lockMatrixAndRecalculateScore = false;

    AssessmentMatrix assessmentMatrix = createTestMatrix(assessmentMatrixId, tenantId);

    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixRepository).findById(assessmentMatrixId);

    String result = service.generateInvitationToken(tenantId, assessmentMatrixId, lockMatrixAndRecalculateScore);

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result.split("\\.")).hasSize(3); // JWT should have 3 parts
    assertThat(assessmentMatrix.getIsLocked()).isNull(); // Should not be locked

    verify(assessmentMatrixRepository, times(1)).findById(assessmentMatrixId);
  }

  @Test
  void shouldGenerateInvitationTokenWithLockingAndRecalculatingMatrix() {
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";
    boolean lockMatrixAndRecalculateScore = true;

    AssessmentMatrix assessmentMatrix = createTestMatrix(assessmentMatrixId, tenantId);
    List<Question> questions = Arrays.asList(
                                             createQuestion("q1", "pillar1", "Technical", "category1", "Programming", QuestionType.YES_NO, 5.0)
    );

    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixRepository).findById(assessmentMatrixId);
    doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(assessmentMatrixId, tenantId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    String result = service.generateInvitationToken(tenantId, assessmentMatrixId, lockMatrixAndRecalculateScore);

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result.split("\\.")).hasSize(3); // JWT should have 3 parts
    assertThat(assessmentMatrix.getIsLocked()).isTrue(); // Should be locked

    verify(assessmentMatrixRepository, times(1)).findById(assessmentMatrixId); // Only once for getAssessmentMatrix
    verify(mockQuestionService, times(1)).findByAssessmentMatrixId(assessmentMatrixId, tenantId);
    verify(assessmentMatrixRepository, times(1)).save(any(AssessmentMatrix.class));
  }

  @Test
  void shouldThrowExceptionWhenMatrixNotFoundDuringTokenGeneration() {
    String tenantId = "tenant-123";
    String assessmentMatrixId = "nonexistent-matrix";
    boolean lockMatrixAndRecalculateScore = false;

    doReturn(Optional.empty()).when(assessmentMatrixRepository).findById(assessmentMatrixId);

    InvalidIdReferenceException exception = assertThrows(InvalidIdReferenceException.class, () -> service.generateInvitationToken(tenantId, assessmentMatrixId, lockMatrixAndRecalculateScore));

    assertThat(exception.getMessage()).contains(assessmentMatrixId);
    assertThat(exception.getMessage()).contains("AssessmentMatrix");

    verify(assessmentMatrixRepository, times(1)).findById(assessmentMatrixId);
  }

  @Test
  void shouldThrowExceptionWhenTenantAccessViolationDuringTokenGeneration() {
    String tenantId = "tenant-123";
    String differentTenantId = "different-tenant-456";
    String assessmentMatrixId = "matrix-456";
    boolean lockMatrixAndRecalculateScore = false;

    AssessmentMatrix assessmentMatrix = createTestMatrix(assessmentMatrixId, differentTenantId);

    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixRepository).findById(assessmentMatrixId);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> service.generateInvitationToken(tenantId, assessmentMatrixId, lockMatrixAndRecalculateScore));

    assertThat(exception.getMessage()).contains("Access denied");

    verify(assessmentMatrixRepository, times(1)).findById(assessmentMatrixId);
  }
}