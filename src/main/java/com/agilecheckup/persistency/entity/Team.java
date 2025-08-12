package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class Team extends TenantDescribableEntity {

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("departmentId"), @DynamoDbSecondaryPartitionKey(indexNames = "departmentId-index")}))
  private String departmentId;
}