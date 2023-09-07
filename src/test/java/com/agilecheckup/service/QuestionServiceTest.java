package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.util.TestObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    String[] categoryIds = { CATEGORY_ID_1, CATEGORY_ID_2};
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
  void create() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedQuestion).when(questionRepository).save(any());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId());

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(originalQuestion);
    verify(questionService).create(originalQuestion.getQuestion(), originalQuestion.getQuestionType(), originalQuestion.getTenantId(), originalQuestion.getPoints(), originalQuestion.getAssessmentMatrixId(), originalQuestion.getPillarId(), originalQuestion.getCategoryId());
  }

  @Test
  void createCustomQuestion() {
    Question savedQuestion = copyQuestionAndAddId(originalCustomQuestion, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedQuestion).when(questionRepository).save(any());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalCustomQuestion.getAssessmentMatrixId());

    // When
    Optional<Question> questionOptional = questionService.createCustomQuestion(originalCustomQuestion.getQuestion(), originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true, createMockedQuestionOptionList("OptionPrefix", 0, 5, 10, 20, 30), originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId());

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(originalCustomQuestion);
    verify(questionService).createCustomQuestion(originalCustomQuestion.getQuestion(), originalCustomQuestion.getQuestionType(), originalCustomQuestion.getTenantId(), false, true, createMockedQuestionOptionList("OptionPrefix", 0, 5, 10, 20, 30), originalCustomQuestion.getAssessmentMatrixId(), originalCustomQuestion.getPillarId(), originalCustomQuestion.getCategoryId());
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

  @Override
  AbstractCrudService<Question, AbstractCrudRepository<Question>> getCrudServiceSpy() {
    return questionService;
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