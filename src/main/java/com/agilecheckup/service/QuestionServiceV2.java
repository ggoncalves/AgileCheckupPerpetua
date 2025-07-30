package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.repository.QuestionRepositoryV2;
import com.agilecheckup.service.exception.InvalidCustomOptionListException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.INVALID_OPTIONS_IDS;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_EMPTY;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TEXT_EMPTY;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_BIG;
import static com.agilecheckup.service.exception.InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_SHORT;

public class QuestionServiceV2 extends AbstractCrudServiceV2<QuestionV2, QuestionRepositoryV2> {

  private static final Integer MIN_CUSTOM_OPTIONS_SIZE = 2;
  private static final Integer MAX_CUSTOM_OPTIONS_SIZE = 64;

  private final QuestionRepositoryV2 questionRepository;
  private final AssessmentMatrixServiceV2 assessmentMatrixService;

  @Inject
  public QuestionServiceV2(QuestionRepositoryV2 questionRepository, AssessmentMatrixServiceV2 assessmentMatrixService) {
    this.questionRepository = questionRepository;
    this.assessmentMatrixService = assessmentMatrixService;
  }

  public Optional<QuestionV2> create(String questionTxt, QuestionType questionType, String tenantId, Double points,
                                   String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    QuestionV2 question = internalCreateQuestion(questionTxt, questionType, tenantId, points, assessmentMatrixId, pillarId, categoryId, extraDescription);
    return createQuestion(question);
  }

  public Optional<QuestionV2> createCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options, String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    QuestionV2 question = internalCreateCustomQuestion(questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options, assessmentMatrixId, pillarId, categoryId, extraDescription);
    return createQuestion(question);
  }

  public Optional<QuestionV2> update(String id, String questionTxt, QuestionType questionType, String tenantId, Double points,
                                   String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Optional<QuestionV2> optionalQuestion = findById(id);
    if (optionalQuestion.isPresent()) {
      QuestionV2 question = optionalQuestion.get();
      AssessmentMatrixV2 assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
      PillarV2 pillar = getPillar(assessmentMatrix, pillarId);
      CategoryV2 category = getCategory(pillar, categoryId);
      
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
    } else {
      return Optional.empty();
    }
  }

  public Optional<QuestionV2> updateCustomQuestion(String id, String questionTxt, QuestionType questionType, String tenantId,
                                                 boolean isMultipleChoice, boolean showFlushed, @NonNull List<QuestionOption> options,
                                                 String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    Optional<QuestionV2> optionalQuestion = findById(id);
    if (optionalQuestion.isPresent()) {
      QuestionV2 question = optionalQuestion.get();
      validateQuestionOptions(options);
      AssessmentMatrixV2 assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
      PillarV2 pillar = getPillar(assessmentMatrix, pillarId);
      CategoryV2 category = getCategory(pillar, categoryId);
      
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
    } else {
      return Optional.empty();
    }
  }

  public List<QuestionV2> findByAssessmentMatrixId(String matrixId, String tenantId) {
    return questionRepository.findByAssessmentMatrixId(matrixId, tenantId);
  }

  public List<QuestionV2> findAllByTenantId(String tenantId) {
    return findAll().stream()
        .filter(question -> tenantId.equals(question.getTenantId()))
        .collect(Collectors.toList());
  }

  public boolean hasCategoryQuestions(String matrixId, String categoryId, String tenantId) {
    return questionRepository.existsByCategoryId(matrixId, categoryId, tenantId);
  }

  private Optional<QuestionV2> createQuestion(QuestionV2 question) {
    Optional<QuestionV2> savedQuestion = super.create(question);
    postCreate(savedQuestion);
    return savedQuestion;
  }

  private void postCreate(Optional<QuestionV2> question) {
    question.ifPresent(q -> assessmentMatrixService.incrementQuestionCount(q.getAssessmentMatrixId()));
  }

  private QuestionV2 internalCreateQuestion(String questionTxt, QuestionType questionType, String tenantId, Double points,
                                          String assessmentMatrixId, String pillarId, String categoryId, String extraDescription) {
    AssessmentMatrixV2 assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    PillarV2 pillar = getPillar(assessmentMatrix, pillarId);
    CategoryV2 category = getCategory(pillar, categoryId);
    return QuestionV2.builder()
        .assessmentMatrixId(assessmentMatrix.getId())
        .pillarId(pillar.getId())
        .pillarName(pillar.getName())
        .categoryId(category.getId())
        .categoryName(category.getName())
        .question(questionTxt)
        .questionType(questionType)
        .tenantId(tenantId)
        .points(points)
        .extraDescription(extraDescription)
        .build();
  }

  private QuestionV2 internalCreateCustomQuestion(String questionTxt, QuestionType questionType, String tenantId,
                                                boolean isMultipleChoice, boolean showFlushed,
                                                @NonNull List<QuestionOption> options, String assessmentMatrixId,
                                                String pillarId, String categoryId, String extraDescription) {
    validateQuestionOptions(options);
    AssessmentMatrixV2 assessmentMatrix = getAssessmentMatrixById(assessmentMatrixId);
    PillarV2 pillar = getPillar(assessmentMatrix, pillarId);
    CategoryV2 category = getCategory(pillar, categoryId);
    return QuestionV2.builder()
        .assessmentMatrixId(assessmentMatrix.getId())
        .pillarId(pillar.getId())
        .pillarName(pillar.getName())
        .categoryId(category.getId())
        .categoryName(category.getName())
        .question(questionTxt)
        .questionType(questionType)
        .optionGroup(createOptionGroup(isMultipleChoice, showFlushed, options))
        .tenantId(tenantId)
        .extraDescription(extraDescription)
        .build();
  }

  private void validateQuestionOptions(List<QuestionOption> options) {
    validateOptionListSize(options);
    options = sortOptionsById(options);
    validateOptions(options);
  }

  private List<QuestionOption> sortOptionsById(List<QuestionOption> options) {
    return options.stream()
        .sorted(Comparator.comparing(QuestionOption::getId))
        .collect(Collectors.toList());
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
      Integer[] optionIds = options.stream()
          .map(QuestionOption::getId)
          .toArray(Integer[]::new);
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
    return OptionGroup.builder()
        .isMultipleChoice(isMultipleChoice)
        .showFlushed(showFlushed)
        .optionMap(toOptionMap(options))
        .build();
  }

  @VisibleForTesting
  Map<Integer, QuestionOption> toOptionMap(List<QuestionOption> options) {
    return options.stream().collect(Collectors.toMap(QuestionOption::getId, Function.identity()));
  }

  private AssessmentMatrixV2 getAssessmentMatrixById(String assessmentMatrixId) {
    Optional<AssessmentMatrixV2> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    return assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
  }

  private PillarV2 getPillar(AssessmentMatrixV2 assessmentMatrix, String pillarId) {
    return assessmentMatrix.getPillarMap().computeIfAbsent(pillarId, s -> {
      throw new InvalidIdReferenceException(pillarId, getClass().getName(), "Pillar");
    });
  }

  private CategoryV2 getCategory(PillarV2 pillar, String categoryId) {
    return pillar.getCategoryMap().computeIfAbsent(categoryId, s -> {
      throw new InvalidIdReferenceException(categoryId, getClass().getName(), "Category");
    });
  }

  public void delete(QuestionV2 question) {
    if (question != null) {
      deleteById(question.getId());
      if (question.getAssessmentMatrixId() != null) {
        assessmentMatrixService.decrementQuestionCount(question.getAssessmentMatrixId());
      }
    }
  }

  @Override
  QuestionRepositoryV2 getRepository() {
    return questionRepository;
  }
}