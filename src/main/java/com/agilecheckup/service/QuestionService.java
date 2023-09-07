package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class QuestionService extends AbstractCrudService<Question, AbstractCrudRepository<Question>> {

  private QuestionRepository questionRepository;

  private AssessmentMatrixService assessmentMatrixService;

  @Inject
  public QuestionService(QuestionRepository questionRepository, AssessmentMatrixService assessmentMatrixService) {
    this.questionRepository = questionRepository;
    this.assessmentMatrixService = assessmentMatrixService;
  }

  public Optional<Question> create(String questionTxt, QuestionType questionType, String tenantId, Integer points, String assessmentMatrixId, String pillarId, String categoryId) {
    return super.create(internalCreateQuestion(questionTxt, questionType, tenantId, points, assessmentMatrixId, pillarId, categoryId));
  }

  public Optional<Question> createCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId) {
    return super.create(internalCreateCustomQuestion(questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options, assessmentMatrixId, pillarId, categoryId));
  }

  private Question internalCreateQuestion(String questionTxt, QuestionType questionType, String tenantId, Integer points, String assessmentMatrixId, String pillarId, String categoryId) {
    AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    Pillar pillar = getPillar(assessmentMatrix, pillarId);
    Category category = getCategory(pillar, categoryId);
    Question question = Question.builder()
        .assessmentMatrixId(assessmentMatrix.getId())
        .pillarId(pillar.getId())
        .pillarName(pillar.getName())
        .categoryId(category.getId())
        .categoryName(category.getName())
        .question(questionTxt)
        .questionType(questionType)
        .tenantId(tenantId)
        .points(points)
        .build();
    return setFixedIdIfConfigured(question);
  }

  private Question internalCreateCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId) {
    AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    Pillar pillar = getPillar(assessmentMatrix, pillarId);
    Category category = getCategory(pillar, categoryId);
    Question question = Question.builder()
        .assessmentMatrixId(assessmentMatrix.getId())
        .pillarId(pillar.getId())
        .pillarName(pillar.getName())
        .categoryId(category.getId())
        .categoryName(category.getName())
        .question(questionTxt)
        .questionType(questionType)
        .optionGroup(createOptionGroup(isMultipleChoice, showFlushed, options))
        .tenantId(tenantId)
        .build();
    return setFixedIdIfConfigured(question);
  }

  private OptionGroup createOptionGroup(boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
    OptionGroup optionGroup = OptionGroup.builder()
        .isMultipleChoice(isMultipleChoice)
        .showFlushed(showFlushed)
        .options(options)
        .build();
    return optionGroup;
  }

  private AssessmentMatrix getAssessmentMatrixById(String assessmentMatrixId) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    return assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
  }

  private Pillar getPillar(AssessmentMatrix assessmentMatrix, String pillarId) {
    return assessmentMatrix.getPillarMap().computeIfAbsent(pillarId, s -> {
      throw new InvalidIdReferenceException(pillarId, getClass().getName(), "Pillar");
    });
  }

  private Category getCategory(Pillar pillar, String categoryId) {
    return pillar.getCategoryMap().computeIfAbsent(categoryId, s -> {
      throw new InvalidIdReferenceException(categoryId, getClass().getName(), "Category");
    });
  }

  @Override
  AbstractCrudRepository<Question> getRepository() {
    return questionRepository;
  }
}
