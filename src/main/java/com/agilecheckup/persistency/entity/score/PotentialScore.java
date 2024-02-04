package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class PotentialScore extends BaseEntity {

  @DynamoDBAttribute
  private Map<String, PillarScore> pillarIdToPillarScoreMap;

  @DynamoDBAttribute
  private Integer maxTotalScore;
}
