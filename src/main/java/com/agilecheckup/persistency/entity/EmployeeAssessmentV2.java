package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.DateAttributeConverter;
import com.agilecheckup.persistency.converter.EmployeeAssessmentScoreV2AttributeConverter;
import com.agilecheckup.persistency.converter.NaturalPersonAttributeConverter;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.base.TenantableEntityV2;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.util.Date;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class EmployeeAssessmentV2 extends TenantableEntityV2 {

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("assessmentMatrixId"), @DynamoDbSecondaryPartitionKey(indexNames = {"assessmentMatrixId-employeeEmail-index"})}))
    private String assessmentMatrixId;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("employee"), @DynamoDbConvertedBy(NaturalPersonAttributeConverter.class)}))
    private NaturalPersonV2 employee;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("teamId")}))
    private String teamId;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("employeeAssessmentScore"), @DynamoDbConvertedBy(EmployeeAssessmentScoreV2AttributeConverter.class)}))
    private EmployeeAssessmentScoreV2 employeeAssessmentScore;

    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("assessmentStatus")}))
    private AssessmentStatus assessmentStatus = AssessmentStatus.INVITED;

    @Builder.Default
    @Getter(onMethod_ = @__({@DynamoDbAttribute("answeredQuestionCount")}))
    private Integer answeredQuestionCount = 0;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("employeeEmailNormalized"), @DynamoDbSecondarySortKey(indexNames = {"assessmentMatrixId-employeeEmail-index"})}))
    private String employeeEmailNormalized;

    @Getter(onMethod_ = @__({@DynamoDbAttribute("lastActivityDate"), @DynamoDbConvertedBy(DateAttributeConverter.class)}))
    private Date lastActivityDate;

    
    /**
     * Sets the employee and automatically updates the normalized email for GSI
     */
    public void setEmployee(NaturalPersonV2 employee) {
        this.employee = employee;
        this.employeeEmailNormalized = Optional.ofNullable(employee)
            .map(NaturalPersonV2::getEmail)
            .filter(StringUtils::isNotBlank)
            .map(email -> email.toLowerCase().trim())
            .orElse(null);
    }
}