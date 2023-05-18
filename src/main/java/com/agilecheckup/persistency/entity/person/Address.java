package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Address extends BaseEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "street")
  private String street;

  @NonNull
  @DynamoDBAttribute(attributeName = "city")
  private String city;

  @NonNull
  @DynamoDBAttribute(attributeName = "state")
  private String state;

  @NonNull
  @DynamoDBAttribute(attributeName = "zipcode")
  private String zipcode;

  @NonNull
  @DynamoDBAttribute(attributeName = "country")
  private String country;

}
