package com.agilecheckup.service.dto;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * V2 Employee assessment summary DTO for dashboard data.
 * 
 * @author Claude (claude-sonnet-4-20250514)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAssessmentSummaryV2 {
    
    private String employeeAssessmentId;
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private String teamId;
    private String teamName;
    private AssessmentStatus assessmentStatus;
    private Double currentScore;
    private Integer answeredQuestionCount;
    private LocalDateTime lastActivityDate;
}