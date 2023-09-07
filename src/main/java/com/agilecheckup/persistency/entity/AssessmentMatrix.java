package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "AssessmentMatrix")
public class AssessmentMatrix extends TenantDescribableEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "performanceCycle")
  private String performanceCycleId;

  @DynamoDBAttribute(attributeName = "pillarMap")
  private Map<String, Pillar> pillarMap;

}