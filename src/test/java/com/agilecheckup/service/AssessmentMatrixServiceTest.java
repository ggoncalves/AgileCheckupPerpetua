package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.dto.EmployeeAssessmentSummary;
import com.agilecheckup.service.dto.TeamAssessmentSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.createCustomAssessmentConfiguration;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrixWithConfiguration;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrixWithDependenciesId;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static com.agilecheckup.util.TestObjectFactory.createMockedPerformanceCycle;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMapV2;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixServiceTest extends AbstractCrudServiceTest<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> {

  @InjectMocks
  @Spy
  private AssessmentMatrixService assessmentMatrixService;

  @Mock
  private AssessmentMatrixRepository mockAssessmentMatrixRepository;

  @Mock
  private PerformanceCycleServiceLegacy mockPerformanceCycleService;

  private QuestionService mockQuestionService;

  @Mock
  private EmployeeAssessmentService mockEmployeeAssessmentService;

  @Mock
  private TeamServiceLegacy mockTeamService;

  private AssessmentMatrix originalAssessmentMatrix;

  private final PerformanceCycle performanceCycle = createMockedPerformanceCycle(GENERIC_ID_1234, GENERIC_ID_1234);

  @BeforeEach
  void setUpBefore() {
    originalAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(
        DEFAULT_ID,
        createMockedPillarMapV2(3, 4, "Pillar", "Category"));
    originalAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    mockQuestionService = Mockito.mock(QuestionService.class);
  }

  @Test
  void create() {
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedAssessmentMatrix, mockAssessmentMatrixRepository);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
    verify(mockAssessmentMatrixRepository).save(savedAssessmentMatrix);
    verify(assessmentMatrixService).create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
    );
    verify(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycleId());
  }

  @Test
  void createNonExistantPerformanceCycleId() {
    assertThrows(NullPointerException.class, () -> assertCreateAssessmentMatrixFor("NonExistentPerformanceCycleId"));
  }

  @Test
  void createNullPerformanceCycleId() {
    assertThrows(NullPointerException.class, () -> assertCreateAssessmentMatrixFor(null));
  }

  void assertCreateAssessmentMatrixFor(String performanceCycleId) {
    originalAssessmentMatrix.setPerformanceCycleId(null);
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedAssessmentMatrix).when(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    if (performanceCycleId != null) doReturn(Optional.empty()).when(mockPerformanceCycleService).findById(any());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        performanceCycleId,
        originalAssessmentMatrix.getPillarMap()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    verify(assessmentMatrixService).create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        performanceCycleId,
        originalAssessmentMatrix.getPillarMap()
    );
    if (performanceCycleId == null) {
      verify(mockPerformanceCycleService, never()).findById(performanceCycleId);
    }
    else {
      verify(mockPerformanceCycleService).findById(performanceCycleId);
    }
  }

  @Test
  void create_NullAssessmentMatrixName() {
    // When
    assertThrows(NullPointerException.class, () -> assessmentMatrixService.create(
        null,
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
                                                                               ));
  }

  @Test
  void shouldIncrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    mockFindById(assessmentMatrixId, originalAssessmentMatrix);
    mockPerformLockedAsSynchronous();

    assessmentMatrixService.incrementQuestionCount(assessmentMatrixId);

    assertQuestionCountIncremented();
    verifyRepositoryInteractions(assessmentMatrixId);
  }

  @Test
  void shouldNotIncrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    doReturn(null).when(mockAssessmentMatrixRepository).findById(assessmentMatrixId);
    assessmentMatrixService.incrementQuestionCount(assessmentMatrixId);
    verify(mockAssessmentMatrixRepository, never()).save(any());
  }

  @Test
  void shouldDecrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    originalAssessmentMatrix.setQuestionCount(5);
    mockFindById(assessmentMatrixId, originalAssessmentMatrix);
    mockPerformLockedAsSynchronous();

    assessmentMatrixService.decrementQuestionCount(assessmentMatrixId);

    assertQuestionCountDecremented();
    verifyRepositoryInteractions(assessmentMatrixId);
  }

  @Test
  void shouldDecrementQuestionCountToZero() {
    String assessmentMatrixId = "matrixId";
    originalAssessmentMatrix.setQuestionCount(1);
    mockFindById(assessmentMatrixId, originalAssessmentMatrix);
    mockPerformLockedAsSynchronous();

    assessmentMatrixService.decrementQuestionCount(assessmentMatrixId);

    assertEquals(0, originalAssessmentMatrix.getQuestionCount());
    verifyRepositoryInteractions(assessmentMatrixId);
  }

  @Test
  void shouldNotDecrementBelowZero() {
    String assessmentMatrixId = "matrixId";
    originalAssessmentMatrix.setQuestionCount(0);
    mockFindById(assessmentMatrixId, originalAssessmentMatrix);
    mockPerformLockedAsSynchronous();

    assessmentMatrixService.decrementQuestionCount(assessmentMatrixId);

    assertEquals(0, originalAssessmentMatrix.getQuestionCount());
    verifyRepositoryInteractions(assessmentMatrixId);
  }

  @Test
  void shouldNotDecrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    doReturn(null).when(mockAssessmentMatrixRepository).findById(assessmentMatrixId);
    assessmentMatrixService.decrementQuestionCount(assessmentMatrixId);
    verify(mockAssessmentMatrixRepository, never()).save(any());
  }

  @Test
  void shouldSumAllOptionsOnMultipleChoiceCustomQuestion() {
    // sum is 70
    Question question = createMockedCustomQuestion("questionId1", true, 10d, 10d, 20d, 10d, 20d);
    assertEquals(70, assessmentMatrixService.computeQuestionMaxScore(question));
  }

  @Test
  void shouldSumAllOptionsOnMultipleChoiceCustomQuestionIncludingNegativePoints() {
    // sum is 30
    Question question = createMockedCustomQuestion("questionId1", true, 10d, 10d, -20d, 10d, 20d);
    assertEquals(30, assessmentMatrixService.computeQuestionMaxScore(question));
  }

  @Test
  void shouldReturnMaxPossibleOptionOnCustomQuestion() {
    Question question = createMockedCustomQuestion("questionId1", false, 10d, 30d, -20d, 10d, 20d);
    assertEquals(30, assessmentMatrixService.computeQuestionMaxScore(question));
  }

  @Test
  void shouldReturnMaxPossibleOptionOnCustomQuestionOnlyNegatives() {
    Question question = createMockedCustomQuestion("questionId1", false, -10d, 0d, -20d);
    assertEquals(0, assessmentMatrixService.computeQuestionMaxScore(question));
  }

  @Test
  void shouldReturnPointsOnNonCustomQuestion() {
    Question question = createMockedQuestion(10d);
    assertEquals(10, assessmentMatrixService.computeQuestionMaxScore(question));
  }

  @Test
  void shouldCreatePotentialScoreWithPillarAndCategoryMapDuringFirstRun() {
    // Given
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(createMockedQuestionList()).when(mockQuestionService).findByAssessmentMatrixId(anyString(),
        anyString());

    mockFindById("assessmentMatrix1", originalAssessmentMatrix);

    // When
    AssessmentMatrix assessmentMatrix = assessmentMatrixService.updateCurrentPotentialScore("assessmentMatrix1", "tenantId1");

    // Then
    assertPotentialScores(assessmentMatrix);
  }
  @Test
  void shouldEmptyPotentialScoreWhenNoQuestionFound() {
    // Given
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(createMockedQuestionList()).when(mockQuestionService).findByAssessmentMatrixId(anyString(),
        anyString());

    mockFindById("assessmentMatrix1", originalAssessmentMatrix);
    AssessmentMatrix assessmentMatrix = assessmentMatrixService.updateCurrentPotentialScore("assessmentMatrix1", "tenantId1");
    assertPotentialScores(assessmentMatrix);

    // Now passing an empty question list
    doReturn(new LinkedList<Question>()).when(mockQuestionService).findByAssessmentMatrixId(anyString(),
        anyString());

    // When
    assessmentMatrix = assessmentMatrixService.updateCurrentPotentialScore("assessmentMatrix1", "tenantId1");

    // Then
    assertNotNull(assessmentMatrix);
    assertNotNull(assessmentMatrix.getPotentialScore());
    assertTrue(assessmentMatrix.getPotentialScore().getPillarIdToPillarScoreMap().isEmpty());
    assertEquals(0, assessmentMatrix.getPotentialScore().getScore());
  }

  @Test
  void shouldUpdatePotentialScoreWithPillarAndCategoryMap() {
    // Given
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(createMockedQuestionList()).when(mockQuestionService).findByAssessmentMatrixId(anyString(),
        anyString());

    mockFindById("assessmentMatrix1", originalAssessmentMatrix);
    AssessmentMatrix assessmentMatrix = assessmentMatrixService.updateCurrentPotentialScore("assessmentMatrix1", "tenantId1");
    assertPotentialScores(assessmentMatrix);

    // Now passing an empty question list
    doReturn(createUpdatedMockedQuestionList()).when(mockQuestionService).findByAssessmentMatrixId(anyString(),
        anyString());

    // When
    assessmentMatrix = assessmentMatrixService.updateCurrentPotentialScore("assessmentMatrix1", "tenantId1");

    // Then
    assertUpdatedPotentialScores(assessmentMatrix);
  }

  private void mockFindById(String matrixId, AssessmentMatrix assessmentMatrix) {
    when(mockAssessmentMatrixRepository.findById(matrixId)).thenReturn(assessmentMatrix);
  }

  private void mockPerformLockedAsSynchronous() {
    doAnswer(invocation -> {
      Runnable runnable = invocation.getArgument(1);
      runnable.run();
      return null;
    }).when(mockAssessmentMatrixRepository).performLocked(anyString(), any(Runnable.class));
  }

  private void assertQuestionCountIncremented() {
    assertEquals(1, originalAssessmentMatrix.getQuestionCount());
  }

  private void assertQuestionCountDecremented() {
    assertEquals(4, originalAssessmentMatrix.getQuestionCount());
  }

  private void verifyRepositoryInteractions(String matrixId) {
    verify(mockAssessmentMatrixRepository, times(2)).findById(matrixId);
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
  }

  private List<Question> createMockedQuestionList() {
    List<Question> questionList = new LinkedList<>();
    // Total Max Points = 75 + 85 = 160
    // Pillar 1 - 75
    //   Category 11 - Pt 20
    //     Question 111 Pt 5 - (Rating 5)
    questionList.add(createMockedQuestion("q111", QuestionType.STAR_FIVE, "p1", "Pillar1", "c11", "Category11",
        5d));
    //     Question 112 Pt Max 15 (Custom Regular)
    questionList.add(createMockedCustomQuestion("q112", false, "p1", "Pillar1", "c11", "Category11", 10d, 10d, 15d
        , 10d));
    //   Category 12 - Pt 55
    //     Question 121 Pt 15 (Rating 3)
    questionList.add(createMockedQuestion("q121", QuestionType.STAR_THREE, "p1", "Pillar1", "c12", "Category12",
        15d));
    //     Question 122 Pt Sum 40(Custom Multiple Choice)
    questionList.add(createMockedCustomQuestion("q122", true, "p1", "Pillar1", "c12", "Category12", 5d, 10d, 10d
        , 5d, 10d));
    // Pillar 2 - 85
    //   Category 21 - Pt 30
    //     Question 211 Pt 20 (Good Bad)
    questionList.add(createMockedQuestion("q211", QuestionType.GOOD_BAD, "p2", "Pillar2", "c21", "Category21",
        20d));
    //     Question 212 Pt 10 (Yes No)
    questionList.add(createMockedQuestion("q212", QuestionType.YES_NO, "p2", "Pillar2", "c21", "Category21",
        10d));
    //   Category 22 - Pt 55
    //     Question 221 Pt Sum 30 (Custom Multiple Choice)
    questionList.add(createMockedCustomQuestion("q221", true, "p2", "Pillar2", "c22", "Category22", 5d, 10d, 10d
        , 5d));
    //     Question 222 Pt Max 25(Custom Regular)
    questionList.add(createMockedCustomQuestion("q222", false, "p2", "Pillar2", "c22", "Category22", 10d, 25d, 15d
        , 10d));

    return questionList;
  }

  private List<Question> createUpdatedMockedQuestionList() {
    List<Question> questionList = new LinkedList<>();
    // Total Max Points = 100 + 90 = 190
    // Pillar 1 - 100
    //   Category 11 - Pt 20
    //     Question 111 Pt 5 - (Rating 5)
    questionList.add(createMockedQuestion("qu111", QuestionType.STAR_FIVE, "pu1", "PUillar1", "cu11", "CUategory11",
        5d));
    //     Question 112 Pt Max 15 (Custom Regular)
    questionList.add(createMockedCustomQuestion("qu112", false, "pu1", "PUillar1", "cu11", "CUategory11", 10d, 10d, 15d
        , 10d));
    //   Category 12 - Pt 80
    //     Question 121 Pt 45 (Rating 3)
    questionList.add(createMockedQuestion("qu121", QuestionType.STAR_THREE, "pu1", "PUillar1", "cu12", "CUategory12",
        45d));
    //     Question 122 Pt Sum 35(Custom Multiple Choice)
    questionList.add(createMockedCustomQuestion("qu122", true, "pu1", "PUillar1", "cu12", "CUategory12", 5d, 10d, 10d
        , 5d, 5d));
    // Pillar 2 - 90
    //   Category 21 - Pt 15
    //     Question 211 Pt 10 (Good Bad)
    questionList.add(createMockedQuestion("qu211", QuestionType.GOOD_BAD, "pu2", "PUillar2", "cu21", "CUategory21",
        10d));
    //     Question 212 Pt 5 (Yes No)
    questionList.add(createMockedQuestion("qu212", QuestionType.YES_NO, "pu2", "PUillar2", "cu21", "CUategory21",
        5d));
    //   Category 22 - Pt 75
    //     Question 221 Pt Sum 40 (Custom Multiple Choice)
    questionList.add(createMockedCustomQuestion("qu221", true, "pu2", "PUillar2", "cu22", "CUategory22", 5d, 10d, 10d
        , 15d));
    //     Question 222 Pt Max 35(Custom Regular)
    questionList.add(createMockedCustomQuestion("qu222", false, "pu2", "PUillar2", "cu22", "CUategory22", 10d, 25d, 15d
        , 35d));

    return questionList;
  }

  private void assertPotentialScores(AssessmentMatrix assessmentMatrix){
    assertNotNull(assessmentMatrix);
    assertNotNull(assessmentMatrix.getPotentialScore());
    assertEquals(160, assessmentMatrix.getPotentialScore().getScore());

    PotentialScore ps = assessmentMatrix.getPotentialScore();

    // Pillar
    Map<String, PillarScore> pillarScoreMap = ps.getPillarIdToPillarScoreMap();

    PillarScore pillar1Score = pillarScoreMap.get("p1");
    assertEquals(75, pillar1Score.getScore());
    assertEquals("p1", pillar1Score.getPillarId());
    assertEquals("Pillar1", pillar1Score.getPillarName());

    PillarScore pillar2Score = pillarScoreMap.get("p2");
    assertEquals(85, pillar2Score.getScore());
    assertEquals("p2", pillar2Score.getPillarId());
    assertEquals("Pillar2", pillar2Score.getPillarName());

    // Category Points
    Map<String, CategoryScore> categoryScoreMap1 = pillar1Score.getCategoryIdToCategoryScoreMap();
    assertEquals(20, categoryScoreMap1.get("c11").getScore());
    assertEquals("c11", categoryScoreMap1.get("c11").getCategoryId());
    assertEquals("Category11", categoryScoreMap1.get("c11").getCategoryName());
    assertEquals(55, categoryScoreMap1.get("c12").getScore());
    assertEquals("c12", categoryScoreMap1.get("c12").getCategoryId());
    assertEquals("Category12", categoryScoreMap1.get("c12").getCategoryName());

    Map<String, CategoryScore> categoryScoreMap2 = pillar2Score.getCategoryIdToCategoryScoreMap();
    assertEquals(30, categoryScoreMap2.get("c21").getScore());
    assertEquals("c21", categoryScoreMap2.get("c21").getCategoryId());
    assertEquals("Category21", categoryScoreMap2.get("c21").getCategoryName());
    assertEquals(55, categoryScoreMap2.get("c22").getScore());
    assertEquals("c22", categoryScoreMap2.get("c22").getCategoryId());
    assertEquals("Category22", categoryScoreMap2.get("c22").getCategoryName());

    // Question Points
    List<QuestionScore> questionScores = categoryScoreMap1.get("c11").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q111 = QuestionScore.builder().questionId("q111").score(5d).build();
    QuestionScore q112 = QuestionScore.builder().questionId("q112").score(15d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q111, q112);

    questionScores = categoryScoreMap1.get("c12").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q121 = QuestionScore.builder().questionId("q121").score(15d).build();
    QuestionScore q122 = QuestionScore.builder().questionId("q122").score(40d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q121, q122);

    questionScores = categoryScoreMap2.get("c21").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q211 = QuestionScore.builder().questionId("q211").score(20d).build();
    QuestionScore q212 = QuestionScore.builder().questionId("q212").score(10d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q211, q212);

    questionScores = categoryScoreMap2.get("c22").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q221 = QuestionScore.builder().questionId("q221").score(30d).build();
    QuestionScore q222 = QuestionScore.builder().questionId("q222").score(25d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q221, q222);

  }

  @Test
  void update_existingAssessmentMatrix_shouldSucceed() {
    // Prepare
    @SuppressWarnings("unchecked")
    Map<String, PillarV2> mockedPillarMap = mock(Map.class);
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, createMockedPillarMapV2(1, 1, "pillar", "category"));
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);
    AssessmentMatrix updatedAssessmentMatrixDetails = createMockedAssessmentMatrixWithDependenciesId("updatedCycleId", mockedPillarMap);
    updatedAssessmentMatrixDetails.setName("Updated Matrix Name");
    updatedAssessmentMatrixDetails.setDescription("Updated Description");
    updatedAssessmentMatrixDetails.setTenantId("Updated Tenant Id");

    PerformanceCycle updatedPerformanceCycle = createMockedPerformanceCycle("companyId", "updatedCycleId");

    // Mock repository calls
    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedAssessmentMatrixDetails, mockAssessmentMatrixRepository);
    doReturn(Optional.of(updatedPerformanceCycle)).when(mockPerformanceCycleService).findById("updatedCycleId");
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(false).when(mockQuestionService).hasCategoryQuestions(anyString(), anyString(), anyString());

    // When
    Optional<AssessmentMatrix> resultOptional = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Matrix Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedCycleId",
        mockedPillarMap
                                                                              );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedAssessmentMatrixDetails, resultOptional.get());
    verify(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    verify(mockAssessmentMatrixRepository).save(updatedAssessmentMatrixDetails);
    verify(mockPerformanceCycleService).findById("updatedCycleId");
    verify(assessmentMatrixService).update(DEFAULT_ID,
        "Updated Matrix Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedCycleId",
        mockedPillarMap);
  }

  @Test
  void update_nonExistingAssessmentMatrix_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";
    @SuppressWarnings("unchecked")
    Map<String, PillarV2> mockedPillarMap = mock(Map.class);

    // Mock repository calls
    doReturn(null).when(mockAssessmentMatrixRepository).findById(nonExistingId);

    // When
    Optional<AssessmentMatrix> resultOptional = assessmentMatrixService.update(
        nonExistingId,
        "name",
        "desc",
        "tenant",
        "cycleId",
        mockedPillarMap
                                                                              );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(mockAssessmentMatrixRepository).findById(nonExistingId);
    verify(assessmentMatrixService).update(nonExistingId, "name", "desc", "tenant", "cycleId", mockedPillarMap);
  }

  @Test
  void findAllByTenantId_shouldReturnFilteredAssessmentMatrices() {
    // Given
    String tenantId = "tenant-123";

    AssessmentMatrix matrix1 = AssessmentMatrix.builder()
        .id("matrix-1")
        .name("Engineering Matrix")
        .description("For engineering assessments")
        .tenantId(tenantId)
        .performanceCycleId("cycle-1")
        .build();

    AssessmentMatrix matrix2 = AssessmentMatrix.builder()
        .id("matrix-2")
        .name("Sales Matrix")
        .description("For sales assessments")
        .tenantId(tenantId)
        .performanceCycleId("cycle-2")
        .build();

    List<AssessmentMatrix> expectedMatrices = List.of(matrix1, matrix2);

    // Create a mock PaginatedQueryList
    @SuppressWarnings("unchecked")
    com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList<AssessmentMatrix> mockPaginatedList =
        mock(com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList.class);
    when(mockPaginatedList.stream()).thenReturn(expectedMatrices.stream());

    when(mockAssessmentMatrixRepository.findAllByTenantId(tenantId)).thenReturn(mockPaginatedList);

    // When
    List<AssessmentMatrix> result = assessmentMatrixService.findAllByTenantId(tenantId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(matrix1));
    assertTrue(result.contains(matrix2));
    verify(mockAssessmentMatrixRepository).findAllByTenantId(tenantId);
  }

  @Test
  void create_withNullDescription_shouldSetEmptyString() {
    // Given
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);
    savedAssessmentMatrix.setDescription("");

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedAssessmentMatrix, mockAssessmentMatrixRepository);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        null, // null description
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals("", assessmentMatrixOptional.get().getDescription());
    verify(mockAssessmentMatrixRepository).save(savedAssessmentMatrix);
  }

  @Test
  void update_withNullDescription_shouldSetEmptyString() {
    // Prepare
    @SuppressWarnings("unchecked")
    Map<String, PillarV2> mockedPillarMap = mock(Map.class);
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, createMockedPillarMapV2(1, 1, "pillar", "category"));
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);
    
    AssessmentMatrix updatedAssessmentMatrixDetails = createMockedAssessmentMatrixWithDependenciesId("updatedCycleId", mockedPillarMap);
    updatedAssessmentMatrixDetails.setName("Updated Matrix Name");
    updatedAssessmentMatrixDetails.setDescription(""); // Should be empty string
    updatedAssessmentMatrixDetails.setTenantId("Updated Tenant Id");

    PerformanceCycle updatedPerformanceCycle = createMockedPerformanceCycle("companyId", "updatedCycleId");

    // Mock repository calls
    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedAssessmentMatrixDetails, mockAssessmentMatrixRepository);
    doReturn(Optional.of(updatedPerformanceCycle)).when(mockPerformanceCycleService).findById("updatedCycleId");
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(false).when(mockQuestionService).hasCategoryQuestions(anyString(), anyString(), anyString());

    // When
    Optional<AssessmentMatrix> resultOptional = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Matrix Name",
        null, // null description
        "Updated Tenant Id",
        "updatedCycleId",
        mockedPillarMap
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals("", resultOptional.get().getDescription());
    verify(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    verify(mockAssessmentMatrixRepository).save(updatedAssessmentMatrixDetails);
  }

  @Test
  void update_withCategoryWithQuestions_shouldThrowValidationException() {
    // Prepare
    Map<String, PillarV2> currentPillarMap = createMockedPillarMapV2(1, 1, "pillar", "category");
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, currentPillarMap);
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);

    // New pillar map without the existing category (simulating deletion)
    Map<String, PillarV2> newPillarMap = createMockedPillarMapV2(1, 0, "pillar", "category"); // No categories

    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    
    // Mock that category has questions
    doReturn(true).when(mockQuestionService).hasCategoryQuestions(eq(DEFAULT_ID), anyString(), anyString());

    // When & Then
    assertThrows(com.agilecheckup.service.exception.ValidationException.class, () -> 
        assessmentMatrixService.update(
            DEFAULT_ID,
            "Updated Matrix Name",
            "Updated Description",
            "Updated Tenant Id",
            GENERIC_ID_1234,
            newPillarMap
        )
    );
  }

  @Test
  void update_withPillarWithQuestions_shouldThrowValidationException() {
    // Prepare
    Map<String, PillarV2> currentPillarMap = createMockedPillarMapV2(2, 1, "pillar", "category");
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, currentPillarMap);
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);

    // New pillar map with only one pillar (simulating deletion of a pillar)
    Map<String, PillarV2> newPillarMap = createMockedPillarMapV2(1, 1, "pillar", "category");

    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    
    // Mock that the removed pillar's category has questions
    doReturn(true).when(mockQuestionService).hasCategoryQuestions(anyString(), anyString(), anyString());

    // When & Then
    assertThrows(com.agilecheckup.service.exception.ValidationException.class, () -> 
        assessmentMatrixService.update(
            DEFAULT_ID,
            "Updated Matrix Name",
            "Updated Description", 
            "Updated Tenant Id",
            GENERIC_ID_1234,
            newPillarMap
        )
    );
  }

  @Test
  void update_withoutDeletingAnything_shouldSucceed() {
    // Prepare - create consistent pillar maps using the same entities
    Map<String, PillarV2> currentPillarMap = createMockedPillarMapV2(1, 1, "pillar", "category");
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, currentPillarMap);
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);

    // Use the SAME pillar map instance (no deletion)
    Map<String, PillarV2> newPillarMap = existingAssessmentMatrix.getPillarMap();

    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(GENERIC_ID_1234);
    doAnswerForUpdate(existingAssessmentMatrix, mockAssessmentMatrixRepository);

    // When
    Optional<AssessmentMatrix> result = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Matrix Name",
        "Updated Description",
        "Updated Tenant Id", 
        GENERIC_ID_1234,
        newPillarMap
    );

    // Then
    assertTrue(result.isPresent());
    verify(mockAssessmentMatrixRepository).save(existingAssessmentMatrix);
    // No validation should occur since nothing is being deleted
  }

  @Test
  void update_deletingCategoryWithoutQuestions_shouldSucceed() {
    // Prepare - use a more controlled test setup
    Map<String, PillarV2> currentPillarMap = createMockedPillarMapV2(1, 2, "pillar", "category");
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, currentPillarMap);
    existingAssessmentMatrix = cloneWithId(existingAssessmentMatrix, DEFAULT_ID);

    // Create new pillar map manually to control the difference
    Map<String, PillarV2> newPillarMap = createMockedPillarMapV2(1, 1, "pillar", "category");
    
    doReturn(existingAssessmentMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(GENERIC_ID_1234);
    doAnswerForUpdate(existingAssessmentMatrix, mockAssessmentMatrixRepository);
    
    // Mock that categories have NO questions - be lenient about multiple calls
    doReturn(false).when(mockQuestionService).hasCategoryQuestions(anyString(), anyString(), anyString());

    // When
    Optional<AssessmentMatrix> result = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Matrix Name",
        "Updated Description",
        "Updated Tenant Id",
        GENERIC_ID_1234,
        newPillarMap
    );

    // Then
    assertTrue(result.isPresent());
    verify(mockAssessmentMatrixRepository).save(existingAssessmentMatrix);
    // Don't verify specific call count since the implementation might validate each category
  }

  @Test
  void createDefaultConfiguration_shouldReturnExpectedDefaults() {
    // When
    AssessmentConfiguration config = assessmentMatrixService.createDefaultConfiguration();

    // Then
    assertThat(config).isNotNull();
    assertThat(config.getAllowQuestionReview()).isTrue();
    assertThat(config.getRequireAllQuestions()).isTrue();
    assertThat(config.getAutoSave()).isTrue();
    assertThat(config.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
  }

  // ========== Configuration Tests ==========

  @Test
  void getEffectiveConfiguration_withNullConfiguration_shouldReturnDefault() {
    // Given
    AssessmentMatrix matrixWithoutConfig = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234,
        createMockedPillarMapV2(1, 1, "pillar", "category"));
    matrixWithoutConfig.setConfiguration(null);

    // When
    AssessmentConfiguration effectiveConfig = assessmentMatrixService.getEffectiveConfiguration(matrixWithoutConfig);

    // Then
    assertThat(effectiveConfig).isNotNull();
    assertThat(effectiveConfig.getAllowQuestionReview()).isTrue();
    assertThat(effectiveConfig.getRequireAllQuestions()).isTrue();
    assertThat(effectiveConfig.getAutoSave()).isTrue();
    assertThat(effectiveConfig.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
  }

  @Test
  void getEffectiveConfiguration_withExistingConfiguration_shouldReturnExisting() {
    // Given
    AssessmentConfiguration customConfig = createCustomAssessmentConfiguration(false, false, false, QuestionNavigationType.SEQUENTIAL);
    AssessmentMatrix matrixWithConfig = createMockedAssessmentMatrixWithConfiguration(GENERIC_ID_1234,
        createMockedPillarMapV2(1, 1, "pillar", "category"), customConfig);

    // When
    AssessmentConfiguration effectiveConfig = assessmentMatrixService.getEffectiveConfiguration(matrixWithConfig);

    // Then
    assertThat(effectiveConfig).isNotNull();
    assertThat(effectiveConfig.getAllowQuestionReview()).isFalse();
    assertThat(effectiveConfig.getRequireAllQuestions()).isFalse();
    assertThat(effectiveConfig.getAutoSave()).isFalse();
    assertThat(effectiveConfig.getNavigationMode()).isEqualTo(QuestionNavigationType.SEQUENTIAL);
  }

  @Test
  void createWithConfiguration_shouldSaveConfiguration() {
    // Given
    AssessmentConfiguration customConfig = createCustomAssessmentConfiguration(false, true, false, QuestionNavigationType.FREE_FORM);
    AssessmentMatrix savedMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);
    savedMatrix.setConfiguration(customConfig);

    doAnswerForSaveWithRandomEntityId(savedMatrix, mockAssessmentMatrixRepository);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService)
        .findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When
    Optional<AssessmentMatrix> result = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap(),
        customConfig
                                                                      );

    // Then
    assertThat(result).isPresent();
    AssessmentMatrix createdMatrix = result.get();
    assertThat(createdMatrix.getConfiguration()).isNotNull();
    assertThat(createdMatrix.getConfiguration().getAllowQuestionReview()).isFalse();
    assertThat(createdMatrix.getConfiguration().getRequireAllQuestions()).isTrue();
    assertThat(createdMatrix.getConfiguration().getAutoSave()).isFalse();
    assertThat(createdMatrix.getConfiguration().getNavigationMode()).isEqualTo(QuestionNavigationType.FREE_FORM);
    verify(mockAssessmentMatrixRepository).save(savedMatrix);
  }

  @Test
  void createWithoutConfiguration_shouldAcceptNullConfiguration() {
    // Given
    AssessmentMatrix savedMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);
    savedMatrix.setConfiguration(null);

    doAnswerForSaveWithRandomEntityId(savedMatrix, mockAssessmentMatrixRepository);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService)
        .findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When
    Optional<AssessmentMatrix> result = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap(),
        null
                                                                      );

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getConfiguration()).isNull();
    verify(mockAssessmentMatrixRepository).save(savedMatrix);
  }

  @Test
  void updateWithConfiguration_shouldUpdateConfiguration() {
    // Given
    AssessmentConfiguration newConfig = createCustomAssessmentConfiguration(true, false, true, QuestionNavigationType.SEQUENTIAL);
    AssessmentMatrix existingMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, createMockedPillarMapV2(1, 1, "pillar", "category"));
    existingMatrix = cloneWithId(existingMatrix, DEFAULT_ID);

    doReturn(existingMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(GENERIC_ID_1234);
    doReturn(mockQuestionService).when(assessmentMatrixService).getQuestionService();
    doReturn(false).when(mockQuestionService).hasCategoryQuestions(anyString(), anyString(), anyString());
    doAnswerForUpdate(existingMatrix, mockAssessmentMatrixRepository);

    // When
    Optional<AssessmentMatrix> result = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Name",
        "Updated Description",
        "Updated Tenant",
        GENERIC_ID_1234,
        originalAssessmentMatrix.getPillarMap(),
        newConfig
                                                                      );

    // Then
    assertThat(result).isPresent();
    AssessmentMatrix updatedMatrix = result.get();
    assertThat(updatedMatrix.getConfiguration()).isNotNull();
    assertThat(updatedMatrix.getConfiguration().getAllowQuestionReview()).isTrue();
    assertThat(updatedMatrix.getConfiguration().getRequireAllQuestions()).isFalse();
    assertThat(updatedMatrix.getConfiguration().getAutoSave()).isTrue();
    assertThat(updatedMatrix.getConfiguration().getNavigationMode()).isEqualTo(QuestionNavigationType.SEQUENTIAL);
    verify(mockAssessmentMatrixRepository).save(existingMatrix);
  }

  @Test
  void backwardCompatibility_createWithoutConfiguration_shouldStillWork() {
    // Given
    AssessmentMatrix savedMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    doAnswerForSaveWithRandomEntityId(savedMatrix, mockAssessmentMatrixRepository);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService)
        .findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When - using the old create method without configuration
    Optional<AssessmentMatrix> result = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
                                                                      );

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getConfiguration()).isNull(); // Should be null for backward compatibility
    verify(mockAssessmentMatrixRepository).save(savedMatrix);
  }

  @Test
  void backwardCompatibility_updateWithoutConfiguration_shouldStillWork() {
    // Given - use the same pillar map to avoid validation logic (no categories are being removed)
    AssessmentMatrix existingMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234,
        createMockedPillarMapV2(1, 1, "pillar", "category"));
    existingMatrix = cloneWithId(existingMatrix, DEFAULT_ID);
    Map<String, PillarV2> samePillarMap = existingMatrix.getPillarMap(); // Same map = no validation needed

    doReturn(existingMatrix).when(mockAssessmentMatrixRepository).findById(DEFAULT_ID);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(GENERIC_ID_1234);
    doAnswerForUpdate(existingMatrix, mockAssessmentMatrixRepository);

    // When - using the old update method without configuration
    Optional<AssessmentMatrix> result = assessmentMatrixService.update(
        DEFAULT_ID,
        "Updated Name",
        "Updated Description",
        "Updated Tenant",
        GENERIC_ID_1234,
        samePillarMap
                                                                      );

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getConfiguration()).isNull(); // Should be null for backward compatibility
    verify(mockAssessmentMatrixRepository).save(existingMatrix);
  }

  @Test
  void getAssessmentDashboard_whenMatrixNotFound_shouldReturnEmpty() {
    // Given
    String matrixId = "nonexistent-matrix";
    String tenantId = "tenant123";

    doReturn(Optional.empty()).when(assessmentMatrixService).findById(matrixId);

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isEmpty();
    verify(assessmentMatrixService).findById(matrixId);
  }

  @Test
  void getAssessmentDashboard_whenTenantMismatch_shouldReturnEmpty() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";
    String differentTenantId = "different-tenant";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(differentTenantId)
        .name(originalAssessmentMatrix.getName())
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isEmpty();
    verify(assessmentMatrixService).findById(matrixId);
  }

  @Test
  void getAssessmentDashboard_withNoEmployeeAssessments_shouldReturnEmptyData() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);
    doReturn(mockEmployeeAssessmentService).when(assessmentMatrixService).getEmployeeAssessmentService();
    doReturn(new ArrayList<>()).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isPresent();
    AssessmentDashboardData data = result.get();
    assertThat(data.getAssessmentMatrixId()).isEqualTo(matrixId);
    assertThat(data.getMatrixName()).isEqualTo("Test Matrix");
    assertThat(data.getTeamSummaries()).isEmpty();
    assertThat(data.getEmployeeSummaries()).isEmpty();
    assertThat(data.getTotalEmployees()).isEqualTo(0);
    assertThat(data.getCompletedAssessments()).isEqualTo(0);
  }

  @Test
  void getAssessmentDashboard_withMixedAssessmentStatuses_shouldCalculateCorrectly() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    List<EmployeeAssessment> employeeAssessments = createMixedStatusEmployeeAssessments(matrixId, tenantId);

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);
    doReturn(mockEmployeeAssessmentService).when(assessmentMatrixService).getEmployeeAssessmentService();
    doReturn(mockTeamService).when(assessmentMatrixService).getTeamService();
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    // Mock team service calls
    setupTeamServiceMocks();

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isPresent();
    AssessmentDashboardData data = result.get();

    assertThat(data.getAssessmentMatrixId()).isEqualTo(matrixId);
    assertThat(data.getMatrixName()).isEqualTo("Test Matrix");
    assertThat(data.getTotalEmployees()).isEqualTo(5);
    assertThat(data.getCompletedAssessments()).isEqualTo(2); // Only COMPLETED ones

    // Check team summaries
    assertThat(data.getTeamSummaries()).hasSize(2);
    TeamAssessmentSummary team1Summary = data.getTeamSummaries().stream()
        .filter(ts -> ts.getTeamId().equals("team-1"))
        .findFirst().orElse(null);
    assertThat(team1Summary).isNotNull();
    assertThat(team1Summary.getTeamName()).isEqualTo("Team Alpha");
    assertThat(team1Summary.getTotalEmployees()).isEqualTo(3);
    assertThat(team1Summary.getCompletedAssessments()).isEqualTo(2);
    assertThat(team1Summary.getCompletionPercentage()).isEqualTo(66.67, org.assertj.core.data.Offset.offset(0.01));
    assertThat(team1Summary.getAverageScore()).isEqualTo(87.5, org.assertj.core.data.Offset.offset(0.01));

    // Check employee summaries
    assertThat(data.getEmployeeSummaries()).hasSize(5);
    EmployeeAssessmentSummary completedEmployee = data.getEmployeeSummaries().stream()
        .filter(es -> es.getAssessmentStatus() == AssessmentStatus.COMPLETED)
        .findFirst().orElse(null);
    assertThat(completedEmployee).isNotNull();
    assertThat(completedEmployee.getCurrentScore()).isNotNull();
  }

  @Test
  void getAssessmentDashboard_withEmployeesWithoutTeams_shouldHandleGracefully() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    List<EmployeeAssessment> employeeAssessments = createEmployeeAssessmentsWithoutTeams(matrixId, tenantId);

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);
    doReturn(mockEmployeeAssessmentService).when(assessmentMatrixService).getEmployeeAssessmentService();
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isPresent();
    AssessmentDashboardData data = result.get();

    // Should have no team summaries since employees have no teams
    assertThat(data.getTeamSummaries()).isEmpty();
    // But should still have employee summaries
    assertThat(data.getEmployeeSummaries()).hasSize(2);
    assertThat(data.getTotalEmployees()).isEqualTo(2);
  }

  @Test
  void getAssessmentDashboard_withTeamServiceErrors_shouldSkipFailedTeams() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    List<EmployeeAssessment> employeeAssessments = createEmployeeAssessmentsWithMixedTeams(matrixId, tenantId);

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);
    doReturn(mockEmployeeAssessmentService).when(assessmentMatrixService).getEmployeeAssessmentService();
    doReturn(mockTeamService).when(assessmentMatrixService).getTeamService();
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    // Mock team service - one succeeds, one fails
    Team goodTeam = Team.builder()
        .id("team-1")
        .name("Good Team")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();
    doReturn(Optional.of(goodTeam)).when(mockTeamService).findById("team-1");
    doThrow(new RuntimeException("Database error"))
        .when(mockTeamService).findById("team-2");

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isPresent();
    AssessmentDashboardData data = result.get();

    // Should only have one team summary (the successful one)
    assertThat(data.getTeamSummaries()).hasSize(1);
    assertThat(data.getTeamSummaries().get(0).getTeamId()).isEqualTo("team-1");
    assertThat(data.getTeamSummaries().get(0).getTeamName()).isEqualTo("Good Team");

    // Employee summaries should still be complete
    assertThat(data.getEmployeeSummaries()).hasSize(4);
  }

  @Test
  void getAssessmentDashboard_withComplexScenario_shouldCalculateAccurately() {
    // Given
    String matrixId = "matrix123";
    String tenantId = "tenant123";

    AssessmentMatrix matrix = AssessmentMatrix.builder()
        .id(matrixId)
        .tenantId(tenantId)
        .name("Complex Test Matrix")
        .description(originalAssessmentMatrix.getDescription())
        .performanceCycleId(originalAssessmentMatrix.getPerformanceCycleId())
        .pillarMap(originalAssessmentMatrix.getPillarMap())
        .build();

    List<EmployeeAssessment> employeeAssessments = createComplexEmployeeAssessmentScenario(matrixId, tenantId);

    doReturn(Optional.of(matrix)).when(assessmentMatrixService).findById(matrixId);
    doReturn(mockEmployeeAssessmentService).when(assessmentMatrixService).getEmployeeAssessmentService();
    doReturn(mockTeamService).when(assessmentMatrixService).getTeamService();
    doReturn(employeeAssessments).when(mockEmployeeAssessmentService).findByAssessmentMatrix(matrixId, tenantId);

    setupComplexTeamServiceMocks();

    // When
    Optional<AssessmentDashboardData> result = assessmentMatrixService.getAssessmentDashboard(matrixId, tenantId);

    // Then
    assertThat(result).isPresent();
    AssessmentDashboardData data = result.get();

    assertThat(data.getTotalEmployees()).isEqualTo(6);
    assertThat(data.getCompletedAssessments()).isEqualTo(3);
    assertThat(data.getTeamSummaries()).hasSize(3);

    // Verify team with 100% completion
    TeamAssessmentSummary perfectTeam = data.getTeamSummaries().stream()
        .filter(ts -> ts.getCompletionPercentage() == 100.0)
        .findFirst().orElse(null);
    assertThat(perfectTeam).isNotNull();
    assertThat(perfectTeam.getAverageScore()).isNotNull();

    // Verify team with 0% completion
    TeamAssessmentSummary incompleteTeam = data.getTeamSummaries().stream()
        .filter(ts -> ts.getCompletionPercentage() == 0.0)
        .findFirst().orElse(null);
    assertThat(incompleteTeam).isNotNull();
    assertThat(incompleteTeam.getAverageScore()).isNull(); // No completed assessments
  }

  // ========== Dashboard Tests ==========

  private void assertUpdatedPotentialScores(AssessmentMatrix assessmentMatrix){
    assertNotNull(assessmentMatrix);
    assertNotNull(assessmentMatrix.getPotentialScore());
    assertEquals(190, assessmentMatrix.getPotentialScore().getScore());

    PotentialScore ps = assessmentMatrix.getPotentialScore();

    // Pillar
    Map<String, PillarScore> pillarScoreMap = ps.getPillarIdToPillarScoreMap();

    PillarScore pillar1Score = pillarScoreMap.get("pu1");
    assertEquals(100, pillar1Score.getScore());
    assertEquals("pu1", pillar1Score.getPillarId());
    assertEquals("PUillar1", pillar1Score.getPillarName());

    PillarScore pillar2Score = pillarScoreMap.get("pu2");
    assertEquals(90, pillar2Score.getScore());
    assertEquals("pu2", pillar2Score.getPillarId());
    assertEquals("PUillar2", pillar2Score.getPillarName());

    // Category Points
    Map<String, CategoryScore> categoryScoreMap1 = pillar1Score.getCategoryIdToCategoryScoreMap();
    assertEquals(20, categoryScoreMap1.get("cu11").getScore());
    assertEquals("cu11", categoryScoreMap1.get("cu11").getCategoryId());
    assertEquals("CUategory11", categoryScoreMap1.get("cu11").getCategoryName());
    assertEquals(80, categoryScoreMap1.get("cu12").getScore());
    assertEquals("cu12", categoryScoreMap1.get("cu12").getCategoryId());
    assertEquals("CUategory12", categoryScoreMap1.get("cu12").getCategoryName());

    Map<String, CategoryScore> categoryScoreMap2 = pillar2Score.getCategoryIdToCategoryScoreMap();
    assertEquals(15, categoryScoreMap2.get("cu21").getScore());
    assertEquals("cu21", categoryScoreMap2.get("cu21").getCategoryId());
    assertEquals("CUategory21", categoryScoreMap2.get("cu21").getCategoryName());
    assertEquals(75, categoryScoreMap2.get("cu22").getScore());
    assertEquals("cu22", categoryScoreMap2.get("cu22").getCategoryId());
    assertEquals("CUategory22", categoryScoreMap2.get("cu22").getCategoryName());

    // Question Points
    List<QuestionScore> questionScores = categoryScoreMap1.get("cu11").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q111 = QuestionScore.builder().questionId("qu111").score(5d).build();
    QuestionScore q112 = QuestionScore.builder().questionId("qu112").score(15d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q111, q112);

    questionScores = categoryScoreMap1.get("cu12").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q121 = QuestionScore.builder().questionId("qu121").score(45d).build();
    QuestionScore q122 = QuestionScore.builder().questionId("qu122").score(35d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q121, q122);

    questionScores = categoryScoreMap2.get("cu21").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q211 = QuestionScore.builder().questionId("qu211").score(10d).build();
    QuestionScore q212 = QuestionScore.builder().questionId("qu212").score(5d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q211, q212);

    questionScores = categoryScoreMap2.get("cu22").getQuestionScores();
    assertEquals(2, questionScores.size());
    QuestionScore q221 = QuestionScore.builder().questionId("qu221").score(40d).build();
    QuestionScore q222 = QuestionScore.builder().questionId("qu222").score(35d).build();
    org.assertj.core.api.Assertions.assertThat(questionScores).usingElementComparatorOnFields("questionId", "score").containsExactlyInAnyOrder(
        q221, q222);

  }

  // ========== Dashboard Test Helper Methods ==========

  private List<EmployeeAssessment> createComplexEmployeeAssessmentScenario(String matrixId, String tenantId) {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    // Team 1: 2 employees, both completed (100% completion)
    assessments.add(createEmployeeAssessment("emp1", "Perfect Employee 1", "pe1@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.COMPLETED, 95.0, 15));
    assessments.add(createEmployeeAssessment("emp2", "Perfect Employee 2", "pe2@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.COMPLETED, 88.0, 14));

    // Team 2: 2 employees, none completed (0% completion)
    assessments.add(createEmployeeAssessment("emp3", "Slow Employee 1", "se1@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.INVITED, null, 0));
    assessments.add(createEmployeeAssessment("emp4", "Slow Employee 2", "se2@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.IN_PROGRESS, null, 2));

    // Team 3: 2 employees, 1 completed (50% completion)
    assessments.add(createEmployeeAssessment("emp5", "Mixed Employee 1", "me1@example.com", matrixId, tenantId, "team-3",
        AssessmentStatus.COMPLETED, 78.0, 12));
    assessments.add(createEmployeeAssessment("emp6", "Mixed Employee 2", "me2@example.com", matrixId, tenantId, "team-3",
        AssessmentStatus.IN_PROGRESS, null, 6));

    return assessments;
  }

  private EmployeeAssessment createEmployeeAssessment(String id, String name, String email, String matrixId,
                                                      String tenantId, String teamId, AssessmentStatus status,
                                                      Double score, Integer answeredQuestions) {
    EmployeeAssessmentScore assessmentScore = null;
    if (score != null) {
      assessmentScore = EmployeeAssessmentScore.builder()
          .score(score)
          .build();
    }

    return EmployeeAssessment.builder()
        .id(id)
        .tenantId(tenantId)
        .assessmentMatrixId(matrixId)
        .teamId(teamId)
        .employee(createNaturalPersonWithEmail(name, email))
        .assessmentStatus(status)
        .employeeAssessmentScore(assessmentScore)
        .answeredQuestionCount(answeredQuestions)
        .build();
  }

  private List<EmployeeAssessment> createEmployeeAssessmentsWithMixedTeams(String matrixId, String tenantId) {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    // Team 1: 2 employees
    assessments.add(createEmployeeAssessment("emp1", "Good Team Member 1", "gtm1@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.COMPLETED, 80.0, 10));
    assessments.add(createEmployeeAssessment("emp2", "Good Team Member 2", "gtm2@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.IN_PROGRESS, null, 5));

    // Team 2: 2 employees (this team service will fail)
    assessments.add(createEmployeeAssessment("emp3", "Bad Team Member 1", "btm1@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.COMPLETED, 70.0, 8));
    assessments.add(createEmployeeAssessment("emp4", "Bad Team Member 2", "btm2@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.INVITED, null, 0));

    return assessments;
  }

  private List<EmployeeAssessment> createEmployeeAssessmentsWithoutTeams(String matrixId, String tenantId) {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    assessments.add(createEmployeeAssessment("emp1", "Solo Worker", "solo@example.com", matrixId, tenantId, null,
        AssessmentStatus.COMPLETED, 75.0, 8));
    assessments.add(createEmployeeAssessment("emp2", "Freelancer", "freelancer@example.com", matrixId, tenantId, "",
        AssessmentStatus.IN_PROGRESS, null, 4));

    return assessments;
  }

  private List<EmployeeAssessment> createMixedStatusEmployeeAssessments(String matrixId, String tenantId) {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    // Team 1: 3 employees, 2 completed
    assessments.add(createEmployeeAssessment("emp1", "John Doe", "john@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.COMPLETED, 85.0, 10));
    assessments.add(createEmployeeAssessment("emp2", "Jane Smith", "jane@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.COMPLETED, 90.0, 12));
    assessments.add(createEmployeeAssessment("emp3", "Bob Johnson", "bob@example.com", matrixId, tenantId, "team-1",
        AssessmentStatus.IN_PROGRESS, null, 5));

    // Team 2: 2 employees, 0 completed
    assessments.add(createEmployeeAssessment("emp4", "Alice Brown", "alice@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.INVITED, null, 0));
    assessments.add(createEmployeeAssessment("emp5", "Charlie Wilson", "charlie@example.com", matrixId, tenantId, "team-2",
        AssessmentStatus.IN_PROGRESS, null, 3));

    return assessments;
  }

  private Team createMockedTeam(String id) {
    return Team.builder()
        .id(id)
        .name("Team " + id)
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();
  }

  private com.agilecheckup.persistency.entity.person.NaturalPerson createNaturalPersonWithEmail(String name, String email) {
    return com.agilecheckup.persistency.entity.person.NaturalPerson.builder()
        .name(name)
        .email(email)
        .documentNumber("1234")
        .personDocumentType(com.agilecheckup.persistency.entity.person.PersonDocumentType.CPF)
        .gender(com.agilecheckup.persistency.entity.person.Gender.MALE)
        .genderPronoun(com.agilecheckup.persistency.entity.person.GenderPronoun.HE)
        .build();
  }

  private void setupComplexTeamServiceMocks() {
    Team team1 = Team.builder()
        .id("team-1")
        .name("Perfect Team")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();
    Team team2 = Team.builder()
        .id("team-2")
        .name("Slow Team")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();
    Team team3 = Team.builder()
        .id("team-3")
        .name("Mixed Team")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();

    doReturn(Optional.of(team1)).when(mockTeamService).findById("team-1");
    doReturn(Optional.of(team2)).when(mockTeamService).findById("team-2");
    doReturn(Optional.of(team3)).when(mockTeamService).findById("team-3");
  }

  private void setupTeamServiceMocks() {
    Team team1 = Team.builder()
        .id("team-1")
        .name("Team Alpha")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();
    Team team2 = Team.builder()
        .id("team-2")
        .name("Team Beta")
        .description("Test team")
        .tenantId("test-tenant-123")
        .departmentId("dept-123")
        .build();

    doReturn(Optional.of(team1)).when(mockTeamService).findById("team-1");
    doReturn(Optional.of(team2)).when(mockTeamService).findById("team-2");
  }
}