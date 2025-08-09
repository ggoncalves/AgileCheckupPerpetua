package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.dto.AnswerWithProgressResponse;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.NonNull;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class AssessmentNavigationService {

  private final QuestionService questionService;
  private final AnswerService answerService;
  private final EmployeeAssessmentService employeeAssessmentService;
  private final AssessmentMatrixService assessmentMatrixService;

  @Inject
  public AssessmentNavigationService(QuestionService questionService, AnswerService answerService,
                                     EmployeeAssessmentService employeeAssessmentService,
                                     AssessmentMatrixService assessmentMatrixService) {
    this.questionService = questionService;
    this.answerService = answerService;
    this.employeeAssessmentService = employeeAssessmentService;
    this.assessmentMatrixService = assessmentMatrixService;
  }

  /**
   * Gets the next unanswered question for the employee assessment along with progress information.
   * 
   * This method manages the assessment lifecycle:
   * - Updates status from CONFIRMED to IN_PROGRESS on first call
   * - Updates status to COMPLETED when no more questions remain
   * - Uses the configured navigation mode (typically RANDOM) to select questions
   * 
   * @param employeeAssessmentId The employee assessment ID
   * @param tenantId The tenant ID for data isolation
   * @return AnswerWithProgressResponse containing the next question and progress info
   */
  public AnswerWithProgressResponse getNextUnansweredQuestion(@NonNull String employeeAssessmentId, @NonNull String tenantId) {
    EmployeeAssessment assessment = validateAndUpdateAssessment(employeeAssessmentId);
    AssessmentMatrix matrix = getAssessmentMatrixById(assessment.getAssessmentMatrixId());
    
    Question nextQuestion = selectNextUnansweredQuestion(assessment, matrix, tenantId);
    
    return buildProgressResponse(nextQuestion, assessment, matrix);
  }
  
  /**
   * Validates the assessment exists and updates status to IN_PROGRESS if needed.
   * Clean Code: Single responsibility - validation and status management.
   */
  private EmployeeAssessment validateAndUpdateAssessment(String employeeAssessmentId) {
    EmployeeAssessment assessment = getEmployeeAssessmentById(employeeAssessmentId);
    updateStatusToInProgressIfNeeded(assessment);
    return assessment;
  }
  
  /**
   * Selects the next unanswered question using efficient database queries.
   * Clean Code: Single responsibility - question selection logic.
   */
  private Question selectNextUnansweredQuestion(EmployeeAssessment assessment, AssessmentMatrix matrix, String tenantId) {
    Set<String> answeredQuestionIds = getAnsweredQuestionIds(assessment.getId(), tenantId);
    List<Question> unansweredQuestions = getUnansweredQuestions(matrix.getId(), tenantId, answeredQuestionIds);
    
    if (unansweredQuestions.isEmpty()) {
      updateStatusToCompletedIfNeeded(assessment);
      return null;
    }
    
    AssessmentConfiguration config = assessmentMatrixService.getEffectiveConfiguration(matrix);
    return selectQuestionByNavigationMode(unansweredQuestions, config.getNavigationMode(), assessment.getId());
  }
  
  /**
   * Efficiently retrieves answered question IDs without loading full Answer objects.
   * Performance: Reduces memory usage and network transfer.
   */
  private Set<String> getAnsweredQuestionIds(String employeeAssessmentId, String tenantId) {
    return answerService.findAnsweredQuestionIds(employeeAssessmentId, tenantId);
  }
  
  /**
   * Retrieves unanswered questions by filtering at the database level.
   * Performance: Reduces in-memory processing.
   */
  private List<Question> getUnansweredQuestions(String matrixId, String tenantId, Set<String> answeredQuestionIds) {
    List<Question> allQuestions = questionService.findByAssessmentMatrixId(matrixId, tenantId);
    return allQuestions.stream()
        .filter(question -> !answeredQuestionIds.contains(question.getId()))
        .collect(Collectors.toList());
  }
  
  /**
   * Selects a question based on the navigation mode configuration.
   * Clean Code: Single responsibility - navigation logic.
   */
  private Question selectQuestionByNavigationMode(List<Question> questions, QuestionNavigationType navigationType, String seedId) {
    return selectNextQuestion(questions, navigationType, seedId).orElse(null);
  }
  
  /**
   * Builds the response object with progress information.
   * Clean Code: Single responsibility - response construction.
   */
  private AnswerWithProgressResponse buildProgressResponse(Question question, EmployeeAssessment assessment, AssessmentMatrix matrix) {
    return AnswerWithProgressResponse.builder()
        .question(question)
        .existingAnswer(null) // Always null - reserved for future partial answers feature
        .currentProgress(assessment.getAnsweredQuestionCount())
        .totalQuestions(matrix.getQuestionCount())
        .build();
  }

  /**
   * Selects a question based on navigation type.
   * Clean Code: Simplified method with clear responsibilities.
   */
  private Optional<Question> selectNextQuestion(List<Question> questions, QuestionNavigationType navigationType, String seedId) {
    if (questions.isEmpty()) {
      return Optional.empty();
    }
    
    switch (navigationType) {
      case RANDOM:
        return getRandomQuestion(questions, seedId);
      case SEQUENTIAL:
      case FREE_FORM:
      default:
        return Optional.of(questions.get(0));
    }
  }

  /**
   * Selects a random question using consistent seeding for deterministic results.
   * Clean Code: Clear method name and single responsibility.
   */
  private Optional<Question> getRandomQuestion(List<Question> questions, String seedId) {
    Random random = new Random(seedId.hashCode());
    List<Question> shuffled = new java.util.ArrayList<>(questions);
    Collections.shuffle(shuffled, random);
    return Optional.of(shuffled.get(0));
  }

  private void updateStatusToInProgressIfNeeded(EmployeeAssessment employeeAssessment) {
    if (AssessmentStatus.CONFIRMED.equals(employeeAssessment.getAssessmentStatus())) {
      employeeAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
      employeeAssessmentService.update(employeeAssessment);
    }
  }

  private void updateStatusToCompletedIfNeeded(EmployeeAssessment employeeAssessment) {
    if (!AssessmentStatus.COMPLETED.equals(employeeAssessment.getAssessmentStatus())) {
      employeeAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
      employeeAssessmentService.update(employeeAssessment);
    }
  }

  private EmployeeAssessment getEmployeeAssessmentById(String employeeAssessmentId) {
    Optional<EmployeeAssessment> employeeAssessment = employeeAssessmentService.findById(employeeAssessmentId);
    return employeeAssessment.orElseThrow(() -> new InvalidIdReferenceException(employeeAssessmentId, getClass().getName(), "EmployeeAssessment"));
  }

  private AssessmentMatrix getAssessmentMatrixById(String assessmentMatrixId) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    return assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
  }

  /**
   * Saves an answer and returns the next unanswered question in a single atomic operation.
   * This method combines answer persistence with navigation logic to optimize the assessment flow.
   *
   * @param employeeAssessmentId The employee assessment ID
   * @param questionId           The ID of the question being answered
   * @param answeredAt           The timestamp when the answer was provided
   * @param value                The answer value
   * @param tenantId             The tenant ID for data isolation
   * @param notes                Optional notes for the answer
   * @return AnswerWithProgressResponse containing the saved answer and next question with progress
   */
  public AnswerWithProgressResponse saveAnswerAndGetNext(@NonNull String employeeAssessmentId,
                                                         @NonNull String questionId,
                                                         LocalDateTime answeredAt,
                                                         @NonNull String value,
                                                         @NonNull String tenantId,
                                                         String notes) {
    // Save the answer first
    Optional<Answer> savedAnswer = answerService.create(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes);

    if (savedAnswer.isEmpty()) {
      throw new RuntimeException("Failed to save answer - answerService.create returned empty Optional");
    }

    // Get the next unanswered question with progress
    return getNextUnansweredQuestion(employeeAssessmentId, tenantId);
  }
}