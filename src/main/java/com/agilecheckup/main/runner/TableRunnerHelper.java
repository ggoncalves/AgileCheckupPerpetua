package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.QuestionService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

public class TableRunnerHelper {

  private AssessmentMatrixService assessmentMatrixService;
  private QuestionService questionService;
  private AnswerService answerService;
  private EmployeeAssessmentService employeeAssessmentService;

  Map<String, Pillar> createPillarsWithCategoriesMap() {
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
    return createPillarsMap(pillars);
  }

  private Map<String, Pillar> createPillarsMap(Set<Pillar> pillars) {
    ImmutableMap.Builder<String, Pillar> builder = ImmutableMap.builder();
    for (Pillar pillar : pillars) {
      builder.put(pillar.getId(), pillar);
    }
    return builder.build();
  }

  AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }

  QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }

  AnswerService getAnswerService() {
    if (answerService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerService = serviceComponent.buildAnswerService();
    }
    return answerService;
  }

  EmployeeAssessmentService getEmployeeAssessmentService() {
    if (employeeAssessmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentService = serviceComponent.buildEmployeeAssessmentService();
    }
    return employeeAssessmentService;
  }

}