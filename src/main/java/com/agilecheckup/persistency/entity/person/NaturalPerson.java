package com.agilecheckup.persistency.entity.person;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class NaturalPerson extends Person {

  @DynamoDBAttribute(attributeName = "aliasName")
  private String aliasName;

  @DynamoDBAttribute(attributeName = "gender")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private Gender gender;

  @DynamoDBAttribute(attributeName = "genderPronoun")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private GenderPronoun genderPronoun;

}
