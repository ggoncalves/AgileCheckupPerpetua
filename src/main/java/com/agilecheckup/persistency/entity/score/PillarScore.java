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
public class PillarScore extends BaseEntity {

  @DynamoDBAttribute
  private String pillarId;

  @DynamoDBAttribute
  private String pillarName;

  @DynamoDBAttribute
  private Map<String, CategoryScore> categoryIdToCategoryScoreMap;

  @DynamoDBAttribute
  private Integer maxPillarScore;

}
