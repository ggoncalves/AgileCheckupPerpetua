package com.agilecheckup.persistency.entity.score;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class PillarScoreV2 implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarId")}))
  private String pillarId;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarName")}))
  private String pillarName;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryIdToCategoryScoreMap")}))
  private Map<String, CategoryScoreV2> categoryIdToCategoryScoreMap;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;
}