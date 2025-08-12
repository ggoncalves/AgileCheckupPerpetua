package com.agilecheckup.persistency.entity.question;

import java.io.Serializable;
import java.util.Map;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class OptionGroup implements Serializable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("isMultipleChoice")}))
  private boolean isMultipleChoice;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("showFlushed")}))
  private boolean showFlushed;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("optionMap")}))
  private Map<Integer, QuestionOption> optionMap;
}