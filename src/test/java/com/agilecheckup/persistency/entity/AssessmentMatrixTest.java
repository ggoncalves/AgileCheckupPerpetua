package com.agilecheckup.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static com.agilecheckup.util.TestObjectFactory.createCustomAssessmentConfiguration;
import static com.agilecheckup.util.TestObjectFactory.createDefaultAssessmentConfiguration;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixTest {

    @Test
    void shouldCreateAssessmentMatrixWithConfiguration() {
        // Given
        AssessmentConfiguration configuration = createDefaultAssessmentConfiguration();
        Map<String, Pillar> pillarMap = createMockedPillarMap(2, 3, "Pillar", "Category");

        // When
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(pillarMap)
            .configuration(configuration)
            .build();

        // Then
        assertThat(matrix.getName()).isEqualTo("Test Matrix");
        assertThat(matrix.getDescription()).isEqualTo("Test Description");
        assertThat(matrix.getTenantId()).isEqualTo("tenant-123");
        assertThat(matrix.getPerformanceCycleId()).isEqualTo("cycle-456");
        assertThat(matrix.getPillarMap()).isEqualTo(pillarMap);
        assertThat(matrix.getConfiguration()).isEqualTo(configuration);
        assertThat(matrix.getConfiguration().getAllowQuestionReview()).isTrue();
        assertThat(matrix.getConfiguration().getNavigationMode()).isEqualTo(QuestionNavigationType.RANDOM);
    }

    @Test
    void shouldCreateAssessmentMatrixWithoutConfiguration() {
        // Given
        Map<String, Pillar> pillarMap = createMockedPillarMap(1, 2, "Pillar", "Category");

        // When
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(pillarMap)
            .build();

        // Then
        assertThat(matrix.getName()).isEqualTo("Test Matrix");
        assertThat(matrix.getDescription()).isEqualTo("Test Description");
        assertThat(matrix.getTenantId()).isEqualTo("tenant-123");
        assertThat(matrix.getPerformanceCycleId()).isEqualTo("cycle-456");
        assertThat(matrix.getPillarMap()).isEqualTo(pillarMap);
        assertThat(matrix.getConfiguration()).isNull(); // Nullable field
    }

    @Test
    void shouldCreateAssessmentMatrixWithCustomConfiguration() {
        // Given
        AssessmentConfiguration configuration = createCustomAssessmentConfiguration(
            false, true, false, QuestionNavigationType.SEQUENTIAL);
        Map<String, Pillar> pillarMap = new HashMap<>();

        // When
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Custom Matrix")
            .description("Custom Description")
            .tenantId("tenant-789")
            .performanceCycleId("cycle-abc")
            .pillarMap(pillarMap)
            .configuration(configuration)
            .questionCount(5)
            .build();

        // Then
        assertThat(matrix.getName()).isEqualTo("Custom Matrix");
        assertThat(matrix.getConfiguration()).isNotNull();
        assertThat(matrix.getConfiguration().getAllowQuestionReview()).isFalse();
        assertThat(matrix.getConfiguration().getRequireAllQuestions()).isTrue();
        assertThat(matrix.getConfiguration().getAutoSave()).isFalse();
        assertThat(matrix.getConfiguration().getNavigationMode()).isEqualTo(QuestionNavigationType.SEQUENTIAL);
        assertThat(matrix.getQuestionCount()).isEqualTo(5);
    }

    @Test
    void shouldSupportSettingConfigurationAfterCreation() {
        // Given
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(new HashMap<>())
            .build();

        AssessmentConfiguration configuration = createCustomAssessmentConfiguration(
            true, false, true, QuestionNavigationType.FREE_FORM);

        // When
        matrix.setConfiguration(configuration);

        // Then
        assertThat(matrix.getConfiguration()).isEqualTo(configuration);
        assertThat(matrix.getConfiguration().getAllowQuestionReview()).isTrue();
        assertThat(matrix.getConfiguration().getRequireAllQuestions()).isFalse();
        assertThat(matrix.getConfiguration().getAutoSave()).isTrue();
        assertThat(matrix.getConfiguration().getNavigationMode()).isEqualTo(QuestionNavigationType.FREE_FORM);
    }

    @Test
    void shouldSupportNullConfiguration() {
        // Given
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(new HashMap<>())
            .configuration(null)
            .build();

        // When & Then
        assertThat(matrix.getConfiguration()).isNull();

        // Should be able to set to null again
        matrix.setConfiguration(null);
        assertThat(matrix.getConfiguration()).isNull();
    }

    @Test
    void shouldMaintainBackwardCompatibilityWithDefaultQuestionCount() {
        // When
        AssessmentMatrix matrix = AssessmentMatrix.builder()
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(new HashMap<>())
            .build();

        // Then
        assertThat(matrix.getQuestionCount()).isEqualTo(0); // Default value
        assertThat(matrix.getConfiguration()).isNull(); // Nullable
        assertThat(matrix.getPotentialScore()).isNull(); // Other existing nullable field
    }

    @Test
    void shouldSupportDataAnnotationMethods() {
        // Given
        AssessmentConfiguration config1 = createDefaultAssessmentConfiguration();
        AssessmentConfiguration config2 = createDefaultAssessmentConfiguration();
        Map<String, Pillar> pillarMap = new HashMap<>();

        AssessmentMatrix matrix1 = AssessmentMatrix.builder()
            .id("matrix-1")
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(pillarMap)
            .configuration(config1)
            .questionCount(0)
            .build();

        AssessmentMatrix matrix2 = AssessmentMatrix.builder()
            .id("matrix-1") // Same ID for equality
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(pillarMap)
            .configuration(config2)
            .questionCount(0)
            .build();

        AssessmentMatrix matrix3 = AssessmentMatrix.builder()
            .id("matrix-2") // Different ID
            .name("Test Matrix")
            .description("Test Description")
            .tenantId("tenant-123")
            .performanceCycleId("cycle-456")
            .pillarMap(pillarMap)
            .configuration(config1)
            .questionCount(0)
            .build();

        // Then
        assertThat(matrix1).isEqualTo(matrix2);
        assertThat(matrix1).isNotEqualTo(matrix3);
        assertThat(matrix1.hashCode()).isEqualTo(matrix2.hashCode());
        assertThat(matrix1.toString()).isNotNull().contains("AssessmentMatrix");
    }
}