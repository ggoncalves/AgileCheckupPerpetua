package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.Tenantable;
import com.agilecheckup.persistency.entity.person.LegalPerson;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
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

  @DynamoDBAttribute(attributeName = "size")
  private Integer size;

  @DynamoDBAttribute(attributeName = "industry")
  private String industry;

  @DynamoDBAttribute(attributeName = "contactPerson")
  @DynamoDBTypeConvertedJson
  private NaturalPerson contactPerson;
}