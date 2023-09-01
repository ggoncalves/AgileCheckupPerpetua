package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "Department")
public class Department extends TenantDescribableEntity {

  // If this class is refactored to be embedded inside Company, audit date and id shaw be removed.
  @NonNull
  @DynamoDBAttribute(attributeName = "companyId")
  private String companyId;


}