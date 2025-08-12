package com.agilecheckup.service;

import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.INVALID_OPTIONS_IDS;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_EMPTY;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TEXT_EMPTY;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_BIG;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_SHORT;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.service.exception.InvalidCustomOptionListException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;

public class QuestionService extends AbstractCrudService<Question, QuestionRepository> {

  private static final Integer MIN_CUSTOM_OPTIONS_SIZE = 2;
  private static final Integer MAX_CUSTOM_OPTIONS_SIZE = 64;

  private final QuestionRepository questionRepository;
  private final AssessmentMatrixService assessmentMatrixService;

  @Inject
  public QuestionService(QuestionRepository questionRepository, AssessmentMatrixService assessmentMatrixService) {
    this.questionRepository = questionRepository;
    this.assessmentMatrixService = assessmentMatrixService;
  }

  public Optional<Question> create(String questionTxt, QuestionType questionType, String tenantId, Double points, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Question question = internalCreateQuestion(questionTxt, questionType, tenantId, points, assessmentMatrixId, pillarId, categoryId, extraDescription);
    return createQuestion(question);
  }

  public Optional<Question> createCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Question question = internalCreateCustomQuestion(questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options, assessmentMatrixId, pillarId, categoryId, extraDescription);
    return createQuestion(question);
  }

  public Optional<Question> update(String id, String questionTxt, QuestionType questionType, String tenantId, Double points, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Optional<Question> optionalQuestion = findById(id);
    if (optionalQuestion.isPresent()) {
      Question question = optionalQuestion.get();
      AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
      Pillar pillar = getPillar(assessmentMatrix, pillarId);
      Category category = getCategory(pillar, categoryId);

      question.setAssessmentMatrixId(assessmentMatrix.getId());
      question.setPillarId(pillar.getId());
      question.setPillarName(pillar.getName());
      question.setCategoryId(category.getId());
      question.setCategoryName(category.getName());
      question.setQuestion(questionTxt);
      question.setQuestionType(questionType);
      question.setTenantId(tenantId);
      question.setPoints(points);
      question.setExtraDescription(extraDescription);
      return super.update(question);
    }
    else {
      return Optional.empty();
    }
  }

  public Optional<Question> updateCustomQuestion(String id, String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, @NonNull List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Optional<Question> optionalQuestion = findById(id);
    if (optionalQuestion.isPresent()) {
      Question question = optionalQuestion.get();
      validateQuestionOptions(options);
      AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
      Pillar pillar = getPillar(assessmentMatrix, pillarId);
      Category category = getCategory(pillar, categoryId);

      question.setAssessmentMatrixId(assessmentMatrix.getId());
      question.setPillarId(pillar.getId());
      question.setPillarName(pillar.getName());
      question.setCategoryId(category.getId());
      question.setCategoryName(category.getName());
      question.setQuestion(questionTxt);
      question.setQuestionType(questionType);
      question.setOptionGroup(createOptionGroup(isMultipleChoice, showFlushed, options));
      question.setTenantId(tenantId);
      question.setExtraDescription(extraDescription);
      return super.update(question);
    }
    else {
      return Optional.empty();
    }
  }

  public List<Question> findByAssessmentMatrixId(String matrixId, String tenantId) {
    return questionRepository.findByAssessmentMatrixId(matrixId, tenantId);
  }

  public List<Question> findAllByTenantId(String tenantId) {
    return findAll().stream().filter(question -> tenantId.equals(question.getTenantId())).collect(Collectors.toList());
  }

  public boolean hasCategoryQuestions(String matrixId, String categoryId, String tenantId) {
    return questionRepository.existsByCategoryId(matrixId, categoryId, tenantId);
  }

  private Optional<Question> createQuestion(Question question) {
    Optional<Question> savedQuestion = super.create(question);
    postCreate(savedQuestion);
    return savedQuestion;
  }

  private void postCreate(Optional<Question> question) {
    question.ifPresent(q -> assessmentMatrixService.incrementQuestionCount(q.getAssessmentMatrixId()));
  }

  private Question internalCreateQuestion(String questionTxt, QuestionType questionType, String tenantId, Double points, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    Pillar pillar = getPillar(assessmentMatrix, pillarId);
    Category category = getCategory(pillar, categoryId);
    return Question.builder().assessmentMatrixId(assessmentMatrix.getId()).pillarId(pillar.getId()).pillarName(pillar.getName()).categoryId(category.getId()).categoryName(category.getName()).question(questionTxt).questionType(questionType).tenantId(tenantId).points(points).extraDescription(extraDescription).build();
  }

  private Question internalCreateCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, @NonNull List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    validateQuestionOptions(options);
    AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    Pillar pillar = getPillar(assessmentMatrix, pillarId);
    Category category = getCategory(pillar, categoryId);
    return Question.builder().assessmentMatrixId(assessmentMatrix.getId()).pillarId(pillar.getId()).pillarName(pillar.getName()).categoryId(category.getId()).categoryName(category.getName()).question(questionTxt).questionType(questionType).optionGroup(createOptionGroup(isMultipleChoice, showFlushed, options)).tenantId(tenantId).extraDescription(extraDescription).build();
  }

  private void validateQuestionOptions(List<QuestionOption> options) {
    validateOptionListSize(options);
    options = sortOptionsById(options);
    validateOptions(options);
  }

  private List<QuestionOption> sortOptionsById(List<QuestionOption> options) {
    return options.stream().sorted(Comparator.comparing(QuestionOption::getId)).collect(Collectors.toList());
  }

  private void validateOptions(List<QuestionOption> options) {
    Integer expectedId = 1;
    for (QuestionOption option : options) {
      validateOptionText(option);
      validateOptionId(option, expectedId, options);
      expectedId++;
    }
  }

  private void validateOptionText(QuestionOption option) {
    if (option.getText().isEmpty()) {
      throw new InvalidCustomOptionListException(OPTION_LIST_TEXT_EMPTY.getReason());
    }
  }

  private void validateOptionId(QuestionOption option, Integer expectedId, List<QuestionOption> options) {
    if (!option.getId().equals(expectedId)) {
      Integer[] optionIds = options.stream().map(QuestionOption::getId).toArray(Integer[]::new);
      throw new InvalidCustomOptionListException(INVALID_OPTIONS_IDS, optionIds);
    }
  }

  private void validateOptionListSize(List<QuestionOption> options) {
    if (isOptionListEmpty(options)) {
      throw new InvalidCustomOptionListException(OPTION_LIST_EMPTY, MIN_CUSTOM_OPTIONS_SIZE, MAX_CUSTOM_OPTIONS_SIZE);
    }
    if (isOptionListTooShort(options)) {
      throw new InvalidCustomOptionListException(OPTION_LIST_TOO_SHORT, MIN_CUSTOM_OPTIONS_SIZE, MAX_CUSTOM_OPTIONS_SIZE);
    }
    if (isOptionListTooBig(options)) {
      throw new InvalidCustomOptionListException(OPTION_LIST_TOO_BIG, MIN_CUSTOM_OPTIONS_SIZE, MAX_CUSTOM_OPTIONS_SIZE);
    }
  }

  private boolean isOptionListEmpty(List<QuestionOption> options) {
    return options.isEmpty();
  }

  private boolean isOptionListTooShort(List<QuestionOption> options) {
    return options.size() < MIN_CUSTOM_OPTIONS_SIZE;
  }

  private boolean isOptionListTooBig(List<QuestionOption> options) {
    return options.size() > MAX_CUSTOM_OPTIONS_SIZE;
  }

  private OptionGroup createOptionGroup(boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
    return OptionGroup.builder().isMultipleChoice(isMultipleChoice).showFlushed(showFlushed).optionMap(toOptionMap(options)).build();
  }

  @VisibleForTesting
  Map<Integer, QuestionOption> toOptionMap(List<QuestionOption> options) {
    return options.stream().collect(Collectors.toMap(QuestionOption::getId, Function.identity()));
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

  public void delete(Question question) {
    if (question != null) {
      deleteById(question.getId());
      if (question.getAssessmentMatrixId() != null) {
        assessmentMatrixService.decrementQuestionCount(question.getAssessmentMatrixId());
      }
    }
  }

  @Override
  QuestionRepository getRepository() {
    return questionRepository;
  }
}