package com.agilecheckup.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AssessmentConfigurationV2Test {

    @Test
    void shouldCreateWithDefaultValues() {
        // When
        AssessmentConfigurationV2 configuration = AssessmentConfigurationV2.builder().build();

        // Then
        assertThat(configuration.getAllowQuestionReview()).isTrue();
        assertThat(configuration.getRequireAllQuestions()).isTrue();
        assertThat(configuration.getAutoSave()).isTrue();
        assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
    }

    @Test
    void shouldCreateWithCustomValues() {
        // When
        AssessmentConfigurationV2 configuration = AssessmentConfigurationV2.builder()
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
        AssessmentConfigurationV2 configuration = AssessmentConfigurationV2.builder()
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
        QuestionNavigationType[] modes = {
            QuestionNavigationType.RANDOM,
            QuestionNavigationType.SEQUENTIAL,
            QuestionNavigationType.FREE_FORM
        };

        for (QuestionNavigationType mode : modes) {
            // When
            AssessmentConfigurationV2 configuration = AssessmentConfigurationV2.builder()
                .navigationMode(mode)
                .build();

            // Then
            assertThat(configuration.getNavigationMode()).isEqualTo(mode);
        }
    }

    @Test
    void shouldCreateWithNoArgsConstructor() {
        // When
        AssessmentConfigurationV2 configuration = new AssessmentConfigurationV2();

        // Then - Lombok @Builder.Default applies defaults even with no-args constructor
        assertThat(configuration.getAllowQuestionReview()).isTrue();
        assertThat(configuration.getRequireAllQuestions()).isTrue();
        assertThat(configuration.getAutoSave()).isTrue();
        assertThat(configuration.getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        // When
        AssessmentConfigurationV2 configuration = new AssessmentConfigurationV2(
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
        AssessmentConfigurationV2 configuration1 = AssessmentConfigurationV2.builder()
            .allowQuestionReview(true)
            .requireAllQuestions(false)
            .autoSave(true)
            .navigationMode(QuestionNavigationType.RANDOM)
            .build();

        AssessmentConfigurationV2 configuration2 = AssessmentConfigurationV2.builder()
            .allowQuestionReview(true)
            .requireAllQuestions(false)
            .autoSave(true)
            .navigationMode(QuestionNavigationType.RANDOM)
            .build();

        AssessmentConfigurationV2 configuration3 = AssessmentConfigurationV2.builder()
            .allowQuestionReview(false)
            .requireAllQuestions(false)
            .autoSave(true)
            .navigationMode(QuestionNavigationType.RANDOM)
            .build();

        // Then
        assertThat(configuration1).isEqualTo(configuration2);
        assertThat(configuration1).isNotEqualTo(configuration3);
        assertThat(configuration1.hashCode()).isEqualTo(configuration2.hashCode());
        assertThat(configuration1.toString()).isNotNull().contains("AssessmentConfigurationV2");
    }
}