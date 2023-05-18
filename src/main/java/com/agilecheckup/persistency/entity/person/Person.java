package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.entity.base.AuditableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Person extends AuditableEntity {

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
