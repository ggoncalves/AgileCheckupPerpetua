package com.agilecheckup.persistency.entity;

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
public class Address extends AbstractEntity {

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
