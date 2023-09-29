package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.AnswerStrategy;
import com.agilecheckup.persistency.entity.question.AnswerStrategyFactory;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.InvalidLocalDateTimeException;
import lombok.NonNull;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

public class AnswerService extends AbstractCrudService<Answer, AbstractCrudRepository<Answer>> {

  private final EmployeeAssessmentService employeeAssessmentService;

  private final QuestionService questionService;

  private final AnswerRepository answerRepository;

  @Inject
  public AnswerService(AnswerRepository answerRepository, EmployeeAssessmentService employeeAssessmentService, QuestionService questionService) {
    this.answerRepository = answerRepository;
    this.employeeAssessmentService = employeeAssessmentService;
    this.questionService = questionService;
  }

  public Optional<Answer> create(@NonNull String employeeAssessmentId, @NonNull String questionId, @NonNull LocalDateTime answeredAt, @NonNull String value, @NonNull String tenantId) {
    return super.create(internalCreateAnswer(employeeAssessmentId, questionId, answeredAt, value, tenantId));
  }

  // Test Cases
  // createWithEmployeeAssessmentId_Invalid
  // createWithEmployeeAssessmentId_Null
  // createWithQuestionId_Invalid
  // createWithQuestionId_Null
  // createWithAnsweredAt_Invalid(?)
  // createWithAnsweredAt_Null
  // createWithTenantId_Invalid
  // createWithTenantId_Null

  // WrongValidation By Question Type
  // createStarThreeWithValue_Invalid
  // createStarThreeWithValue_Null
  // ..
  // all options
  // createCustomizedWithValue_Invalid
  // createCustomizedWithValue_Null

  // createAnswerStarThree
  // createAnswerStarThree_Boundary
  // ...
  // all options
  // createAnswerCustomized_Boundary


  private Answer internalCreateAnswer(@NonNull String employeeAssessmentId, @NonNull String questionId, @NonNull LocalDateTime answeredAt, @NonNull String value, @NonNull String tenantId) {
    validateAnsweredAt(answeredAt);
    Question question = getQuestionById(questionId);
    // TODO allowNullValue must be fetched from Question
    AnswerStrategy answerStrategy = AnswerStrategyFactory.createStrategy(question, false);
    answerStrategy.assignValue(value);
    EmployeeAssessment employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
    Answer answer = Answer.builder()
        .employeeAssessmentId(employeeAssessment.getId())
        .pillarId(question.getPillarId())
        .categoryId(question.getCategoryId())
        .questionId(question.getId())
        .questionType(question.getQuestionType())
        .answeredAt(answeredAt)
        .value(answerStrategy.valueToString())
        .tenantId(tenantId)
        .build();
    return setFixedIdIfConfigured(answer);
  }

  private void validateAnsweredAt(LocalDateTime answeredAt) {
    if (LocalDateTime.now().plusMinutes(60).isBefore(answeredAt)) {
      throw new InvalidLocalDateTimeException(InvalidLocalDateTimeException.InvalidReasonEnum.FUTURE_60_MIN);
    }
  }

  @Override
  AbstractCrudRepository<Answer> getRepository() {
    return answerRepository;
  }

  private Question getQuestionById(String questionId) {
    Optional<Question> question = questionService.findById(questionId);
    return question.orElseThrow(() -> new InvalidIdReferenceException(questionId, getClass().getName(), "Question"));
  }

  private EmployeeAssessment getEmployeeAssessmentById(String employeeAssessmentId) {
    Optional<EmployeeAssessment> employeeAssessment = employeeAssessmentService.findById(employeeAssessmentId);
    return employeeAssessment.orElseThrow(() -> new InvalidIdReferenceException(employeeAssessmentId, getClass().getName(), "EmployeeAssessment"));
  }
}