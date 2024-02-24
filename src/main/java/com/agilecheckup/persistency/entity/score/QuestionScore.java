package com.agilecheckup.persistency.entity.score;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class QuestionScore implements Scorable {

  @DynamoDBAttribute
  private String questionId;

  @DynamoDBAttribute
  private Integer score;
}
