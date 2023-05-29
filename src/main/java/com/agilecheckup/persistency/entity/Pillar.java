package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.DescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Pillar extends DescribableEntity {

  @DynamoDBAttribute(attributeName = "categories")
  @DynamoDBTypeConvertedJson
  private Set<Category> categories;

}