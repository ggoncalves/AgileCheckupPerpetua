package com.agilecheckup.persistency.entity;

import java.io.Serializable;
import java.time.Instant;

import com.agilecheckup.persistency.converter.InstantAttributeConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Entity for storing precomputed dashboard analytics data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DashboardAnalytics implements Serializable {

  @Getter(onMethod_ = @__({@DynamoDbPartitionKey, @DynamoDbAttribute("companyPerformanceCycleId")}))
  private String companyPerformanceCycleId; // Format: companyId#performanceCycleId

  @Getter(onMethod_ = @__({@DynamoDbSortKey, @DynamoDbAttribute("assessmentMatrixTeamId")}))
  private String assessmentMatrixScopeId; // Format: assessmentMatrixId#scope#teamId (teamId is null for ASSESSMENT_MATRIX scope)

  @Getter(onMethod_ = @__({@DynamoDbSecondaryPartitionKey(indexNames = {"company-cycle-index"}), @DynamoDbAttribute("companyId")}))
  private String companyId;

  @Getter(onMethod_ = @__({@DynamoDbSecondarySortKey(indexNames = {"company-cycle-index"}), @DynamoDbAttribute("performanceCycleId")}))
  private String performanceCycleId;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("assessmentMatrixId")))
  private String assessmentMatrixId;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("scope")))
  private AnalyticsScope scope;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("teamId")))
  private String teamId;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("teamName")))
  private String teamName;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("companyName")))
  private String companyName;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("performanceCycleName")))
  private String performanceCycleName;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("assessmentMatrixName")))
  private String assessmentMatrixName;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("generalAverage")))
  private Double generalAverage;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("employeeCount")))
  private Integer employeeCount;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("completionPercentage")))
  private Double completionPercentage;

  @Getter(onMethod_ = @__({@DynamoDbAttribute("lastUpdated"), @DynamoDbConvertedBy(InstantAttributeConverter.class)}))
  private Instant lastUpdated;

  @Getter(onMethod_ = @__(@DynamoDbAttribute("analyticsDataJson")))
  private String analyticsDataJson;
}