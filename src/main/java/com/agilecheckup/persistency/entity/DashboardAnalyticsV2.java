package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.InstantAttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.io.Serializable;
import java.time.Instant;

/**
 * V2 Entity for storing precomputed dashboard analytics data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DashboardAnalyticsV2 implements Serializable {

    private String companyPerformanceCycleId; // Format: companyId#performanceCycleId
    private String assessmentMatrixScopeId; // Format: assessmentMatrixId#scope#teamId (teamId is null for ASSESSMENT_MATRIX scope)
    private String companyId;
    private String performanceCycleId;
    private String assessmentMatrixId;
    private AnalyticsScope scope;
    private String teamId;
    private String teamName;
    private String companyName;
    private String performanceCycleName;
    private String assessmentMatrixName;
    private Double generalAverage;
    private Integer employeeCount;
    private Double completionPercentage;
    private Instant lastUpdated;
    private String analyticsDataJson;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyPerformanceCycleId")
    public String getCompanyPerformanceCycleId() {
        return companyPerformanceCycleId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("assessmentMatrixTeamId")
    public String getAssessmentMatrixScopeId() {
        return assessmentMatrixScopeId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"company-cycle-index"})
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSecondarySortKey(indexNames = {"company-cycle-index"})
    @DynamoDbAttribute("performanceCycleId")
    public String getPerformanceCycleId() {
        return performanceCycleId;
    }

    @DynamoDbAttribute("assessmentMatrixId")
    public String getAssessmentMatrixId() {
        return assessmentMatrixId;
    }

    @DynamoDbAttribute("scope")
    public AnalyticsScope getScope() {
        return scope;
    }

    @DynamoDbAttribute("teamId")
    public String getTeamId() {
        return teamId;
    }

    @DynamoDbAttribute("teamName")
    public String getTeamName() {
        return teamName;
    }

    @DynamoDbAttribute("companyName")
    public String getCompanyName() {
        return companyName;
    }

    @DynamoDbAttribute("performanceCycleName")
    public String getPerformanceCycleName() {
        return performanceCycleName;
    }

    @DynamoDbAttribute("assessmentMatrixName")
    public String getAssessmentMatrixName() {
        return assessmentMatrixName;
    }

    @DynamoDbAttribute("generalAverage")
    public Double getGeneralAverage() {
        return generalAverage;
    }

    @DynamoDbAttribute("employeeCount")
    public Integer getEmployeeCount() {
        return employeeCount;
    }

    @DynamoDbAttribute("completionPercentage")
    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    @DynamoDbAttribute("lastUpdated")
    @DynamoDbConvertedBy(InstantAttributeConverter.class)
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @DynamoDbAttribute("analyticsDataJson")
    public String getAnalyticsDataJson() {
        return analyticsDataJson;
    }
}