package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeAssessmentV2Test {

    @Test
    void testEmployeeAssessmentV2Construction() {
        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("John Doe")
            .email("john.doe@example.com")
            .documentNumber("123456789")
            .personDocumentType(PersonDocumentType.CPF)
            .gender(Gender.MALE)
            .genderPronoun(GenderPronoun.HE)
            .build();

        EmployeeAssessmentScore score = EmployeeAssessmentScore.builder()
            .score(85.5)
            .build();

        Date lastActivity = new Date();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .assessmentMatrixId("matrix-123")
            .employee(employee)
            .teamId("team-123")
            .employeeAssessmentScore(score)
            .assessmentStatus(AssessmentStatus.IN_PROGRESS)
            .answeredQuestionCount(5)
            .employeeEmailNormalized("john.doe@example.com")
            .lastActivityDate(lastActivity)
            .build();
        assessment.setTenantId("tenant-123");

        assertThat(assessment.getId()).isEqualTo("assessment-123");
        assertThat(assessment.getTenantId()).isEqualTo("tenant-123");
        assertThat(assessment.getAssessmentMatrixId()).isEqualTo("matrix-123");
        assertThat(assessment.getEmployee()).isEqualTo(employee);
        assertThat(assessment.getTeamId()).isEqualTo("team-123");
        assertThat(assessment.getEmployeeAssessmentScore()).isEqualTo(score);
        assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
        assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(5);
        assertThat(assessment.getEmployeeEmailNormalized()).isEqualTo("john.doe@example.com");
        assertThat(assessment.getLastActivityDate()).isEqualTo(lastActivity);
    }

    @Test
    void testSetEmployeeUpdatesNormalizedEmail() {
        NaturalPerson initialEmployee = NaturalPerson.builder()
            .id("person-initial")
            .name("Initial Name")
            .email("initial@example.com")
            .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .assessmentMatrixId("matrix-123")
            .teamId("team-123")
            .employee(initialEmployee)
            .build();
        assessment.setTenantId("tenant-123");

        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("Jane Smith")
            .email("JANE.SMITH@EXAMPLE.COM")
            .build();

        assessment.setEmployee(employee);

        assertThat(assessment.getEmployee()).isEqualTo(employee);
        assertThat(assessment.getEmployeeEmailNormalized()).isEqualTo("jane.smith@example.com");
    }


    @Test
    void testSetEmployeeWithBlankEmailSetsNormalizedEmailToNull() {
        NaturalPerson initialEmployee = NaturalPerson.builder()
            .id("person-initial")
            .name("Initial Name")
            .email("initial@example.com")
            .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .assessmentMatrixId("matrix-123")
            .teamId("team-123")
            .employee(initialEmployee)
            .build();
        assessment.setTenantId("tenant-123");

        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("Jane Smith")
            .email("   ")
            .build();

        assessment.setEmployee(employee);

        assertThat(assessment.getEmployee()).isEqualTo(employee);
        assertThat(assessment.getEmployeeEmailNormalized()).isNull();
    }

    @Test
    void testDefaultAnsweredQuestionCountIsZero() {
        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("Test Employee")
            .email("test@example.com")
            .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .tenantId("tenant-123")
            .assessmentMatrixId("matrix-123")
            .teamId("team-123")
            .employee(employee)
            .build();

        assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(0);
    }

    @Test
    void testDefaultAssessmentStatusIsInvited() {
        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("Test Employee")
            .email("test@example.com")
            .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .tenantId("tenant-123")
            .assessmentMatrixId("matrix-123")
            .teamId("team-123")
            .employee(employee)
            .build();

        assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.INVITED);
    }
}