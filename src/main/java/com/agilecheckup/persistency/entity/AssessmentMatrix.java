package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@DynamoDBTable(tableName = "AssessmentMatrix")
public class AssessmentMatrix extends TenantDescribableEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "performanceCycle")
  private String performanceCycleId;

  @DynamoDBAttribute(attributeName = "pillarMap")
  private Map<String, Pillar> pillarMap;

  @DynamoDBAttribute(attributeName = "questionCount")
  @Builder.Default
  private Integer questionCount = 0;

  @DynamoDBAttribute(attributeName = "potentialScore")
  private PotentialScore potentialScore;

}