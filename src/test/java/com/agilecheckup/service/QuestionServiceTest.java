package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.service.exception.InvalidCustomOptionListException;
import com.agilecheckup.util.TestObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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

  private AssessmentMatrix assessmentMatrix = createMockedAssessmentMatrix(GENERIC_ID_1234, DEFAULT_ID, createMockedPillarMap(1, 2, PILLAR_PREFIX, CATEGORY_PREFIX));

  private Question originalQuestion;
  private Question originalCustomQuestion;

  @BeforeEach
  void setUpBefore() {
    originalQuestion = createMockedQuestion(PILLAR_ID_1, CATEGORY_ID_1);
    originalCustomQuestion = createMockedCustomQuestion(PILLAR_ID_1, CATEGORY_ID_2);
    setPillarAndCategoryToAssessmentMatrix(assessmentMatrix);
  }

  private AssessmentMatrix setPillarAndCategoryToAssessmentMatrix(AssessmentMatrix assessmentMatrix) {
    int index = 0;
    String[] categoryIds = {CATEGORY_ID_1, CATEGORY_ID_2};
    Map<String, Pillar> newPillarMap = new HashMap<>();
    Map<String, Category> newCategoryMap = new HashMap<>();
    for (Map.Entry<String, Pillar> pillarEntry : assessmentMatrix.getPillarMap().entrySet()) {
      for (Map.Entry<String, Category> categoryEntry : pillarEntry.getValue().getCategoryMap().entrySet()) {
        Category category = categoryEntry.getValue();
        category.setId(categoryIds[index++]);
        newCategoryMap.put(category.getId(), category);
      }
      Pillar pillar = pillarEntry.getValue();
      pillar.setId(PILLAR_ID_1);
      pillar.setCategoryMap(newCategoryMap);
      newPillarMap.put(pillar.getId(), pillar);
    }
    assessmentMatrix.setPillarMap(newPillarMap);
    return assessmentMatrix;
  }

  @Test
  void createCustomQuestionWithEmptyText() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createMockedQuestionOptionList("", 0d, 5d, 10d, 20d, 30d), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list text is empty.", exception.getMessage());
  }

  @Test
  void createCustomQuestionWithEmptyList() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            new ArrayList<QuestionOption>(), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list is empty. There should be at least 2 options.", exception.getMessage());
  }

  @Test
  void createCustomQuestionTooShort() {
    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createMockedQuestionOptionList("Prefix", 0d), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

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
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list too big. There should be at max 64 options.", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsDuplicated() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(1, 1, 2), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 1, 1, 2", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsDuplicated2() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(2, 1, 3, 5, 7, 6, 4, 6), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 1, 2, 3, 4, 5, 6, 6, 7", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsWithZero() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(0, 1, 2), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 0, 1, 2", exception.getMessage());
  }

  @Test
  void createCustomQuestionInvalidIdsMissing() {

    // When
    Exception exception = assertThrows(InvalidCustomOptionListException.class,
        () -> questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
            originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
            createQuestionOption(1, 3, 4), originalCustomQuestion.getAssessmentMatrixId(),
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

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
            originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId()));

    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids" +
        ". The ids in the sorted order: 1, 2, 3, 4, 6", exception.getMessage());
  }

  private List<QuestionOption> createQuestionOption(Integer ... ids) {
    return IntStream.range(0, ids.length)
        .mapToObj(index -> TestObjectFactory.createQuestionOption(ids[index], "Text", 5d))
        .collect(Collectors.toList());
  }

  @Test
  void create() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedQuestion, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId());

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(savedQuestion);
    verify(questionService).create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId());
    verify(assessmentMatrixService).incrementQuestionCount(originalCustomQuestion.getAssessmentMatrixId());
  }

  @Test
  void createCustomQuestion() {
    Question savedQuestion = copyQuestionAndAddId(originalCustomQuestion, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedQuestion, questionRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalCustomQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.createCustomQuestion(originalCustomQuestion.getQuestion(),
        originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
        createMockedQuestionOptionList("OptionPrefix", 0d, 5d, 10d, 20d, 30d),
        originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId());

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(savedQuestion);
    verify(questionService).createCustomQuestion(originalCustomQuestion.getQuestion(),
        originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true,
        createMockedQuestionOptionList("OptionPrefix", 0d, 5d, 10d, 20d, 30d),
        originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId());
    verify(assessmentMatrixService).incrementQuestionCount(originalCustomQuestion.getAssessmentMatrixId());
  }

  @Test
  void create_NullQuestion() {
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());
    // When
    assertThrows(NullPointerException.class, () -> questionService.create(null, originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId()));
  }

  @Test
  void create_NullPoints() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);

    doReturn(savedQuestion).when(questionRepository).save(any());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());
    // When
    questionService.create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), null, originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId());
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

  private Question createMockedQuestion(String pillarId, String categoryId) {
    Question q = TestObjectFactory.createMockedQuestion(DEFAULT_ID);
    q.setPillarId(pillarId);
    q.setCategoryId(categoryId);
    return q;
  }

  private Question createMockedCustomQuestion(String pillarId, String categoryId) {
    Question q = TestObjectFactory.createMockedCustomQuestion(DEFAULT_ID);
    q.setPillarId(pillarId);
    q.setCategoryId(categoryId);
    return q;
  }
}