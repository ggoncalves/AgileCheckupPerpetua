package com.agilecheckup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
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

    PerformanceCycle mockCycle = PerformanceCycle.builder().id(performanceCycleId).name("Test Cycle").description("Test Cycle Description").tenantId(tenantId).companyId("company-123").isActive(true).isTimeSensitive(true).build();

    AssessmentMatrix savedMatrix = AssessmentMatrix.builder().id("matrix-123").name(name).description(description).tenantId(tenantId).performanceCycleId(performanceCycleId).pillarMap(pillarMap).questionCount(0).build();

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
    AssessmentConfiguration configuration = AssessmentConfiguration.builder().allowQuestionReview(false).requireAllQuestions(true).build();

    PerformanceCycle mockCycle = PerformanceCycle.builder().id(performanceCycleId).name("Test Cycle").description("Test Cycle Description").tenantId(tenantId).companyId("company-123").isActive(true).isTimeSensitive(true).build();

    AssessmentMatrix savedMatrix = AssessmentMatrix.builder().id("matrix-123").name(name).description(description).tenantId(tenantId).performanceCycleId(performanceCycleId).pillarMap(pillarMap).configuration(configuration).questionCount(0).build();

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
        AssessmentMatrix.builder().id("matrix-1").name("Matrix 1").description("Matrix 1 Description").tenantId(tenantId).performanceCycleId("cycle-1").build(), AssessmentMatrix.builder().id("matrix-2").name("Matrix 2").description("Matrix 2 Description").tenantId(tenantId).performanceCycleId("cycle-2").build()
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
    AssessmentMatrix existingMatrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").questionCount(5).build();

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").questionCount(6).build();

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
    AssessmentMatrix existingMatrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").questionCount(5).build();

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").questionCount(4).build();

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

    AssessmentMatrix matrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(tenantId).performanceCycleId("cycle-123").potentialScore(potentialScore).build();

    // Mock employee assessments
    NaturalPerson employee1 = NaturalPerson.builder().name("John Doe").email("john@example.com").build();

    NaturalPerson employee2 = NaturalPerson.builder().name("Jane Smith").email("jane@example.com").build();

    EmployeeAssessmentScore score1 = EmployeeAssessmentScore.builder().score(85.0).build();

    EmployeeAssessment assessment1 = EmployeeAssessment.builder().id("assessment-1").tenantId(tenantId).assessmentMatrixId(matrixId).employee(employee1).teamId("team-1").assessmentStatus(AssessmentStatus.COMPLETED).employeeAssessmentScore(score1).answeredQuestionCount(8).lastActivityDate(new Date()).build();

    EmployeeAssessment assessment2 = EmployeeAssessment.builder().id("assessment-2").tenantId(tenantId).assessmentMatrixId(matrixId).employee(employee2).teamId("team-1").assessmentStatus(AssessmentStatus.IN_PROGRESS).answeredQuestionCount(5).lastActivityDate(new Date()).build();

    List<EmployeeAssessment> employeeAssessments = Arrays.asList(assessment1, assessment2);

    // Mock team
    Team team = Team.builder().id("team-1").name("Engineering Team").description("Software Engineering Team").tenantId(tenantId).departmentId("department-1").build();

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

    AssessmentMatrix matrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(differentTenantId)  // Different tenant
        .performanceCycleId("cycle-123").build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);

    Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

    assertThat(result).isEmpty();
  }

  @Test
  void testGetAssessmentDashboard_NoEmployeeAssessments() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";

    AssessmentMatrix matrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(tenantId).performanceCycleId("cycle-123").build();

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

    AssessmentMatrix matrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(tenantId).performanceCycleId("cycle-123").build();

    NaturalPerson employee = NaturalPerson.builder().name("John Doe").email("john@example.com").build();

    EmployeeAssessment assessment = EmployeeAssessment.builder().id("assessment-1").tenantId(tenantId).assessmentMatrixId(matrixId).employee(employee).teamId("nonexistent-team").assessmentStatus(AssessmentStatus.COMPLETED).build();

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

    AssessmentMatrix matrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(tenantId).performanceCycleId("cycle-123").build();

    List<Question> questions = Arrays.asList(
        Question.builder().id("question-1").tenantId(tenantId).assessmentMatrixId(matrixId).pillarId("pillar-1").pillarName("Technical Skills").categoryId("category-1").categoryName("Programming").question("Test Question 1").questionType(QuestionType.ONE_TO_TEN).points(10.0).build(), Question.builder().id("question-2").tenantId(tenantId).assessmentMatrixId(matrixId).pillarId("pillar-1").pillarName("Technical Skills").categoryId("category-2").categoryName("Architecture").question("Test Question 2").questionType(QuestionType.YES_NO).points(5.0).build()
    );

    AssessmentMatrix updatedMatrix = AssessmentMatrix.builder().id(matrixId).name("Test Matrix").description("Test Description").tenantId(tenantId).performanceCycleId("cycle-123").potentialScore(PotentialScore.builder().score(15.0).build()).build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixRepository).findById(matrixId);
    doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
    doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepository).save(any(AssessmentMatrix.class));

    AssessmentMatrix result = service.updateCurrentPotentialScore(matrixId, tenantId);

    assertThat(result).isNotNull();
    assertThat(result.getPotentialScore()).isNotNull();
    assertThat(result.getPotentialScore().getScore()).isEqualTo(15.0);

    verify(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
    verify(assessmentMatrixRepository).findById(matrixId);
    verify(assessmentMatrixRepository).save(any(AssessmentMatrix.class));
  }

  @Test
  void testUpdateCurrentPotentialScore_MatrixNotFound() {
    String matrixId = "nonexistent-matrix";
    String tenantId = "tenant-123";

    List<Question> questions = Arrays.asList();

    doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
    doReturn(Optional.empty()).when(assessmentMatrixRepository).findById(matrixId);

    RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
        RuntimeException.class, () -> service.updateCurrentPotentialScore(matrixId, tenantId)
    );

    assertThat(exception.getMessage()).contains("Matrix not found: " + matrixId);
  }

  @Test
  void testComputeQuestionMaxScore_OneToTen() {
    Question question = Question.builder().id("question-1").tenantId("tenant-123").assessmentMatrixId("matrix-123").pillarId("pillar-1").pillarName("Technical Skills").categoryId("category-1").categoryName("Programming").question("Test Question 1").questionType(QuestionType.ONE_TO_TEN).points(10.0).build();

    Double result = service.computeQuestionMaxScore(question);

    assertThat(result).isEqualTo(10.0);
  }

  @Test
  void testComputeQuestionMaxScore_YesNo() {
    Question question = Question.builder().id("question-2").tenantId("tenant-123").assessmentMatrixId("matrix-123").pillarId("pillar-1").pillarName("Technical Skills").categoryId("category-1").categoryName("Programming").question("Test Question 2").questionType(QuestionType.YES_NO).points(5.0).build();

    Double result = service.computeQuestionMaxScore(question);

    assertThat(result).isEqualTo(5.0);
  }
}