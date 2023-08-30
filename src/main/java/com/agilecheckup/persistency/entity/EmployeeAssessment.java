package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
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
@DynamoDBTable(tableName = "EmployeeAssessment")
public class EmployeeAssessment extends BaseEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "assessmentMatrixId")
  private String assessmentMatrixId;

  @NonNull
  @DynamoDBAttribute(attributeName = "employee")
  @DynamoDBTypeConvertedJson
  private NaturalPerson employee;

  @DynamoDBAttribute(attributeName = "team")
  @DynamoDBTypeConvertedJson
  private Team team;
}