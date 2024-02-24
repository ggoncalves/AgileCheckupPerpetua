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
public class PillarScore implements Scorable {

  @DynamoDBAttribute
  private String pillarId;

  @DynamoDBAttribute
  private String pillarName;

  @DynamoDBAttribute
  private Map<String, CategoryScore> categoryIdToCategoryScoreMap;

  @DynamoDBAttribute
  private Integer score;

}
