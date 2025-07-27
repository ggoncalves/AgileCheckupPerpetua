package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.service.exception.InvalidCustomOptionListException;
import com.agilecheckup.util.TestObjectFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.copyQuestionAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrix;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMapV2;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestionOptionList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest extends AbstractCrudServiceTest<Question, AbstractCrudRepository<Question>> {

  public static final String PILLAR_PREFIX = "pillarId";
  public static final String CATEGORY_PREFIX = "categoryId";

  public static final String PILLAR_ID_1 = PILLAR_PREFIX + "1";

  public static final String CATEGORY_ID_1 = CATEGORY_PREFIX + "1";

  public static final String CATEGORY_ID_2 = CATEGORY_PREFIX + "2";

  @InjectMocks
  @Spy
  private QuestionService questionService;

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private AssessmentMatrixService assessmentMatrixService;

  private final AssessmentMatrix assessmentMatrix = createMockedAssessmentMatrix(GENERIC_ID_1234, DEFAULT_ID,
      createMockedPillarMapV2(1, 2, PILLAR_PREFIX, CATEGORY_PREFIX));

  private Question originalQuestion;
  private Question originalCustomQuestion;

  @BeforeEach
  void setUpBefore() {
    originalQuestion = createMockedQuestion();
    originalCustomQuestion = createMockedCustomQuestion(CATEGORY_ID_2);
    setPillarAndCategoryToAssessmentMatrix(assessmentMatrix);
  }

  private void setPillarAndCategoryToAssessmentMatrix(AssessmentMatrix assessmentMatrix) {
    int index = 0;
    String[] categoryIds = {CATEGORY_ID_1, CATEGORY_ID_2};
    Map<String, PillarV2> newPillarMap = new HashMap<>();
    Map<String, CategoryV2> newCategoryMap = new HashMap<>();
    for (Map.Entry<String, PillarV2> pillarEntry : assessmentMatrix.getPillarMap().entrySet()) {
      for (Map.Entry<String, CategoryV2> categoryEntry : pillarEntry.getValue().getCategoryMap().entrySet()) {
        CategoryV2 category = categoryEntry.getValue();
        category.setId(categoryIds[index++]);
        newCategoryMap.put(category.getId(), category);
      }
      PillarV2 pillar = pillarEntry.getValue();
      pillar.setId(PILLAR_ID_1);
      pillar.setCategoryMap(newCategoryMap);
      newPillarMap.put(pillar.getId(), pillar);
    }
    assessmentMatrix.setPillarMap(newPillarMap);
  }

  @Test
  void createCustomQuestionWithEmptyText() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createMockedQuestionOptionList("", 0d, 5d, 10d, 20d, 30d), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list text is empty.", exception.getMessage());
  }

  @Test
  void createCustomQuestionWithEmptyList() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            new ArrayList<>(), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list is empty. There should be at least 2 options.", exception.getMessage());
  }

  @Test
  void createCustomQuestionTooShort() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createMockedQuestionOptionList("Prefix", 0d), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list too small. There should be at least 2 options.", exception.getMessage());
  }

  @Test
  void createCustomQuestionTooBig() {
    Double[] options = IntStream
        .rangeClosed(1, 65)
        .asDoubleStream()
        .boxed()
        .toArray(Double[]::new);

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createMockedQuestionOptionList("Prefix", options), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list too big. There should be at max 64 options.", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsDuplicated() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(1, 1, 2), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 1, 1, 2", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsDuplicated2() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(2, 1, 3, 5, 7, 6, 4, 6), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 1, 2, 3, 4, 5, 6, 6, 7", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsWithZero() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(0, 1, 2), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 0, 1, 2", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsMissing() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(1, 3, 4), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids" +
        ". The ids in the sorted order: 1, 3, 4", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsMissing2() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(2, 3, 4, 1, 6), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description"));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids" +
        ". The ids in the sorted order: 1, 2, 3, 4, 6", exception.getMessage());
  }

  private List<QuestionOption> createQuestionOption(Integer ... ids) {
    return Arrays.stream(ids).map(id -> TestObjectFactory.createQuestionOption(id, "Text", 5d))
        .collect(Collectors.toList());
  }

  @Test
  void create() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);
    savedQuestion.setExtraDescription("Test extra description");

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedQuestion, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId(), "Test extra description");

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(savedQuestion);
    verify(questionService).create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId(), "Test extra description");
    verify(assessmentMatrixService).incrementQuestionCount(originalCustomQuestion.getAssessmentMatrixId());
  }

  @Test
  void createCustomQuestion() {
    Question savedQuestion = copyQuestionAndAddId(originalCustomQuestion, DEFAULT_ID);
    savedQuestion.setExtraDescription("Test extra description");

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedQuestion, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalCustomQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
        originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
        createMockedQuestionOptionList("OptionPrefix", 0d, 5d, 10d, 20d, 30d),
        originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(),
        originalCustomQuestion.getCategoryId(), "Test extra description");

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(savedQuestion);
    verify(questionService).createCustomQuestion(originalCustomQuestion.getQuestion(),
        originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
        createMockedQuestionOptionList("OptionPrefix", 0d, 5d, 10d, 20d, 30d),
        originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId(), "Test extra description");
    verify(assessmentMatrixService).incrementQuestionCount(originalCustomQuestion.getAssessmentMatrixId());
  }

  @Test
  void create_NullQuestion() {
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());
    // When
    assertThrows(NullPointerException.class, () -> questionService.create(null, originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId(), "Test extra description"));
  }

  @Test
  void create_NullPoints() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);

    doReturn(savedQuestion).when(questionRepository).save(any());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());
    // When
    questionService.create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), null, originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId(), "Test extra description");
  }

  @Test
  void toOptionMap() {
    List<QuestionOption> options = createMockedQuestionOptionList("Option", 5d, 10d, 15d, 20d);
    assertEquals(1, options.get(0).getId());
    assertEquals(2, options.get(1).getId());
    assertEquals(3, options.get(2).getId());
    assertEquals(4, options.get(3).getId());

    Map<Integer, QuestionOption> map = questionService.toOptionMap(options);

    assertEquals(4, map.size());
    assertEquals(5, map.get(1).getPoints());
    assertEquals(10, map.get(2).getPoints());
    assertEquals(15, map.get(3).getPoints());
    assertEquals(20, map.get(4).getPoints());
  }

  private Question createMockedQuestion() {
    Question q = TestObjectFactory.createMockedQuestion(DEFAULT_ID);
    q.setPillarId(QuestionServiceTest.PILLAR_ID_1);
    q.setCategoryId(QuestionServiceTest.CATEGORY_ID_1);
    return q;
  }

  private Question createMockedCustomQuestion(String categoryId) {
    Question q = TestObjectFactory.createMockedCustomQuestion(DEFAULT_ID);
    q.setPillarId(QuestionServiceTest.PILLAR_ID_1);
    q.setCategoryId(categoryId);
    return q;
  }

  @Test
  void update_existingQuestion_shouldSucceed() {
    // Prepare
    Question existingQuestion = createMockedQuestion();
    Question updatedQuestionDetails = createMockedQuestion();
    updatedQuestionDetails.setQuestion("Updated Question Text");
    updatedQuestionDetails.setQuestionType(QuestionType.ONE_TO_TEN);
    updatedQuestionDetails.setTenantId("Updated Tenant Id");
    updatedQuestionDetails.setPoints(10.0);
    updatedQuestionDetails.setExtraDescription("Test extra description");

    // Mock repository calls
    doReturn(existingQuestion).when(questionRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedQuestionDetails, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(DEFAULT_ID);

    // When
    Optional<Question> resultOptional = questionService.update(
        DEFAULT_ID,
        "Updated Question Text",
        QuestionType.ONE_TO_TEN,
        "Updated Tenant Id",
        10.0,
        DEFAULT_ID,
        PILLAR_ID_1,
        CATEGORY_ID_1,
        "Test extra description"
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedQuestionDetails, resultOptional.get());
    verify(questionRepository).findById(DEFAULT_ID);
    verify(questionRepository).save(updatedQuestionDetails);
    verify(assessmentMatrixService).findById(DEFAULT_ID);
    verify(questionService).update(DEFAULT_ID,
        "Updated Question Text",
        QuestionType.ONE_TO_TEN,
        "Updated Tenant Id",
        10.0,
        DEFAULT_ID,
        PILLAR_ID_1,
        CATEGORY_ID_1,
        "Test extra description");
  }

  @Test
  void updateCustomQuestion_existingQuestion_shouldSucceed() {
    // Prepare
    boolean updatedIsMultipleChoice = false;
    boolean updatedIsFlushed = true;
    Question existingQuestion = createMockedCustomQuestion(CATEGORY_ID_1);
    List<QuestionOption> updatedOptions = createMockedQuestionOptionList("Option", 5d, 10d, 15d, 20d);
    Question updatedQuestion = createUpdatedQuestionOptionList(updatedIsMultipleChoice, updatedIsFlushed, updatedOptions);

    // Mock repository calls
    doReturn(existingQuestion).when(questionRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedQuestion, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(DEFAULT_ID);

    // When
    Optional<Question> resultOptional = questionService.updateCustomQuestion(
        DEFAULT_ID,
        "Updated Custom Question Text",
        QuestionType.CUSTOMIZED,
        "Updated Tenant Id",
        updatedIsMultipleChoice,
        updatedIsFlushed,
        updatedOptions,
        DEFAULT_ID,
        PILLAR_ID_1,
        CATEGORY_ID_1,
        "Test extra description"
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedQuestion, resultOptional.get());
    verify(questionRepository).findById(DEFAULT_ID);
    verify(questionRepository).save(updatedQuestion);
    verify(assessmentMatrixService).findById(DEFAULT_ID);
    verify(questionService).updateCustomQuestion(DEFAULT_ID,
        "Updated Custom Question Text",
        QuestionType.CUSTOMIZED,
        "Updated Tenant Id",
        false,
        true,
        updatedOptions,
        DEFAULT_ID,
        PILLAR_ID_1,
        CATEGORY_ID_1,
        "Test extra description");
  }

  @Test
  void update_nonExistingQuestion_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(questionRepository).findById(nonExistingId);

    // When
    Optional<Question> resultOptional = questionService.update(
        nonExistingId,
        "question",
        QuestionType.YES_NO,
        "tenant",
        5.0,
        "matrixId",
        "pillarId",
        "categoryId",
        "Test extra description"
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(questionRepository).findById(nonExistingId);
    verify(questionService).update(nonExistingId, "question", QuestionType.YES_NO, "tenant", 5.0, "matrixId", "pillarId", "categoryId", "Test extra description");
  }

  @Test
  void hasCategoryQuestions_withExistingQuestions_shouldReturnTrue() {
    // Given
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-789";

    doReturn(true).when(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);

    // When
    boolean result = questionService.hasCategoryQuestions(matrixId, categoryId, tenantId);

    // Then
    assertTrue(result);
    verify(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);
  }

  @Test
  void hasCategoryQuestions_withNoQuestions_shouldReturnFalse() {
    // Given
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-789";

    doReturn(false).when(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);

    // When
    boolean result = questionService.hasCategoryQuestions(matrixId, categoryId, tenantId);

    // Then
    assertFalse(result);
    verify(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);
  }

  @Test
  void findByAssessmentMatrixId_shouldDelegateToRepository() {
    // Given
    String matrixId = "matrix-123";
    String tenantId = "tenant-789";
    List<Question> expectedQuestions = List.of(createMockedQuestion());

    doReturn(expectedQuestions).when(questionRepository).findByAssessmentMatrixId(matrixId, tenantId);

    // When
    List<Question> result = questionService.findByAssessmentMatrixId(matrixId, tenantId);

    // Then
    assertEquals(expectedQuestions, result);
    verify(questionRepository).findByAssessmentMatrixId(matrixId, tenantId);
  }

  @Test
  void findAllByTenantId_shouldDelegateToRepository() {
    // Given
    String tenantId = "tenant-789";
    @SuppressWarnings("unchecked")
    PaginatedQueryList<Question> expectedQuestions = mock(PaginatedQueryList.class);

    doReturn(expectedQuestions).when(questionRepository).findAllByTenantId(tenantId);

    // When
    PaginatedQueryList<Question> result = questionService.findAllByTenantId(tenantId);

    // Then
    assertEquals(expectedQuestions, result);
    verify(questionRepository).findAllByTenantId(tenantId);
  }

  @Test
  void delete_shouldDecrementQuestionCount() {
    // Given
    Question questionToDelete = createMockedQuestion();
    questionToDelete.setAssessmentMatrixId("matrix-123");

    // When
    questionService.delete(questionToDelete);

    // Then
    verify(questionRepository).delete(questionToDelete);
    verify(assessmentMatrixService).decrementQuestionCount("matrix-123");
  }

  @Test
  void delete_withNullQuestion_shouldNotDecrementQuestionCount() {
    // When
    questionService.delete(null);

    // Then
    verify(questionRepository).delete(null);
    verify(assessmentMatrixService, org.mockito.Mockito.never()).decrementQuestionCount(org.mockito.ArgumentMatchers.anyString());
  }



  private Question createUpdatedQuestionOptionList(boolean updatedIsMultipleChoice, boolean updatedIsFlushed, List<QuestionOption> updatedOptions) {
    Question updatedQuestion = createMockedCustomQuestion(CATEGORY_ID_1);
    updatedQuestion.setQuestion("Updated Custom Question Text");
    updatedQuestion.setTenantId("Updated Tenant Id");
    updatedQuestion.setExtraDescription("Test extra description");
    updatedQuestion.setOptionGroup(OptionGroup.builder()
        .isMultipleChoice(updatedIsMultipleChoice)
        .showFlushed(updatedIsFlushed)
        .optionMap(questionService.toOptionMap(updatedOptions))
        .build());
    return updatedQuestion;
  }
}