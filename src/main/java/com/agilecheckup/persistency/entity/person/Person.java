package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.converter.AddressAttributeConverter;
import com.agilecheckup.persistency.converter.PersonDocumentTypeConverter;
import com.agilecheckup.persistency.entity.base.AuditableEntity;

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

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public abstract class Person extends AuditableEntity {

  @NonNull
  @Getter(onMethod_ = @__(@DynamoDbAttribute("name")))
  private String name;

  @NonNull
  @Getter(onMethod_ = @__(@DynamoDbAttribute("email")))
  private String email;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("phone")))
  private String phone;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("address"), @DynamoDbConvertedBy(AddressAttributeConverter.class)}))
  private Address address;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("personDocumentType"), @DynamoDbConvertedBy(PersonDocumentTypeConverter.class)}))
  private PersonDocumentType personDocumentType;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("documentNumber")))
  private String documentNumber;
}