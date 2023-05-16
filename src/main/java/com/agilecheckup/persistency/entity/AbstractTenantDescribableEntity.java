package com.agilecheckup.persistency.entity;

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
public class AbstractTenantDescribableEntity extends AbstractTenantableEntity implements Describable {

  @DynamoDBAttribute(attributeName = "name")
  @NonNull
  private String name;

  @DynamoDBAttribute(attributeName = "description")
  @NonNull
  private String description;

}
