package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.service.AnswerServiceV2;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.QuestionServiceV2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

public class TableRunnerHelper {

  private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;
  private QuestionServiceV2 questionServiceV2;
  private AnswerServiceV2 answerServiceV2;
  private EmployeeAssessmentServiceV2 employeeAssessmentServiceV2;

  Map<String, PillarV2> createPillarsWithCategoriesMapV2() {
    CategoryV2 c1 = CategoryV2.builder()
        .name("Categoria de Nome 1")
        .description("Categoria de Descrição 1")
        .build();

    CategoryV2 c2 = CategoryV2.builder()
        .name("Categoria de Nome 2")
        .description("Categoria de Descrição 2")
        .build();

    Map<String, CategoryV2> categoryMap = ImmutableMap.of(c1.getId(), c1, c2.getId(), c2);

    PillarV2 p1 = PillarV2.builder()
        .name("Pillar name 1")
        .description("Pillar description 1 ")
        .categoryMap(categoryMap)
        .build();

    CategoryV2 c3 = CategoryV2.builder()
        .name("Categoria de Nome 3")
        .description("Categoria de Descrição 3")
        .build();

    CategoryV2 c4 = CategoryV2.builder()
        .name("Categoria de Nome 4")
        .description("Categoria de Descrição 4")
        .build();

    Map<String, CategoryV2> categoryMap2 = ImmutableMap.of(c3.getId(), c3, c4.getId(), c4);

    PillarV2 p2 = PillarV2.builder()
        .name("Pillar name 2")
        .description("Pillar description 2 ")
        .categoryMap(categoryMap2)
        .build();

    Set<PillarV2> pillars = ImmutableSet.of(p1, p2);
    return createPillarsMapV2(pillars);
  }

  private Map<String, PillarV2> createPillarsMapV2(Set<PillarV2> pillars) {
    ImmutableMap.Builder<String, PillarV2> builder = ImmutableMap.builder();
    for (PillarV2 pillar : pillars) {
      builder.put(pillar.getId(), pillar);
    }
    return builder.build();
  }

  AssessmentMatrixServiceV2 getAssessmentMatrixServiceV2() {
    if (assessmentMatrixServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixServiceV2 = serviceComponent.buildAssessmentMatrixServiceV2();
    }
    return assessmentMatrixServiceV2;
  }

  QuestionServiceV2 getQuestionServiceV2() {
    if (questionServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionServiceV2 = serviceComponent.buildQuestionServiceV2();
    }
    return questionServiceV2;
  }

  AnswerServiceV2 getAnswerServiceV2() {
    if (answerServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerServiceV2 = serviceComponent.buildAnswerServiceV2();
    }
    return answerServiceV2;
  }

  EmployeeAssessmentServiceV2 getEmployeeAssessmentServiceV2() {
    if (employeeAssessmentServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentServiceV2 = serviceComponent.buildEmployeeAssessmentServiceV2();
    }
    return employeeAssessmentServiceV2;
  }

}