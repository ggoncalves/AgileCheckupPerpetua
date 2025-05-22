package com.agilecheckup.persistency.entity.base;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
@ToString(callSuper=true, includeFieldNames=true)
public class TenantableEntity extends AuditableEntity implements Tenantable {

  @NonNull
  @DynamoDBAttribute(attributeName = "tenantId")
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "tenantId-index", attributeName = "tenantId")
  private String tenantId;
}
