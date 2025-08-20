package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentConfigurationTest {

  @Test
  void shouldCreateWithDefaultValues() {
    // When
    AssessmentConfiguration configuration = AssessmentConfiguration.builder().build();

    // Then
    assertThat(configuration.getAllowQuestionReview()).isTrue();
    assertThat(configuration.getRequireAllQuestions()).isTrue();
    assertThat(configuration.getAutoSave()).isTrue();
    assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
  }

  @Test
  void shouldCreateWithCustomValues() {
    // When
    AssessmentConfiguration configuration = AssessmentConfiguration.builder()
                                                                   .allowQuestionReview(false)
                                                                   .requireAllQuestions(false)
                                                                   .autoSave(false)
                                                                   .navigationMode(QuestionNavigationType.SEQUENTIAL)
                                                                   .build();

    // Then
    assertThat(configuration.getAllowQuestionReview()).isFalse();
    assertThat(configuration.getRequireAllQuestions()).isFalse();
    assertThat(configuration.getAutoSave()).isFalse();
    assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.SEQUENTIAL);
  }

  @Test
  void shouldCreateWithPartialCustomValues() {
    // When
    AssessmentConfiguration configuration = AssessmentConfiguration.builder()
                                                                   .allowQuestionReview(false)
                                                                   .navigationMode(QuestionNavigationType.FREE_FORM)
                                                                   .build();

    // Then
    assertThat(configuration.getAllowQuestionReview()).isFalse();
    assertThat(configuration.getRequireAllQuestions()).isTrue(); // default
    assertThat(configuration.getAutoSave()).isTrue(); // default
    assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.FREE_FORM);
  }

  @Test
  void shouldSupportAllNavigationModes() {
    // Given
    QuestionNavigationType[] modes = {QuestionNavigationType.RANDOM, QuestionNavigationType.SEQUENTIAL, QuestionNavigationType.FREE_FORM
    };

    for (QuestionNavigationType mode : modes) {
      // When
      AssessmentConfiguration configuration = AssessmentConfiguration.builder().navigationMode(mode).build();

      // Then
      assertThat(configuration.getNavigationMode()).isEqualTo(mode);
    }
  }

  @Test
  void shouldCreateWithNoArgsConstructor() {
    // When
    AssessmentConfiguration configuration = new AssessmentConfiguration();

    // Then - Lombok @Builder.Default applies defaults even with no-args constructor
    assertThat(configuration.getAllowQuestionReview()).isTrue();
    assertThat(configuration.getRequireAllQuestions()).isTrue();
    assertThat(configuration.getAutoSave()).isTrue();
    assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
  }

  @Test
  void shouldCreateWithAllArgsConstructor() {
    // When
    AssessmentConfiguration configuration = new AssessmentConfiguration(
                                                                        false, true, false, QuestionNavigationType.SEQUENTIAL);

    // Then
    assertThat(configuration.getAllowQuestionReview()).isFalse();
    assertThat(configuration.getRequireAllQuestions()).isTrue();
    assertThat(configuration.getAutoSave()).isFalse();
    assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.SEQUENTIAL);
  }

  @Test
  void shouldSupportDataAnnotationMethods() {
    // Given
    AssessmentConfiguration configuration1 = AssessmentConfiguration.builder()
                                                                    .allowQuestionReview(true)
                                                                    .requireAllQuestions(false)
                                                                    .autoSave(true)
                                                                    .navigationMode(QuestionNavigationType.RANDOM)
                                                                    .build();

    AssessmentConfiguration configuration2 = AssessmentConfiguration.builder()
                                                                    .allowQuestionReview(true)
                                                                    .requireAllQuestions(false)
                                                                    .autoSave(true)
                                                                    .navigationMode(QuestionNavigationType.RANDOM)
                                                                    .build();

    AssessmentConfiguration configuration3 = AssessmentConfiguration.builder()
                                                                    .allowQuestionReview(false)
                                                                    .requireAllQuestions(false)
                                                                    .autoSave(true)
                                                                    .navigationMode(QuestionNavigationType.RANDOM)
                                                                    .build();

    // Then
    assertThat(configuration1).isEqualTo(configuration2);
    assertThat(configuration1).isNotEqualTo(configuration3);
    assertThat(configuration1.hashCode()).isEqualTo(configuration2.hashCode());
    assertThat(configuration1.toString()).isNotNull().contains("AssessmentConfiguration");
  }
}