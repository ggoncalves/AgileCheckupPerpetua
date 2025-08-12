package com.agilecheckup.persistency.entity.score;

import java.util.Map;

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
public class PillarScore implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarId")}))
  private String pillarId;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarName")}))
  private String pillarName;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryIdToCategoryScoreMap")}))
  private Map<String, CategoryScore> categoryIdToCategoryScoreMap;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;
}