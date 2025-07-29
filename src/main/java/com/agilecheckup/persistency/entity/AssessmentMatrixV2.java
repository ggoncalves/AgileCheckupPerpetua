package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.AssessmentConfigurationAttributeConverter;
import com.agilecheckup.persistency.converter.PillarMapAttributeConverter;
import com.agilecheckup.persistency.converter.PotentialScoreAttributeConverter;
import com.agilecheckup.persistency.entity.base.TenantDescribableEntityV2;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@DynamoDbBean
public class AssessmentMatrixV2 extends TenantDescribableEntityV2 {

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("performanceCycle")}))
    private String performanceCycleId;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("pillarMap"), @DynamoDbConvertedBy(PillarMapAttributeConverter.class)}))
    private Map<String, PillarV2> pillarMap;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("questionCount")}))
    private Integer questionCount;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("potentialScore"), @DynamoDbConvertedBy(PotentialScoreAttributeConverter.class)}))
    private PotentialScore potentialScore;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("configuration"), @DynamoDbConvertedBy(AssessmentConfigurationAttributeConverter.class)}))
    private AssessmentConfiguration configuration;
}