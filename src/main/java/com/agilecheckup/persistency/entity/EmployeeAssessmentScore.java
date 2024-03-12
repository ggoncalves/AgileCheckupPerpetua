package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.Scorable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class EmployeeAssessmentScore implements Scorable {

  @DynamoDBAttribute
  private Map<String, PillarScore> pillarIdToPillarScoreMap;

  @DynamoDBAttribute
  private Integer score;

}