package com.agilecheckup.persistency.entity.score;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class CategoryScore implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryId")}))
  private String categoryId;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryName")}))
  private String categoryName;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("questionScores")}))
  private List<QuestionScore> questionScores;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;
}