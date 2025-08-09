package com.agilecheckup.persistency.entity.score;

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
  protected final String value;
  
  protected final QuestionV2 questionV2;

  public abstract Double getCalculatedScore();
  
  /**
   * Helper method to get points from question entity.
   */
  protected Double getQuestionPoints() {
    if (questionV2 != null) {
      return questionV2.getPoints();
    } else {
      throw new IllegalStateException("questionV2 is null");
    }
  }

}
