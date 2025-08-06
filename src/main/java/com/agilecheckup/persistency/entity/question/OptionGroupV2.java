package com.agilecheckup.persistency.entity.question;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class OptionGroupV2 implements Serializable {

  @Getter(onMethod_ = @__({@DynamoDbAttribute("isMultipleChoice")}))
  private boolean isMultipleChoice;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("showFlushed")}))
  private boolean showFlushed;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("optionMap")}))
  private Map<Integer, QuestionOptionV2> optionMap;
}