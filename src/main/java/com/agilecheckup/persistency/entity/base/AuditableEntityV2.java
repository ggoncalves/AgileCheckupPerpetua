package com.agilecheckup.persistency.entity.base;

import com.agilecheckup.persistency.converter.InstantAttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public abstract class AuditableEntityV2 extends BaseEntityV2 implements AuditableV2 {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("createdDate"), @DynamoDbConvertedBy(InstantAttributeConverter.class)}))
  private Instant createdDate;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("lastModifiedDate"), @DynamoDbConvertedBy(InstantAttributeConverter.class)}))
  private Instant lastModifiedDate;

  public void updateTimestamps() {
    Instant now = Instant.now();
    if (this.createdDate == null) {
      this.createdDate = now;
    }
    this.lastModifiedDate = now;
  }
}