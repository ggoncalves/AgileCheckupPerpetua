package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.AnswerV2;
import com.agilecheckup.persistency.entity.question.strategy.AnswerStrategy;
import com.agilecheckup.persistency.entity.question.strategy.AnswerStrategyFactory;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;
import com.agilecheckup.persistency.entity.score.strategy.ScoreCalculationStrategyFactory;
import com.agilecheckup.persistency.repository.AnswerRepositoryV2;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.InvalidLocalDateTimeException;
import lombok.NonNull;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AnswerServiceV2 extends AbstractCrudServiceV2<AnswerV2, AnswerRepositoryV2> {

    private final EmployeeAssessmentServiceV2 employeeAssessmentService;
    private final QuestionServiceV2 questionService;
    private final AnswerRepositoryV2 answerRepository;
    private final AssessmentMatrixServiceV2 assessmentMatrixService;

    @Inject
    public AnswerServiceV2(AnswerRepositoryV2 answerRepository, 
                          EmployeeAssessmentServiceV2 employeeAssessmentService, 
                          QuestionServiceV2 questionService, 
                          AssessmentMatrixServiceV2 assessmentMatrixService) {
        this.answerRepository = answerRepository;
        this.employeeAssessmentService = employeeAssessmentService;
        this.questionService = questionService;
        this.assessmentMatrixService = assessmentMatrixService;
    }

    public Optional<AnswerV2> create(@NonNull String employeeAssessmentId, @NonNull String questionId,
                                    LocalDateTime answeredAt, @NonNull String value, @NonNull String tenantId,
                                    String notes) {
        // Check for existing answer to prevent duplicates
        Optional<AnswerV2> existingAnswer = answerRepository.findByEmployeeAssessmentIdAndQuestionId(
            employeeAssessmentId, questionId, tenantId);
        
        if (existingAnswer.isPresent()) {
            // Update existing answer instead of creating duplicate
            return updateExistingAnswer(existingAnswer.get(), answeredAt, value, notes);
        } else {
            // Create new answer
            return super.create(internalCreateAnswer(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes));
        }
    }

    public Optional<AnswerV2> update(@NonNull String id, @NonNull LocalDateTime answeredAt, @NonNull String value,
                                    String notes) {
        Optional<AnswerV2> optionalAnswer = findById(id);
        if (optionalAnswer.isPresent()) {
            AnswerV2 answer = optionalAnswer.get();
            validateAnsweredAt(answeredAt);
            QuestionV2 question = getQuestionById(answer.getQuestionId());
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

    /**
     * Updates an existing answer when duplicate prevention is triggered.
     * This method is called when a create operation detects an existing answer
     * for the same employeeAssessmentId + questionId combination.
     * 
     * @param existingAnswer The existing answer to update
     * @param answeredAt The new timestamp
     * @param value The new value
     * @param notes The new notes
     * @return Optional containing the updated answer
     */
    private Optional<AnswerV2> updateExistingAnswer(@NonNull AnswerV2 existingAnswer, @NonNull LocalDateTime answeredAt, 
                                                   @NonNull String value, String notes) {
        validateAnsweredAt(answeredAt);
        QuestionV2 question = getQuestionById(existingAnswer.getQuestionId());
        AnswerStrategy<?> answerStrategy = AnswerStrategyFactory.createStrategy(question, false);
        answerStrategy.assignValue(value);
        AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);
        
        existingAnswer.setAnsweredAt(answeredAt);
        existingAnswer.setValue(answerStrategy.valueToString());
        existingAnswer.setScore(scoreCalculator.getCalculatedScore());
        existingAnswer.setNotes(notes);
        
        return super.update(existingAnswer);
    }

    @Override
    public void postCreate(AnswerV2 saved) {
        employeeAssessmentService.incrementAnsweredQuestionCount(saved.getEmployeeAssessmentId());
        updateLastActivityIfNotCompleted(saved.getEmployeeAssessmentId());
        checkAndUpdateAssessmentStatus(saved.getEmployeeAssessmentId());
    }

    @Override
    public void postUpdate(AnswerV2 saved) {
        employeeAssessmentService.incrementAnsweredQuestionCount(saved.getEmployeeAssessmentId());
        updateLastActivityIfNotCompleted(saved.getEmployeeAssessmentId());
        checkAndUpdateAssessmentStatus(saved.getEmployeeAssessmentId());
    }
    
    private void checkAndUpdateAssessmentStatus(String employeeAssessmentId) {
        EmployeeAssessmentV2 employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
        AssessmentMatrixV2 assessmentMatrix = getAssessmentMatrixById(employeeAssessment.getAssessmentMatrixId());
        
        if (employeeAssessment.getAnsweredQuestionCount() >= assessmentMatrix.getQuestionCount()) {
            AssessmentStatus previousStatus = employeeAssessment.getAssessmentStatus();
            employeeAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
            
            // Set completion timestamp - this will be the final lastActivityDate
            if (previousStatus != AssessmentStatus.COMPLETED) {
                employeeAssessment.setLastActivityDate(new Date());
            }
            
            employeeAssessmentService.save(employeeAssessment);
            
            // Automatically calculate score when assessment becomes COMPLETED
            if (previousStatus != AssessmentStatus.COMPLETED) {
                employeeAssessmentService.updateEmployeeAssessmentScore(employeeAssessmentId, employeeAssessment.getTenantId());
            }
        }
    }
    
    private AssessmentMatrixV2 getAssessmentMatrixById(String assessmentMatrixId) {
        Optional<AssessmentMatrixV2> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
        return assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
    }

    private AnswerV2 internalCreateAnswer(@NonNull String employeeAssessmentId, @NonNull String questionId,
                                         @NonNull LocalDateTime answeredAt, @NonNull String value,
                                         @NonNull String tenantId, String notes) {
        validateAnsweredAt(answeredAt);
        QuestionV2 question = getQuestionById(questionId);
        AnswerStrategy<?> answerStrategy = AnswerStrategyFactory.createStrategy(question, false);
        answerStrategy.assignValue(value);
        AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);
        EmployeeAssessmentV2 employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
        return AnswerV2.builder()
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
    AnswerRepositoryV2 getRepository() {
        return answerRepository;
    }

    private QuestionV2 getQuestionById(String questionId) {
        Optional<QuestionV2> question = questionService.findById(questionId);
        return question.orElseThrow(() -> new InvalidIdReferenceException(questionId, getClass().getName(), "Question"));
    }

    private EmployeeAssessmentV2 getEmployeeAssessmentById(String employeeAssessmentId) {
        Optional<EmployeeAssessmentV2> employeeAssessment = employeeAssessmentService.findById(employeeAssessmentId);
        return employeeAssessment.orElseThrow(() -> new InvalidIdReferenceException(employeeAssessmentId, getClass().getName(), "EmployeeAssessment"));
    }

    public List<AnswerV2> findByEmployeeAssessmentId(@NonNull String employeeAssessmentId, @NonNull String tenantId) {
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

    /**
     * Updates the lastActivityDate for an employee assessment only if it's not COMPLETED.
     * Once an assessment is COMPLETED, the lastActivityDate should not be modified.
     * 
     * @param employeeAssessmentId The employee assessment ID to update
     */
    private void updateLastActivityIfNotCompleted(@NonNull String employeeAssessmentId) {
        EmployeeAssessmentV2 employeeAssessment = getEmployeeAssessmentById(employeeAssessmentId);
        
        // Only update lastActivityDate if assessment is not completed
        if (employeeAssessment.getAssessmentStatus() != AssessmentStatus.COMPLETED) {
            employeeAssessmentService.updateLastActivityDate(employeeAssessmentId);
        }
    }

}