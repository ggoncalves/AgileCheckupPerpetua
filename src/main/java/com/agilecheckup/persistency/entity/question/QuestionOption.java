package com.agilecheckup.persistency.entity.question;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class QuestionOption implements Serializable {

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("id")}))
  private Integer id;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("text")}))
  private String text;

  @NonNull
  @Getter(onMethod_ = @__({@DynamoDbAttribute("points")}))
  private Double points;
}