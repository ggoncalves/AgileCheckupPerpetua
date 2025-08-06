package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.DescribableEntityV2;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Pillar extends DescribableEntityV2 {

  @DynamoDBAttribute(attributeName = "categoryMap")
  private Map<String, Category> categoryMap;

}