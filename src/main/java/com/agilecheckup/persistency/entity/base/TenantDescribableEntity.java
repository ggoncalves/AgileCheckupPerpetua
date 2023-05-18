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
public class TenantDescribableEntity extends TenantableEntity implements Describable {

  @DynamoDBAttribute(attributeName = "name")
  @NonNull
  private String name;

  @DynamoDBAttribute(attributeName = "description")
  @NonNull
  private String description;

}
