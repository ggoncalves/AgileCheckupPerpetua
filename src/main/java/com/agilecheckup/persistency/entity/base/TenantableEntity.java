package com.agilecheckup.persistency.entity.base;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
@ToString(callSuper=true, includeFieldNames=true)
public class TenantableEntity extends AuditableEntity implements Tenantable {

  @DynamoDBAttribute(attributeName = "tenantId")
  @NonNull
  private String tenantId;

}
