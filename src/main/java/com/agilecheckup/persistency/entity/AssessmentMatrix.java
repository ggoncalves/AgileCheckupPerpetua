package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.converter.PillarV2MapConverter;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
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
  @DynamoDBTypeConverted(converter = PillarV2MapConverter.class)
  private Map<String, PillarV2> pillarMap;

  @DynamoDBAttribute(attributeName = "questionCount")
  @Builder.Default
  private Integer questionCount = 0;

  @DynamoDBAttribute(attributeName = "potentialScore")
  private PotentialScore potentialScore;

  @DynamoDBAttribute(attributeName = "configuration")
  private AssessmentConfiguration configuration;

}