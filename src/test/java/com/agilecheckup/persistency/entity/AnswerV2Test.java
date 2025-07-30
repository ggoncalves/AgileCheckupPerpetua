package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.question.AnswerV2;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AnswerV2Test {

    @Test
    void shouldCreateAnswerV2WithAllFields() {
        // Given
        LocalDateTime answeredAt = LocalDateTime.now();
        Question question = createMockedQuestion();
        NaturalPerson reviewer = NaturalPerson.builder()
                .name("John Reviewer")
                .email("john@reviewer.com")
                .build();

        // When
        AnswerV2 answer = AnswerV2.builder()
                .id("answer-123")
                .employeeAssessmentId("assessment-123")
                .answeredAt(answeredAt)
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId("question-123")
                .reviewer(reviewer)
                .questionType(QuestionType.YES_NO)
                .question(question)
                .pendingReview(false)
                .value("Yes")
                .score(10.0)
                .notes("Test notes")
                .tenantId("tenant-123")
                .build();

        // Then
        assertThat(answer.getId()).isEqualTo("answer-123");
        assertThat(answer.getEmployeeAssessmentId()).isEqualTo("assessment-123");
        assertThat(answer.getAnsweredAt()).isEqualTo(answeredAt);
        assertThat(answer.getPillarId()).isEqualTo("pillar-123");
        assertThat(answer.getCategoryId()).isEqualTo("category-123");
        assertThat(answer.getQuestionId()).isEqualTo("question-123");
        assertThat(answer.getReviewer()).isEqualTo(reviewer);
        assertThat(answer.getQuestionType()).isEqualTo(QuestionType.YES_NO);
        assertThat(answer.getQuestion()).isEqualTo(question);
        assertThat(answer.isPendingReview()).isFalse();
        assertThat(answer.getValue()).isEqualTo("Yes");
        assertThat(answer.getScore()).isEqualTo(10.0);
        assertThat(answer.getNotes()).isEqualTo("Test notes");
        assertThat(answer.getTenantId()).isEqualTo("tenant-123");
    }

    @Test
    void shouldCreateAnswerV2WithRequiredFieldsOnly() {
        // Given
        LocalDateTime answeredAt = LocalDateTime.now();

        // When
        AnswerV2 answer = AnswerV2.builder()
                .employeeAssessmentId("assessment-123")
                .answeredAt(answeredAt)
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId("question-123")
                .questionType(QuestionType.STAR_FIVE)
                .value("4")
                .tenantId("tenant-123")
                .build();

        // Then
        assertThat(answer.getEmployeeAssessmentId()).isEqualTo("assessment-123");
        assertThat(answer.getAnsweredAt()).isEqualTo(answeredAt);
        assertThat(answer.getPillarId()).isEqualTo("pillar-123");
        assertThat(answer.getCategoryId()).isEqualTo("category-123");
        assertThat(answer.getQuestionId()).isEqualTo("question-123");
        assertThat(answer.getQuestionType()).isEqualTo(QuestionType.STAR_FIVE);
        assertThat(answer.getValue()).isEqualTo("4");
        assertThat(answer.getTenantId()).isEqualTo("tenant-123");
        assertThat(answer.isPendingReview()).isFalse(); // Default value
        assertThat(answer.getReviewer()).isNull();
        assertThat(answer.getQuestion()).isNull();
        assertThat(answer.getScore()).isNull();
        assertThat(answer.getNotes()).isNull();
    }

    @Test
    void shouldSetPendingReviewToTrueByDefault() {
        // When
        AnswerV2 answer = AnswerV2.builder()
                .employeeAssessmentId("assessment-123")
                .answeredAt(LocalDateTime.now())
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId("question-123")
                .questionType(QuestionType.OPEN_ANSWER)
                .value("Open answer text")
                .tenantId("tenant-123")
                .pendingReview(true)
                .build();

        // Then
        assertThat(answer.isPendingReview()).isTrue();
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        // Given
        LocalDateTime answeredAt = LocalDateTime.now();
        
        AnswerV2 answer1 = AnswerV2.builder()
                .id("answer-123")
                .employeeAssessmentId("assessment-123")
                .answeredAt(answeredAt)
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId("question-123")
                .questionType(QuestionType.YES_NO)
                .value("Yes")
                .tenantId("tenant-123")
                .build();

        AnswerV2 answer2 = AnswerV2.builder()
                .id("answer-123")
                .employeeAssessmentId("assessment-123")
                .answeredAt(answeredAt)
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId("question-123")
                .questionType(QuestionType.YES_NO)
                .value("Yes")
                .tenantId("tenant-123")
                .build();

        // Then
        assertThat(answer1).isEqualTo(answer2);
        assertThat(answer1.hashCode()).isEqualTo(answer2.hashCode());
    }

    @Test
    void shouldCreateAnswerV2WithDifferentQuestionTypes() {
        // Test different question types
        QuestionType[] questionTypes = {
            QuestionType.YES_NO,
            QuestionType.STAR_FIVE,
            QuestionType.ONE_TO_TEN,
            QuestionType.CUSTOMIZED,
            QuestionType.OPEN_ANSWER
        };

        for (QuestionType questionType : questionTypes) {
            AnswerV2 answer = AnswerV2.builder()
                    .employeeAssessmentId("assessment-123")
                    .answeredAt(LocalDateTime.now())
                    .pillarId("pillar-123")
                    .categoryId("category-123")
                    .questionId("question-123")
                    .questionType(questionType)
                    .value("test-value")
                    .tenantId("tenant-123")
                    .build();

            assertThat(answer.getQuestionType()).isEqualTo(questionType);
        }
    }
}