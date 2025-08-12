package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.CompanySizeConverter;
import com.agilecheckup.persistency.converter.IndustryConverter;
import com.agilecheckup.persistency.converter.NaturalPersonConverter;
import com.agilecheckup.persistency.entity.base.Tenantable;
import com.agilecheckup.persistency.entity.person.LegalPerson;
import com.agilecheckup.persistency.entity.person.NaturalPerson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class Company extends LegalPerson implements Tenantable {

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("tenantId"), @DynamoDbSecondaryPartitionKey(indexNames = {"tenantId-index"})}))
  private String tenantId;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("website")))
  private String website;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("legalName")))
  private String legalName;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("size"), @DynamoDbConvertedBy(CompanySizeConverter.class)}))
  private CompanySize size;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("industry"), @DynamoDbConvertedBy(IndustryConverter.class)}))
  private Industry industry;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("contactPerson"), @DynamoDbConvertedBy(NaturalPersonConverter.class)}))
  private NaturalPerson contactPerson;
}