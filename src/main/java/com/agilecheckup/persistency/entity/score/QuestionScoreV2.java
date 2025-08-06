package com.agilecheckup.persistency.entity.score;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class QuestionScoreV2 implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("questionId")}))
  private String questionId;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;
}