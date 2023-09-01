package com.agilecheckup.persistency.entity.question;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDBDocument
public class QuestionOption implements Serializable {

  @NonNull
  @DynamoDBAttribute(attributeName = "id")
  private Integer id;

  @NonNull
  @DynamoDBAttribute(attributeName = "text")
  private String text;

  @NonNull
  @DynamoDBAttribute(attributeName = "points")
  private Integer points;
}
