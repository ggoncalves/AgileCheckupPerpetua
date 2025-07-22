package com.agilecheckup.persistency.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public abstract class TenantDescribableEntityV2 extends TenantableEntityV2 implements DescribableV2 {

  @Getter(onMethod_ = @__(@DynamoDbAttribute("name")))
  private String name;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("description")))
  private String description;
}