package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrixWithDependenciesId;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static com.agilecheckup.util.TestObjectFactory.createMockedPerformanceCycle;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMap;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
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
  private PerformanceCycleService mockPerformanceCycleService;

  private QuestionService mockQuestionService;

  private AssessmentMatrix originalAssessmentMatrix;

  private final PerformanceCycle performanceCycle = createMockedPerformanceCycle(GENERIC_ID_1234, GENERIC_ID_1234);

  @BeforeEach
  void setUpBefore() {
    originalAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(
        DEFAULT_ID,
        createMockedPillarMap(3, 4, "Pillar", "Category"));
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
    Map<String, Pillar> mockedPillarMap = mock(Map.class);
    AssessmentMatrix existingAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(GENERIC_ID_1234, createMockedPillarMap(1, 1, "pillar", "category"));
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
    Map<String, Pillar> mockedPillarMap = mock(Map.class);

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
}