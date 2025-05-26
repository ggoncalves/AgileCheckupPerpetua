package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.Tenantable;
import com.agilecheckup.persistency.entity.person.LegalPerson;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "Company")
public class Company extends LegalPerson implements Tenantable {

  @NonNull
  @DynamoDBAttribute(attributeName = "tenantId")
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "tenantId-index", attributeName = "tenantId")
  private String tenantId;

  @DynamoDBAttribute(attributeName = "website")
  private String website;

  @DynamoDBAttribute(attributeName = "legalName")
  private String legalName;

  @DynamoDBAttribute(attributeName = "size")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private CompanySize size;

  @DynamoDBAttribute(attributeName = "industry")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private Industry industry;

  @DynamoDBAttribute(attributeName = "contactPerson")
  @DynamoDBTypeConvertedJson
  private NaturalPerson contactPerson;
}