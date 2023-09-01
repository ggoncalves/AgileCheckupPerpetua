package com.agilecheckup.persistency.entity.question;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.base.TenantableEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDBTable(tableName = "Question")
public class Question extends TenantableEntity {

  @DynamoDBAttribute(attributeName = "question")
  @NonNull
  private String question;

  @DynamoDBAttribute(attributeName = "extraDescription")
  private String extraDescription;

  @DynamoDBAttribute(attributeName = "points")
  private Integer points;

  @NonNull
  @DynamoDBAttribute(attributeName = "type")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private QuestionType questionType;

  @DynamoDBAttribute(attributeName = "optionGroup")
  @DynamoDBTypeConvertedJson
  private OptionGroup optionGroup;

}
