package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.entity.base.AuditableEntityV2;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Person extends AuditableEntityV2 {

  @NonNull
  @DynamoDBAttribute(attributeName = "name")
  private String name;

  @NonNull
  @DynamoDBAttribute(attributeName = "email")
  private String email;

  @DynamoDBAttribute(attributeName = "phone")
  private String phone;

  @DynamoDBAttribute(attributeName = "address")
  @DynamoDBTypeConvertedJson
  private Address address;

  @DynamoDBAttribute(attributeName = "personDocumentType")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private PersonDocumentType personDocumentType;

  @DynamoDBAttribute(attributeName = "documentNumber")
  private String documentNumber;
}
