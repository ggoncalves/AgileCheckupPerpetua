package com.agilecheckup.persistency.entity.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public abstract class TenantableEntityV2 extends AuditableEntityV2 implements TenantableV2 {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("tenantId"), @DynamoDbSecondaryPartitionKey(indexNames = {"tenantId-index"})}))
  private String tenantId;
}