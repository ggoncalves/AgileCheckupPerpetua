package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixTest {

  @Test
  void testEntityCreation() {
    Map<String, Pillar> pillarMap = new HashMap<>();
    AssessmentConfiguration configuration = AssessmentConfiguration.builder().allowQuestionReview(true).requireAllQuestions(false).autoSave(true).build();

    AssessmentMatrix matrix = AssessmentMatrix.builder().name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").pillarMap(pillarMap).questionCount(5).configuration(configuration).build();

    assertThat(matrix.getName()).isEqualTo("Test Matrix");
    assertThat(matrix.getDescription()).isEqualTo("Test Description");
    assertThat(matrix.getTenantId()).isEqualTo("tenant-123");
    assertThat(matrix.getPerformanceCycleId()).isEqualTo("cycle-123");
    assertThat(matrix.getPillarMap()).isEqualTo(pillarMap);
    assertThat(matrix.getQuestionCount()).isEqualTo(5);
    assertThat(matrix.getConfiguration()).isEqualTo(configuration);
  }

  @Test
  void testDefaultValues() {
    AssessmentMatrix matrix = AssessmentMatrix.builder().name("Test Matrix").description("Test Description").tenantId("tenant-123").performanceCycleId("cycle-123").build();

    assertThat(matrix.getQuestionCount()).isNull();
    assertThat(matrix.getPillarMap()).isNull();
    assertThat(matrix.getPotentialScore()).isNull();
    assertThat(matrix.getConfiguration()).isNull();
  }

  @Test
  void testBuilderPattern() {
    AssessmentMatrix matrix = AssessmentMatrix.builder().name("Builder Test").description("Built with builder").tenantId("tenant-456").performanceCycleId("cycle-456").questionCount(10).build();

    assertThat(matrix).isNotNull();
    assertThat(matrix.getName()).isEqualTo("Builder Test");
    assertThat(matrix.getQuestionCount()).isEqualTo(10);
  }
}