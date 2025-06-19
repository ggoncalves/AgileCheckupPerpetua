package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AnswerService extends AbstractCrudService<Answer, AbstractCrudRepository<Answer>> {

  private final EmployeeAssessmentService employeeAssessmentService;

  private final QuestionService questionService;

  private final AnswerRepository answerRepository;
  
  private final AssessmentMatrixService assessmentMatrixService;

  @Inject
  public AnswerService(AnswerRepository answerRepository, EmployeeAssessmentService employeeAssessmentService, 
                       QuestionService questionService, AssessmentMatrixService assessmentMatrixService) {
    this.answerRepository = answerRepository;
    this.employeeAssessmentService = employeeAssessmentService;
    this.questionService = questionService;
    this.assessmentMatrixService = assessmentMatrixService;
  }

  public Optional<Answer> create(@NonNull String employeeAssessmentId, @NonNull String questionId,
                                 @NonNull LocalDateTime answeredAt, @NonNull String value, @NonNull String tenantId,
                                 String notes) {
    return super.create(internalCreateAnswer(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes));
  }

  public Optional<Answer> update(@NonNull String id, @NonNull LocalDateTime answeredAt, @NonNull String value,
                                 String notes) {
    Optional<Answer> optionalAnswer = findById(id);
    if (optionalAnswer.isPresent()) {
      Answer answer = optionalAnswer.get();
      validateAnsweredAt(answeredAt);
      Question question = getQuestionById(answer.getQuestionId());
      AnswerStrategy<?> answerStrategy = AnswerStrategyFactory.createStrategy(question, false);
      answerStrategy.assignValue(value);
      AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);
      answer.setAnsweredAt(answeredAt);
      answer.setValue(answerStrategy.valueToString());
      answer.setScore(scoreCalculator.getCalculatedScore());
      answer.setNotes(notes);
      return super.update(answer);
    } else {
      return Optional.empty();
    }
  }

  // TODO: It can also trigger an event to start another lambda to actually update and notify HR
  @Override
  public void postCreate(Answer saved) {
    employeeAssessmentService.incrementAnsweredQuestionCount(saved.getEmployeeAssessmentId());
    checkAndUpdateAssessmentStatus(saved.getEmployeeAssessmentId());
  }

  // TODO: It can also trigger an event to start another lambda to actually update and notify HR
  @Override
  public void postUpdate(Answer saved) {
    employeeAssessmentService.incrementAnsweredQuestionCount(saved.getEmployeeAssessmentId());
    checkAndUpdateAssessmentStatus(saved.getEmployeeAssessmentId());
  }
  
  private void checkAndUpdateAssessmentStatus(String employeeAssessmentId) {
    EmployeeAssessment employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
    AssessmentMatrix assessmentMatrix = getAssessmentMatrixById(employeeAssessment.getAssessmentMatrixId());
    
    if (employeeAssessment.getAnsweredQuestionCount() >= assessmentMatrix.getQuestionCount()) {
      employeeAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
      employeeAssessmentService.save(employeeAssessment);
    }
  }
  
  private AssessmentMatrix getAssessmentMatrixById(String assessmentMatrixId) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    return assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
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

  public List<Answer> findByEmployeeAssessmentId(@NonNull String employeeAssessmentId, @NonNull String tenantId) {
    return answerRepository.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
  }

  /**
   * Efficiently retrieves only the question IDs that have been answered for an employee assessment.
   * Performance optimized: Returns only IDs instead of full Answer objects to reduce memory usage
   * and network transfer when checking completion status.
   * 
   * @param employeeAssessmentId The employee assessment ID
   * @param tenantId The tenant ID for data isolation
   * @return Set of question IDs that have been answered
   */
  public Set<String> findAnsweredQuestionIds(@NonNull String employeeAssessmentId, @NonNull String tenantId) {
    return answerRepository.findAnsweredQuestionIds(employeeAssessmentId, tenantId);
  }

}