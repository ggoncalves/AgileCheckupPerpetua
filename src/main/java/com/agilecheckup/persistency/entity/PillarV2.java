package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.CategoryMapAttributeConverter;
import com.agilecheckup.persistency.entity.base.DescribableEntityV2;
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

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class PillarV2 extends DescribableEntityV2 {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryMap"), @DynamoDbConvertedBy(CategoryMapAttributeConverter.class)}))
  private Map<String, CategoryV2> categoryMap;

}