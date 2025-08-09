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
public class PotentialScore implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarIdToPillarScoreMap")}))
  private Map<String, PillarScore> pillarIdToPillarScoreMap;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;
}