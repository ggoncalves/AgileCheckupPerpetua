package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.question.Answer.LocalDateTimeConverter;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity for storing precomputed dashboard analytics data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "DashboardAnalytics")
public class DashboardAnalytics implements Serializable {

  @DynamoDBHashKey(attributeName = "companyPerformanceCycleId")
  private String companyPerformanceCycleId; // Format: companyId#performanceCycleId

  @DynamoDBRangeKey(attributeName = "assessmentMatrixTeamId")
  private String assessmentMatrixScopeId; // Format: assessmentMatrixId#scope#teamId (teamId is null for ASSESSMENT_MATRIX scope)

  @DynamoDBAttribute(attributeName = "companyId")
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "company-cycle-index")
  private String companyId;

  @DynamoDBAttribute(attributeName = "performanceCycleId")
  @DynamoDBIndexRangeKey(globalSecondaryIndexName = "company-cycle-index")
  private String performanceCycleId;

  @DynamoDBAttribute(attributeName = "assessmentMatrixId")
  private String assessmentMatrixId;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "scope")
  private AnalyticsScope scope;

  @DynamoDBAttribute(attributeName = "teamId")
  private String teamId;

  @DynamoDBAttribute(attributeName = "teamName")
  private String teamName;

  @DynamoDBAttribute(attributeName = "companyName")
  private String companyName;

  @DynamoDBAttribute(attributeName = "performanceCycleName")
  private String performanceCycleName;

  @DynamoDBAttribute(attributeName = "assessmentMatrixName")
  private String assessmentMatrixName;

  @DynamoDBAttribute(attributeName = "generalAverage")
  private Double generalAverage;

  @DynamoDBAttribute(attributeName = "employeeCount")
  private Integer employeeCount;

  @DynamoDBAttribute(attributeName = "completionPercentage")
  private Double completionPercentage;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "lastUpdated")
  private LocalDateTime lastUpdated;

  @DynamoDBAttribute(attributeName = "analyticsDataJson")
  private String analyticsDataJson;
}