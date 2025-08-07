package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.score.PillarScoreV2;
import com.agilecheckup.persistency.entity.score.Scorable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.Map;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class EmployeeAssessmentScoreV2 implements Scorable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarIdToPillarScoreMap")}))
  private Map<String, PillarScoreV2> pillarIdToPillarScoreMap;
  
  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;

}