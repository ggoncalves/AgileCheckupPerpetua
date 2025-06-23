package com.agilecheckup.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain DTO representing team-level assessment summary statistics.
 * 
 * @author Claude (claude-sonnet-4-20250514)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamAssessmentSummary {
    
    private String teamId;
    private String teamName;
    private int totalEmployees;
    private int completedAssessments;
    private double completionPercentage;
    private Double averageScore;
}