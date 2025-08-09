package com.agilecheckup.persistency.entity.question;

import com.agilecheckup.persistency.converter.OptionGroupAttributeConverter;
import com.agilecheckup.persistency.converter.QuestionTypeAttributeConverter;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.base.TenantableEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class Question extends TenantableEntity {

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("assessmentMatrixId")}))
    private String assessmentMatrixId;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarId")}))
    private String pillarId;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarName")}))
    private String pillarName;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryId")}))
    private String categoryId;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("categoryName")}))
    private String categoryName;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("question")}))
    private String question;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("extraDescription")}))
    private String extraDescription;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("points")}))
    private Double points;

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("type"), @DynamoDbConvertedBy(QuestionTypeAttributeConverter.class)}))
    private QuestionType questionType;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("optionGroup"), @DynamoDbConvertedBy(OptionGroupAttributeConverter.class)}))
    private OptionGroup optionGroup;
}