package com.agilecheckup.persistency.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.entity.question.Answer.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity for storing precomputed dashboard analytics data.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "dashboard_analytics")
public class DashboardAnalytics extends BaseEntity {

  @DynamoDBHashKey(attributeName = "company_performance_cycle_id")
  private String companyPerformanceCycleId; // Format: companyId#performanceCycleId

  @DynamoDBRangeKey(attributeName = "assessment_matrix_team_id")
  private String assessmentMatrixTeamId; // Format: assessmentMatrixId#teamId (or "OVERVIEW" for summary)

  @DynamoDBAttribute(attributeName = "company_id")
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "company-cycle-index")
  private String companyId;

  @DynamoDBAttribute(attributeName = "performance_cycle_id")
  @DynamoDBIndexRangeKey(globalSecondaryIndexName = "company-cycle-index")
  private String performanceCycleId;

  @DynamoDBAttribute(attributeName = "assessment_matrix_id")
  private String assessmentMatrixId;

  @DynamoDBAttribute(attributeName = "team_id")
  private String teamId;

  @DynamoDBAttribute(attributeName = "team_name")
  private String teamName;

  @DynamoDBAttribute(attributeName = "general_average")
  private Double generalAverage;

  @DynamoDBAttribute(attributeName = "employee_count")
  private Integer employeeCount;

  @DynamoDBAttribute(attributeName = "completion_percentage")
  private Double completionPercentage;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "last_updated")
  private LocalDateTime lastUpdated;

  @DynamoDBAttribute(attributeName = "analytics_data")
  private String analyticsDataJson;
}