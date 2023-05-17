package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;

public class TestObjectFactory {

  public static final String QUESTION_ID_1234 = "1234";

  public static Question createMockedQuestion() {
    return Question.builder()
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question createMockedQuestion(String id) {
    return Question.builder()
        .id(id)
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question copyQuestionAndAddId(Question question, String id) {
    return Question.builder()
        .id(id)
        .question(question.getQuestion())
        .rateType(question.getRateType())
        .tenantId(question.getTenantId())
        .points(question.getPoints())
        .build();
  }
}
