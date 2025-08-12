package com.agilecheckup.persistency.entity;

import java.util.Map;

import com.agilecheckup.persistency.converter.CategoryMapAttributeConverter;
import com.agilecheckup.persistency.entity.base.DescribableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class Pillar extends DescribableEntity {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryMap"), @DynamoDbConvertedBy(CategoryMapAttributeConverter.class)}))
  private Map<String, Category> categoryMap;

}