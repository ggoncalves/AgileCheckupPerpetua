package com.agilecheckup.persistency.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public abstract class TenantDescribableEntity extends TenantableEntity implements Describable {

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("name")}))
  private String name;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("description")}))
  private String description;

}