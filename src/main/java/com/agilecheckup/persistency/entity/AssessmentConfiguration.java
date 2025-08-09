package com.agilecheckup.persistency.entity;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class AssessmentConfiguration {
    
    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("allowQuestionReview")}))
    private Boolean allowQuestionReview = true;
    
    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("requireAllQuestions")}))
    private Boolean requireAllQuestions = true;
    
    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("autoSave")}))
    private Boolean autoSave = true;
    
    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("navigationMode")}))
    private QuestionNavigationType navigationMode = QuestionNavigationType.RANDOM;
}