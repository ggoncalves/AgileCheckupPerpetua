package com.agilecheckup.persistency.entity.question;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDBDocument
public class OptionGroup implements Serializable {

  @DynamoDBAttribute(attributeName = "isMultipleChoice")
  private boolean isMultipleChoice;

  @DynamoDBAttribute(attributeName = "showFlushed")
  private boolean showFlushed;

  @DynamoDBAttribute(attributeName = "options")
  @DynamoDBTypeConvertedJson
  private List<QuestionOption> options;
}
