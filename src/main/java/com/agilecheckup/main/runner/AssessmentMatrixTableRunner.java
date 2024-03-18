package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AssessmentMatrixService;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Supplier;

@Log4j2
public class AssessmentMatrixTableRunner extends AbstractEntityCrudRunner<AssessmentMatrix> {

  private AssessmentMatrixService assessmentMatrixService;
  private List<Question> questions;

  private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();

  public AssessmentMatrixTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<AssessmentMatrix>>> getCreateSupplier() {
    Map<String, Pillar> pillarMap = tableRunnerHelper.createPillarsWithCategoriesMap();

    Collection<Supplier<Optional<AssessmentMatrix>>> collection = new ArrayList<>();
    collection.add(() -> getAssessmentMatrixService().create(
        "AssessmentMatrixName",
        "AssessmentMatrix Description",
        "Another TenantId",
        "321c5be6-9534-4b7c-9919-2f4418900935",
        pillarMap
    ));
    return collection;
  }

  @Override
  protected AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> getCrudService() {
    return getAssessmentMatrixService();
  }

  @Override
  protected void verifySavedEntity(AssessmentMatrix savedEntity, AssessmentMatrix fetchedEntity) {
    // Do nothing
  }

  private AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }

  private List<Question> getQuestions() {
    if (questions == null) {
      questions = new ArrayList<>();
    }
    return questions;
  }

  @Override
  protected void postCreate(Collection<AssessmentMatrix> entities) {
    entities.forEach(entity -> {
      createDependencies(entity);
      getAssessmentMatrixService().updateCurrentPotentialScore(entity.getId(), entity.getTenantId());
    });
  }

  private void createDependencies(AssessmentMatrix entity) {
    Pillar pillar = entity.getPillarMap().values().iterator().next();
    Category category = pillar.getCategoryMap().values().iterator().next();
    tableRunnerHelper.getQuestionService().create(
        "Pergunta oficial",
        QuestionType.STAR_THREE,
        entity.getTenantId(),
        15d,
        entity.getId(),
        pillar.getId(),
        category.getId()
    ).ifPresent(question -> getQuestions().add(question));
  }

  @Override
  protected void deleteDependencies() {
    super.deleteDependencies();
    getQuestions().forEach(question -> tableRunnerHelper.getQuestionService().delete(question));
  }

}
