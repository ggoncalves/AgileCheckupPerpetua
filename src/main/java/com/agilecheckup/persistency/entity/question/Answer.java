package com.agilecheckup.persistency.entity.question;

import java.time.LocalDateTime;

import com.agilecheckup.persistency.converter.LocalDateTimeAttributeConverter;
import com.agilecheckup.persistency.converter.NaturalPersonAttributeConverter;
import com.agilecheckup.persistency.converter.QuestionAttributeConverter;
import com.agilecheckup.persistency.converter.QuestionTypeAttributeConverter;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.base.TenantableEntity;
import com.agilecheckup.persistency.entity.person.NaturalPerson;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class Answer extends TenantableEntity {

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("employeeAssessmentId"), @DynamoDbSecondaryPartitionKey(indexNames = "employeeAssessmentId-tenantId-index")}))
  private String employeeAssessmentId;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("answeredAt"), @DynamoDbConvertedBy(LocalDateTimeAttributeConverter.class)}))
  private LocalDateTime answeredAt;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarId")}))
  private String pillarId;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryId")}))
  private String categoryId;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("questionId")}))
  private String questionId;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("reviewer"), @DynamoDbConvertedBy(NaturalPersonAttributeConverter.class)}))
  private NaturalPerson reviewer;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("questionType"), @DynamoDbConvertedBy(QuestionTypeAttributeConverter.class)}))
  private QuestionType questionType;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("question"), @DynamoDbConvertedBy(QuestionAttributeConverter.class)}))
  private Question question;

  @Builder.Default
  @Getter(onMethod_ = @__({@DynamoDbAttribute("pendingReview")}))
  private boolean pendingReview = false;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("value")}))
  private String value;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("score")}))
  private Double score;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("notes")}))
  private String notes;

  /**
   * Override tenantId to add it as range key for employeeAssessmentId-tenantId-index GSI.
   * This enables efficient querying by both employeeAssessmentId and tenantId.
   */
  @Override
  @DynamoDbSecondarySortKey(indexNames = "employeeAssessmentId-tenantId-index")
  public String getTenantId() {
    return super.getTenantId();
  }
}