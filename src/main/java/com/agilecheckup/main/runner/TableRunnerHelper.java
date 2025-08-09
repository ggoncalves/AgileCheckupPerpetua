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

  private AssessmentMatrixService assessmentMatrixServiceV2;
  private QuestionService questionServiceV2;
  private AnswerService answerServiceV2;
  private EmployeeAssessmentService employeeAssessmentServiceV2;

  Map<String, Pillar> createPillarsWithCategoriesMapV2() {
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
    return createPillarsMapV2(pillars);
  }

  private Map<String, Pillar> createPillarsMapV2(Set<Pillar> pillars) {
    ImmutableMap.Builder<String, Pillar> builder = ImmutableMap.builder();
    for (Pillar pillar : pillars) {
      builder.put(pillar.getId(), pillar);
    }
    return builder.build();
  }

  AssessmentMatrixService getAssessmentMatrixServiceV2() {
    if (assessmentMatrixServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixServiceV2 = serviceComponent.buildAssessmentMatrixServiceV2();
    }
    return assessmentMatrixServiceV2;
  }

  QuestionService getQuestionServiceV2() {
    if (questionServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionServiceV2 = serviceComponent.buildQuestionServiceV2();
    }
    return questionServiceV2;
  }

  AnswerService getAnswerServiceV2() {
    if (answerServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerServiceV2 = serviceComponent.buildAnswerServiceV2();
    }
    return answerServiceV2;
  }

  EmployeeAssessmentService getEmployeeAssessmentServiceV2() {
    if (employeeAssessmentServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentServiceV2 = serviceComponent.buildEmployeeAssessmentServiceV2();
    }
    return employeeAssessmentServiceV2;
  }

}