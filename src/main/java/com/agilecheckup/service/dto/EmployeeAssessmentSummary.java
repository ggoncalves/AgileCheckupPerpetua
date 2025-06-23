package com.agilecheckup.service.dto;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain DTO representing individual employee assessment summary.
 * 
 * @author Claude (claude-sonnet-4-20250514)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAssessmentSummary {
    
    private String employeeAssessmentId;
    private String employeeName;
    private String employeeEmail;
    private String teamId;
    private AssessmentStatus assessmentStatus;
    private Integer answeredQuestions;
    private Double currentScore;
    private LocalDateTime lastActivityDate;
}