package com.agilecheckup.persistency.entity;

import java.util.Map;

import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.Scorable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class EmployeeAssessmentScore implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarIdToPillarScoreMap")}))
  private Map<String, PillarScore> pillarIdToPillarScoreMap;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;

}