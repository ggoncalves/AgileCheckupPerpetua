package com.agilecheckup.persistency.entity.question;

import com.agilecheckup.persistency.entity.QuestionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {

  private static final String EMPLOYEE_ASSESSMENT_ID = "employeeAssessmentId";
  private static final String PILLAR_ID = "pillarId";
  private static final String CATEGORY_ID = "categoryId";
  private static final String QUESTION_ID = "questionId";
  private static final String TENANT_ID = "tenantId";
  private static final String ANY_VALUE = "value";
  private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2024, 12, 12, 6, 0, 0);
  private static final Question STAR_FIVE_QUESTION = createMockedQuestion("questionId", QuestionType.STAR_FIVE);
  private static final Question OPEN_ANSWER_QUESTION = createMockedQuestion("questionIdOpen", QuestionType.OPEN_ANSWER);


  @Test
  void shouldCreateAnswerForStarFiveWithNoPendingReview() {
    Answer answer = Answer.builder()
        .id(GENERIC_ID_1234)
        .employeeAssessmentId(EMPLOYEE_ASSESSMENT_ID)
        .pillarId(PILLAR_ID)
        .categoryId(CATEGORY_ID)
        .questionId(QUESTION_ID)
        .question(STAR_FIVE_QUESTION)
        .questionType(STAR_FIVE_QUESTION.getQuestionType())
        .answeredAt(ANSWERED_AT)
        .value(ANY_VALUE)
        .tenantId(TENANT_ID)
        .build();

    assertEquals(GENERIC_ID_1234, answer.getId());
    assertEquals(EMPLOYEE_ASSESSMENT_ID, answer.getEmployeeAssessmentId());
    assertEquals(PILLAR_ID, answer.getPillarId());
    assertEquals(CATEGORY_ID, answer.getCategoryId());
    assertEquals(QUESTION_ID, answer.getQuestionId());
    assertEquals(STAR_FIVE_QUESTION, answer.getQuestion());
    assertEquals(STAR_FIVE_QUESTION.getQuestionType(), answer.getQuestionType());
    assertEquals(ANSWERED_AT, answer.getAnsweredAt());
    assertEquals(ANY_VALUE, answer.getValue());
    assertEquals(TENANT_ID, answer.getTenantId());

    // Verifying custom builder fields
//    assertFalse(answer.isPendingReview());
    assertNull(answer.getReviewer());
  }

  @Test
  void shouldCreateAnswerForOpenAnswerWithPendingReview() {
    Answer answer = Answer.builder()
        .id(GENERIC_ID_1234)
        .employeeAssessmentId(EMPLOYEE_ASSESSMENT_ID)
        .pillarId(PILLAR_ID)
        .categoryId(CATEGORY_ID)
        .questionId(QUESTION_ID)
        .question(OPEN_ANSWER_QUESTION)
        .questionType(OPEN_ANSWER_QUESTION.getQuestionType())
        .answeredAt(ANSWERED_AT)
        .pendingReview(true)
        .value(ANY_VALUE)
        .tenantId(TENANT_ID)
        .build();

    assertEquals(GENERIC_ID_1234, answer.getId());
    assertEquals(EMPLOYEE_ASSESSMENT_ID, answer.getEmployeeAssessmentId());
    assertEquals(PILLAR_ID, answer.getPillarId());
    assertEquals(CATEGORY_ID, answer.getCategoryId());
    assertEquals(QUESTION_ID, answer.getQuestionId());
    assertEquals(OPEN_ANSWER_QUESTION, answer.getQuestion());
    assertEquals(OPEN_ANSWER_QUESTION.getQuestionType(), answer.getQuestionType());
    assertEquals(ANSWERED_AT, answer.getAnsweredAt());
    assertEquals(ANY_VALUE, answer.getValue());
    assertEquals(TENANT_ID, answer.getTenantId());

    // Verifying custom builder fields
    assertTrue(answer.isPendingReview());
    assertNull(answer.getReviewer());
  }
}