package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "AssessmentMatrix")
public class AssessmentMatrix extends TenantDescribableEntity {

  @DynamoDBAttribute(attributeName = "performanceCycle")
  @DynamoDBTypeConvertedJson
  private PerformanceCycle performanceCycle;

  @DynamoDBAttribute(attributeName = "pillars")
  @DynamoDBTypeConvertedJson
  private Set<Pillar> pillars;

}