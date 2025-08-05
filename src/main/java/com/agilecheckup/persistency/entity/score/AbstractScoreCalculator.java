package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@Getter
public abstract class AbstractScoreCalculator {

  @NonNull
  final String value;

  final Question question;
  
  final QuestionV2 questionV2;

  public abstract Double getCalculatedScore();
  
  /**
   * Helper method to get points from either V1 or V2 question entity.
   */
  protected Double getQuestionPoints() {
    if (questionV2 != null) {
      return questionV2.getPoints();
    } else if (question != null) {
      return question.getPoints();
    } else {
      throw new IllegalStateException("Both question and questionV2 are null");
    }
  }


}
