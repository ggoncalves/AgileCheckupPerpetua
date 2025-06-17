package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.ValidationException;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.google.common.annotations.VisibleForTesting;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssessmentMatrixService extends AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> {

  private final AssessmentMatrixRepository assessmentMatrixRepository;

  private final PerformanceCycleService performanceCycleService;

  private final Lazy<QuestionService> questionService;

  private static final String DEFAULT_WHEN_NULL = "";

  @Inject
  public AssessmentMatrixService(AssessmentMatrixRepository assessmentMatrixRepository,
                                 PerformanceCycleService performanceCycleService,
                                 Lazy<QuestionService> questionService) {
    this.assessmentMatrixRepository = assessmentMatrixRepository;
    this.performanceCycleService = performanceCycleService;
    this.questionService = questionService;
  }

  @VisibleForTesting
  protected QuestionService getQuestionService() {
    return questionService.get();
  }

  public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId,
                                           Map<String, Pillar> pillarMap) {
    return create(name, description, tenantId, performanceCycleId, pillarMap, null);
  }

  public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId,
                                           Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap, configuration));
  }

  public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                           Map<String, Pillar> pillarMap) {
    return update(id, name, description, tenantId, performanceCycleId, pillarMap, null);
  }

  public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                           Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    Optional<AssessmentMatrix> optionalAssessmentMatrix = findById(id);
    if (optionalAssessmentMatrix.isPresent()) {
      AssessmentMatrix assessmentMatrix = optionalAssessmentMatrix.get();
      
      // Validate that no categories with questions are being removed
      validateCategoryDeletion(assessmentMatrix, pillarMap, tenantId);
      
      Optional<PerformanceCycle> performanceCycle = getPerformanceCycle(performanceCycleId);
      
      assessmentMatrix.setName(name);
      assessmentMatrix.setDescription(StringUtils.defaultIfBlank(description, DEFAULT_WHEN_NULL));
      assessmentMatrix.setTenantId(tenantId);
      assessmentMatrix.setPerformanceCycleId(performanceCycle.orElseThrow(() -> new InvalidIdReferenceException(performanceCycleId, "AssessmentMatrix", "PerformanceCycle")).getId());
      assessmentMatrix.setPillarMap(pillarMap);
      assessmentMatrix.setConfiguration(configuration);
      return super.update(assessmentMatrix);
    } else {
      return Optional.empty();
    }
  }

  private void validateCategoryDeletion(AssessmentMatrix currentMatrix, Map<String, Pillar> newPillarMap, String tenantId) {
    Map<String, Pillar> currentPillarMap = currentMatrix.getPillarMap();
    
    // Check for removed pillars
    for (Map.Entry<String, Pillar> currentPillarEntry : currentPillarMap.entrySet()) {
      String pillarId = currentPillarEntry.getKey();
      Pillar currentPillar = currentPillarEntry.getValue();
      
      if (!newPillarMap.containsKey(pillarId)) {
        // Pillar is being removed - check all its categories for questions
        for (Map.Entry<String, Category> categoryEntry : currentPillar.getCategoryMap().entrySet()) {
          String categoryId = categoryEntry.getKey();
          if (getQuestionService().hasCategoryQuestions(currentMatrix.getId(), categoryId, tenantId)) {
            throw new ValidationException("Cannot delete pillar '" + currentPillar.getName() + "' because it contains categories with questions");
          }
        }
      } else {
        // Pillar exists - check for removed categories
        Pillar newPillar = newPillarMap.get(pillarId);
        for (Map.Entry<String, Category> currentCategoryEntry : currentPillar.getCategoryMap().entrySet()) {
          String categoryId = currentCategoryEntry.getKey();
          Category currentCategory = currentCategoryEntry.getValue();
          
          if (!newPillar.getCategoryMap().containsKey(categoryId)) {
            // Category is being removed - check for questions
            if (getQuestionService().hasCategoryQuestions(currentMatrix.getId(), categoryId, tenantId)) {
              throw new ValidationException("Cannot delete category '" + currentCategory.getName() + "' because it contains questions");
            }
          }
        }
      }
    }
  }

  public AssessmentMatrix incrementQuestionCount(String matrixId) {
    assessmentMatrixRepository.performLocked(matrixId, () -> {
      AssessmentMatrix matrix = getRepository().findById(matrixId);
      if (matrix != null) {
        matrix.setQuestionCount(matrix.getQuestionCount() + 1);
        getRepository().save(matrix);
      }
    });
    return getRepository().findById(matrixId);
  }

  /**
   * Creates a default assessment configuration with RANDOM navigation mode
   * and standard settings for new assessments.
   */
  public AssessmentConfiguration createDefaultConfiguration() {
    return AssessmentConfiguration.builder()
        .allowQuestionReview(true)
        .requireAllQuestions(true)
        .autoSave(true)
        .navigationMode(QuestionNavigationType.RANDOM)
        .build();
  }

  /**
   * Gets the configuration for an assessment matrix, returning default if null
   */
  public AssessmentConfiguration getEffectiveConfiguration(AssessmentMatrix assessmentMatrix) {
    return assessmentMatrix.getConfiguration() != null
        ? assessmentMatrix.getConfiguration()
        : createDefaultConfiguration();
  }

  private AssessmentMatrix createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId,
                                                  Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    return AssessmentMatrix.builder()
        .name(name)
        .description(StringUtils.defaultIfBlank(description, DEFAULT_WHEN_NULL))
        .tenantId(tenantId)
        .performanceCycleId(getPerformanceCycle(performanceCycleId).orElseThrow(() -> new InvalidIdReferenceException(performanceCycleId, "AssessmentMatrix", "PerformanceCycle")).getId())
        .pillarMap(pillarMap)
        .configuration(configuration)
        .build();
  }

  private Optional<PerformanceCycle> getPerformanceCycle(String performanceCycleId) {
    if (performanceCycleId == null) return Optional.empty();
    return performanceCycleService.findById(performanceCycleId);
  }

  public List<AssessmentMatrix> findAllByTenantId(String tenantId) {
    PaginatedQueryList<AssessmentMatrix> paginatedList = assessmentMatrixRepository.findAllByTenantId(tenantId);
    return paginatedList.stream().collect(Collectors.toList());
  }

  @Override
  protected AbstractCrudRepository<AssessmentMatrix> getRepository() {
    return assessmentMatrixRepository;
  }

  public AssessmentMatrix updateCurrentPotentialScore(String matrixId, String tenantId) {
    List<Question> questions = getQuestionService().findByAssessmentMatrixId(matrixId, tenantId);

    // Map pillarId -> PillarScore
    Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

    // 2. Calculate the maximum possible total score from the retrieved questions
    double totalPoints = questions.stream()
        .mapToDouble(question -> {
          // Retrieve or create the PillarScore
          PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
              PillarScore.builder()
                  .pillarId(id)
                  .pillarName(question.getPillarName())
                  .categoryIdToCategoryScoreMap(new HashMap<>())
                  .build()
          );

          // Run the logic to update PillarScore and if necessary CategoryScore based on the question
          updatePillarScoreWithQuestion(pillarScore, question);

          return computeQuestionMaxScore(question);
        })
        .sum();

    // 3.1 Retrieve the AssessmentMatrix
    AssessmentMatrix assessmentMatrix = assessmentMatrixRepository.findById(matrixId);

    // 3.2 Update the PotentialScore of the AssessmentMatrix
    PotentialScore potentialScore =  assessmentMatrix.getPotentialScore();
    if (potentialScore == null) {
      potentialScore = PotentialScore.builder().build();
      assessmentMatrix.setPotentialScore(potentialScore);
    }
    potentialScore.setPillarIdToPillarScoreMap(pillarIdToPillarScoreMap);
    potentialScore.setScore(totalPoints);

    // 4. Save the updated AssessmentMatrix
    assessmentMatrixRepository.save(assessmentMatrix);

    return assessmentMatrix;
  }

  private void updatePillarScoreWithQuestion(PillarScore pillarScore, Question question) {
    // Assuming each CategoryScore can be identified by the category ID in the question
    String categoryId = question.getCategoryId();
    CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap().computeIfAbsent(categoryId, id ->
        CategoryScore.builder()
            .categoryId(id)
            .categoryName(question.getCategoryName())
            .questionScores(new ArrayList<>())
            .build()
    );

    // Create and add QuestionScore to the questionScores list in CategoryScore
    QuestionScore questionScore = QuestionScore.builder()
        .questionId(question.getId())
        .score(computeQuestionMaxScore(question))
        .build();
    categoryScore.getQuestionScores().add(questionScore);

    // Update the maxCategoryScore in CategoryScore
    categoryScore.setScore(categoryScore.getQuestionScores().stream()
        .mapToDouble(QuestionScore::getScore)
        .sum());

    // Update the maxPillarScore in PillarScore
    pillarScore.setScore(pillarScore.getCategoryIdToCategoryScoreMap().values().stream()
        .mapToDouble(CategoryScore::getScore)
        .sum());
  }

  @VisibleForTesting
  Double computeQuestionMaxScore(Question question) {
    if (QuestionType.CUSTOMIZED.equals(question.getQuestionType())) {
      if (question.getOptionGroup().isMultipleChoice()) {
        return question.getOptionGroup().getOptionMap().values().stream()
            .mapToDouble(QuestionOption::getPoints)
            .sum();
      } else {
        return question.getOptionGroup().getOptionMap().values().stream()
            .mapToDouble(QuestionOption::getPoints)
            .max().orElse(0);
      }
    } else {
      return question.getPoints();
    }
  }
}