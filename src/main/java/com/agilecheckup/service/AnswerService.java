package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.AnswerStrategy;
import com.agilecheckup.persistency.entity.question.AnswerStrategyFactory;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;
import com.agilecheckup.persistency.entity.score.ScoreCalculationStrategyFactory;
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

  public Optional<Answer> create(@NonNull String employeeAssessmentId, @NonNull String questionId,
                                 @NonNull LocalDateTime answeredAt, @NonNull String value, @NonNull String tenantId,
                                 String notes) {
    return super.create(internalCreateAnswer(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes));
  }

  @Override
  public void postCreate(Answer saved) {
    employeeAssessmentService.incrementAnsweredQuestionCount(saved.getEmployeeAssessmentId());
  }

  private Answer internalCreateAnswer(@NonNull String employeeAssessmentId, @NonNull String questionId,
                                      @NonNull LocalDateTime answeredAt, @NonNull String value,
                                      @NonNull String tenantId, String notes) {
    validateAnsweredAt(answeredAt);
    Question question = getQuestionById(questionId);
    // TODO allowNullValue must be fetched from Question
    AnswerStrategy<?> answerStrategy = AnswerStrategyFactory.createStrategy(question, false);
    answerStrategy.assignValue(value);
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);
    EmployeeAssessment employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
    return Answer.builder()
        .employeeAssessmentId(employeeAssessment.getId())
        .pillarId(question.getPillarId())
        .categoryId(question.getCategoryId())
        .questionId(question.getId())
        .questionType(question.getQuestionType())
        .question(question)
        .pendingReview(QuestionType.OPEN_ANSWER.equals(question.getQuestionType()))
        .answeredAt(answeredAt)
        .value(answerStrategy.valueToString())
        .score(scoreCalculator.getCalculatedScore())
        .tenantId(tenantId)
        .notes(notes)
        .build();
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