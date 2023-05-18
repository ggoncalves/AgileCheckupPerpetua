package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;

public class TestObjectFactory {

  public static final String QUESTION_ID_1234 = "1234";
  public static final String GENERIC_ID_1234 = "1234";

  public static Question createMockedQuestion() {
    return Question.builder()
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question createMockedQuestion(String id) {
    return Question.builder()
        .id(id)
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question copyQuestionAndAddId(Question question, String id) {
    return Question.builder()
        .id(id)
        .question(question.getQuestion())
        .rateType(question.getRateType())
        .tenantId(question.getTenantId())
        .points(question.getPoints())
        .build();
  }

  public static Company createMockedCompany() {
    return Company.builder()
        .name("Company Name")
        .description("Company description")
        .tenantId("Random Tenant Id")
        .email("company@email.com")
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber("0001")
        .build();
  }

  public static Company createMockedCompany(String id) {
    return copyCompanyAndAddId(createMockedCompany(), id);
  }

  public static Company copyCompanyAndAddId(Company company, String id) {
    return Company.builder()
        .id(id)
        .name(company.getName())
        .description(company.getDescription())
        .tenantId(company.getTenantId())
        .email(company.getEmail())
        .personDocumentType(company.getPersonDocumentType())
        .documentNumber(company.getDocumentNumber())
        .industry(company.getIndustry())
        .size(company.getSize())
        .address(company.getAddress())
        .build();
  }

  public static Department createMockedDepartment() {
    return Department.builder()
        .company(createMockedCompany("A company Id"))
        .name("DepartmentName")
        .description("Department description")
        .tenantId("tenantId")
        .build();
  }

  public static Department createMockedDepartmentWithDependenciesId(String companyId) {
    return Department.builder()
        .company(createMockedCompany(companyId))
        .name("DepartmentName")
        .description("Department description")
        .tenantId("tenantId")
        .build();
  }

  public static Department createMockedDepartment(String id) {
    return copyDepartmentAndAddId(createMockedDepartment(), id);
  }

  public static Department copyDepartmentAndAddId(Department department, String id) {
    return Department.builder()
        .id(id)
        .company(department.getCompany())
        .name(department.getName())
        .description(department.getDescription())
        .tenantId(department.getTenantId())
        .build();
  }

  public static Team createMockedTeam() {
    return Team.builder()
        .department(createMockedDepartment("A DepartmentId"))
        .name("TeamName")
        .description("Team description")
        .tenantId("tenantId")
        .build();
  }

  public static Team createMockedTeamWithDependenciesId(String companyId, String departmentId) {
    return Team.builder()
        .department(createMockedDepartment(departmentId))
        .name("TeamName")
        .description("Team description")
        .tenantId("tenantId")
        .build();
  }

  public static Team createMockedTeam(String id) {
    return copyTeamAndAddId(createMockedTeam(), id);
  }

  public static Team copyTeamAndAddId(Team team, String id) {
    return Team.builder()
        .id(id)
        .department(team.getDepartment())
        .name(team.getName())
        .description(team.getDescription())
        .tenantId(team.getTenantId())
        .build();
  }
}
