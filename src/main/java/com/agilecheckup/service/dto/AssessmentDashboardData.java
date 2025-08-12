package com.agilecheckup.service.dto;

import java.util.List;

import com.agilecheckup.persistency.entity.score.PotentialScore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain DTO containing assessment dashboard data from business layer.
 * 
 * @author Claude (claude-sonnet-4-20250514)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDashboardData {

  private String assessmentMatrixId;
  private String matrixName;
  private PotentialScore potentialScore;
  private List<TeamAssessmentSummary> teamSummaries;
  private List<EmployeeAssessmentSummary> employeeSummaries;
  private int totalEmployees;
  private int completedAssessments;
}