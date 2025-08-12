package com.agilecheckup.persistency.entity.base;

import java.time.Instant;

import com.agilecheckup.persistency.converter.InstantAttributeConverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public abstract class AuditableEntity extends BaseEntity implements Auditable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("createdDate"), @DynamoDbConvertedBy(InstantAttributeConverter.class)}))
  private Instant createdDate;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("lastUpdatedDate"), @DynamoDbConvertedBy(InstantAttributeConverter.class)}))
  private Instant lastUpdatedDate;

  public void updateTimestamps() {
    Instant now = Instant.now();
    if (this.createdDate == null) {
      this.createdDate = now;
    }
    this.lastUpdatedDate = now;
  }
}