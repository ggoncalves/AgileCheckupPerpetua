package com.agilecheckup.persistency.entity.question;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.base.TenantableEntity;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@DynamoDBTable(tableName = "Answer")
public class Answer extends TenantableEntity {

  @NonNull
  @DynamoDBAttribute(attributeName = "employeeAssessmentId")
  private String employeeAssessmentId;

  @NonNull
  @DynamoDBAttribute(attributeName = "answeredAt")
  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  private LocalDateTime answeredAt;

  @NonNull
  @DynamoDBAttribute(attributeName = "pillarId")
  private String pillarId;

  @NonNull
  @DynamoDBAttribute(attributeName = "categoryId")
  private String categoryId;

  @NonNull
  @DynamoDBAttribute(attributeName = "questionId")
  private String questionId;

  @DynamoDBAttribute(attributeName = "reviewer")
  @DynamoDBTypeConvertedJson
  private NaturalPerson reviewer;

  @NonNull
  @DynamoDBAttribute(attributeName = "questionType")
  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
  private QuestionType questionType;

  @DynamoDBAttribute(attributeName = "question")
  @DynamoDBTypeConvertedJson
  private Question question;

  // TODO: Remove all question related fields

  @DynamoDBAttribute(attributeName = "pendingReview")
  @Builder.Default
  private boolean pendingReview = false;

  @NonNull
  @DynamoDBAttribute(attributeName = "value")
  private String value;

  @DynamoDBAttribute(attributeName = "score")
  private Double score;

  @DynamoDBAttribute(attributeName = "notes")
  private String notes;

  public static class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convert(final LocalDateTime time) {
      return time.format(FORMATTER);
    }

    @Override
    public LocalDateTime unconvert(final String stringValue) {
      return LocalDateTime.parse(stringValue, FORMATTER);
    }
  }
}
