package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.DescribableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Pillar extends DescribableEntity {

  @DynamoDBAttribute(attributeName = "categoryMap")
  private Map<String, Category> categoryMap;

}