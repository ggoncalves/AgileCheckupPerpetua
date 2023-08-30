package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBConvertedBool;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "PerformanceCycle")
public class PerformanceCycle extends TenantDescribableEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "companyId")
  private String companyId;

  @NonNull
  @DynamoDBAttribute(attributeName = "isTimeSensitive")
  @DynamoDBConvertedBool(DynamoDBConvertedBool.Format.true_false)
  private Boolean isTimeSensitive;

  @NonNull
  @DynamoDBAttribute(attributeName = "isActive")
  @DynamoDBConvertedBool(DynamoDBConvertedBool.Format.true_false)
  private Boolean isActive;

  @DynamoDBAttribute(attributeName = "startDate")
  private Date startDate;

  @DynamoDBAttribute(attributeName = "endDate")
  private Date endDate;

  // TODO: Add the field below
//  @DynamoDBAttribute(attributeName = "consulting")
//  private Person consulting;
}