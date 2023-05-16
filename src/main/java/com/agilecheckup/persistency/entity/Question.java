package com.agilecheckup.persistency.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "Question")
public class Question extends AbstractTenantableEntity {

  @DynamoDBAttribute(attributeName = "question")
  @NonNull
  private String question;

  @DynamoDBAttribute(attributeName = "extraDescription")
  private String extraDescription;

  @DynamoDBAttribute(attributeName = "points")
  private Integer points;

  @NonNull
  @DynamoDBAttribute(attributeName = "rateType")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private RateType rateType;

}
