package com.agilecheckup.service.dto;

import com.agilecheckup.persistency.entity.score.PotentialScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<TeamAssessmentSummaryV2> teamSummaries;
    private List<EmployeeAssessmentSummaryV2> employeeSummaries;
    private int totalEmployees;
    private int completedAssessments;
}