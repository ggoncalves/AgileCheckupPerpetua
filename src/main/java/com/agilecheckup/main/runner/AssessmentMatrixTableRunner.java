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
import com.agilecheckup.service.QuestionService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Supplier;

@Log4j2
public class AssessmentMatrixTableRunner extends AbstractEntityCrudRunner<AssessmentMatrix> {

  private AssessmentMatrixService assessmentMatrixService;
  private QuestionService questionService;

  private List<Question> questions;

  public AssessmentMatrixTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<AssessmentMatrix>>> getCreateSupplier() {
    Category c1 = Category.builder()
        .name("Categoria de Nome 1")
        .description("Categoria de Descrição 1")
        .build();

    Category c2 = Category.builder()
        .name("Categoria de Nome 2")
        .description("Categoria de Descrição 2")
        .build();

    Map<String, Category> categoryMap = ImmutableMap.of(c1.getId(), c1, c2.getId(), c2);

    Pillar p1 = Pillar.builder()
        .name("Pillar name 1")
        .description("Pillar description 1 ")
        .categoryMap(categoryMap)
        .build();

    Category c3 = Category.builder()
        .name("Categoria de Nome 3")
        .description("Categoria de Descrição 3")
        .build();

    Category c4 = Category.builder()
        .name("Categoria de Nome 4")
        .description("Categoria de Descrição 4")
        .build();

    Map<String, Category> categoryMap2 = ImmutableMap.of(c3.getId(), c3, c4.getId(), c4);

    Pillar p2 = Pillar.builder()
        .name("Pillar name 2")
        .description("Pillar description 2 ")
        .categoryMap(categoryMap2)
        .build();

    Set<Pillar> pillars = ImmutableSet.of(p1, p2);
    Map<String, Pillar> pillarMap = ImmutableMap.of(p1.getId(), p1, p2.getId(), p2);

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

  private QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
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
    getQuestionService().create(
        "Pergunta oficial",
        QuestionType.STAR_THREE,
        entity.getTenantId(),
        15,
        entity.getId(),
        pillar.getId(),
        category.getId()
    ).ifPresent(question -> getQuestions().add(question));
  }

  @Override
  protected void deleteDependencies() {
    super.deleteDependencies();
    getQuestions().forEach(question -> getQuestionService().delete(question));
  }

}
