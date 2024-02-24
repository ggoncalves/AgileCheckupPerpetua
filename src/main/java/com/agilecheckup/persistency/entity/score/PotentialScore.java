package com.agilecheckup.persistency.entity.score;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class PotentialScore implements Scorable {

  @DynamoDBAttribute
  private Map<String, PillarScore> pillarIdToPillarScoreMap;

  @DynamoDBAttribute
  private Integer score;
}
