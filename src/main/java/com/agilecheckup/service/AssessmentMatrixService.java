package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
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
import com.google.common.annotations.VisibleForTesting;
import dagger.Lazy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AssessmentMatrixService extends AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> {

  private final AssessmentMatrixRepository assessmentMatrixRepository;

  private final PerformanceCycleService performanceCycleService;

  private final Lazy<QuestionService> questionService;

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
    return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap));
  }

  public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                           Map<String, Pillar> pillarMap) {
    Optional<AssessmentMatrix> optionalAssessmentMatrix = findById(id);
    if (optionalAssessmentMatrix.isPresent()) {
      AssessmentMatrix assessmentMatrix = optionalAssessmentMatrix.get();
      Optional<PerformanceCycle> performanceCycle = getPerformanceCycle(performanceCycleId);
      
      assessmentMatrix.setName(name);
      assessmentMatrix.setDescription(description);
      assessmentMatrix.setTenantId(tenantId);
      assessmentMatrix.setPerformanceCycleId(performanceCycle.orElseThrow(() -> new InvalidIdReferenceException(performanceCycleId, "AssessmentMatrix", "PerformanceCycle")).getId());
      assessmentMatrix.setPillarMap(pillarMap);
      return super.update(assessmentMatrix);
    } else {
      return Optional.empty();
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

  private AssessmentMatrix createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId,
                                                  Map<String, Pillar> pillarMap) {
    return AssessmentMatrix.builder()
        .name(name)
        .description(description)
        .tenantId(tenantId)
        .performanceCycleId(getPerformanceCycle(performanceCycleId).orElseThrow(() -> new InvalidIdReferenceException(performanceCycleId, "AssessmentMatrix", "PerformanceCycle")).getId())
        .pillarMap(pillarMap).build();
  }

  private Optional<PerformanceCycle> getPerformanceCycle(String performanceCycleId) {
    if (performanceCycleId == null) return Optional.empty();
    return performanceCycleService.findById(performanceCycleId);
  }

  @Override
  AbstractCrudRepository<AssessmentMatrix> getRepository() {
    return assessmentMatrixRepository;
  }

  public AssessmentMatrix updateCurrentPotentialScore(String matrixId, String tenantId) {
    List<Question> questions = getQuestionService().findByAssessmentMatrixId(matrixId, tenantId);

    // Map pillarId -> PillarScore
    Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

    // 2. Calcula a pontuacao total possivelmente máxima das questões recuperadas
    double totalPoints = questions.stream()
        .mapToDouble(question -> {
          // Recupera ou cria o PillarScore
          PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
              PillarScore.builder()
                  .pillarId(id)
                  .pillarName(question.getPillarName())
                  .categoryIdToCategoryScoreMap(new HashMap<>())
                  .build()
          );

          // Roda lógica suposta para atualizar PillarScore e se necessario CategoryScore com base na questão
          updatePillarScoreWithQuestion(pillarScore, question);

          return computeQuestionMaxScore(question);
        })
        .sum();

    // 3.1 Recupere o AssessmentMatrix
    AssessmentMatrix assessmentMatrix = assessmentMatrixRepository.findById(matrixId);

    // 3.2 Atualize o PotentialScore do AssessmentMatrix
    PotentialScore potentialScore =  assessmentMatrix.getPotentialScore();
    if (potentialScore == null) {
      potentialScore = PotentialScore.builder().build();
      assessmentMatrix.setPotentialScore(potentialScore);
    }
    potentialScore.setPillarIdToPillarScoreMap(pillarIdToPillarScoreMap);
    potentialScore.setScore(totalPoints);

    // 4. Salve o AssessmentMatrix atualizado
    assessmentMatrixRepository.save(assessmentMatrix);

    return assessmentMatrix;
  }

  private void updatePillarScoreWithQuestion(PillarScore pillarScore, Question question) {
    // Supondo que cada CategoryScore pode ser identificado pelo ID da categoria na questão
    String categoryId = question.getCategoryId();
    CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap().computeIfAbsent(categoryId, id ->
        CategoryScore.builder()
            .categoryId(id)
            .categoryName(question.getCategoryName())
            .questionScores(new ArrayList<>())
            .build()
    );

    // Crie e adicione QuestionScore à lista de questionScores na CategoryScore
    QuestionScore questionScore = QuestionScore.builder()
        .questionId(question.getId())
        .score(computeQuestionMaxScore(question))
        .build();
    categoryScore.getQuestionScores().add(questionScore);

    // Atualiza o maxCategoryScore na CategoryScore
    categoryScore.setScore(categoryScore.getQuestionScores().stream()
        .mapToDouble(QuestionScore::getScore)
        .sum());

    // Atualiza o maxPillarScore no PillarScore
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