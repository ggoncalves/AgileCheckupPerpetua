package com.agilecheckup.persistency.entity.base;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public abstract class BaseEntity implements Identifiable, Serializable {

  @Getter(onMethod_ = @__(@DynamoDbPartitionKey))
  @Builder.Default
  private String id = UUID.randomUUID().toString();

  public void generateId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}