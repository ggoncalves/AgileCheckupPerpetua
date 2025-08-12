package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.converter.GenderConverter;
import com.agilecheckup.persistency.converter.GenderPronounConverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class NaturalPerson extends Person {

  @Getter(onMethod_ = @__(@DynamoDbAttribute("aliasName")))
  private String aliasName;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("gender"), @DynamoDbConvertedBy(GenderConverter.class)}))
  private Gender gender;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("genderPronoun"), @DynamoDbConvertedBy(GenderPronounConverter.class)}))
  private GenderPronoun genderPronoun;
}