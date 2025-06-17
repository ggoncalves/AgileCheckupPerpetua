package com.agilecheckup.persistency.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class AssessmentConfiguration {
    
    @DynamoDBAttribute(attributeName = "allowQuestionReview")
    @Builder.Default
    private Boolean allowQuestionReview = true;
    
    @DynamoDBAttribute(attributeName = "requireAllQuestions")
    @Builder.Default
    private Boolean requireAllQuestions = true;
    
    @DynamoDBAttribute(attributeName = "autoSave")
    @Builder.Default
    private Boolean autoSave = true;
    
    @DynamoDBAttribute(attributeName = "navigationMode")
    @DynamoDBTypeConvertedEnum
    @Builder.Default
    private QuestionNavigationType navigationMode = QuestionNavigationType.RANDOM;
}