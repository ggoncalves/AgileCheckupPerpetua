package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.entity.person.*;
import com.agilecheckup.persistency.entity.question.Question;
import lombok.NonNull;
import org.apache.commons.lang3.SerializationUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestObjectFactory {

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
        .companyId("A company Id")
        .name("DepartmentName")
        .description("Department description")
        .tenantId("tenantId")
        .build();
  }

  public static EmployeeAssessment createMockedEmployeeAssessment(String id, String name, String assessmentMatrixId) {
    return EmployeeAssessment.builder()
        .id(id)
        .assessmentMatrixId(assessmentMatrixId)
        .team(createMockedTeam(id))
        .employee(createMockedNaturalPerson(name))
        .build();
  }

  public static NaturalPerson createMockedNaturalPerson(String name) {
    return NaturalPerson.builder()
        .id(null)
        .name(name)
        .email("name@company.com")
        .documentNumber("1234")
        .personDocumentType(PersonDocumentType.CPF)
        .gender(Gender.MALE)
        .genderPronoun(GenderPronoun.HE)
        .build();
  }



  public static Department createMockedDepartmentWithDependenciesId(String companyId) {
    return Department.builder()
        .companyId(companyId)
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
        .companyId(department.getCompanyId())
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

  public static Team createMockedTeamWithDependenciesId(String departmentId) {
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

  public static PerformanceCycle createMockedPerformanceCycleWithDependenciesId(String companyId) {
    return PerformanceCycle.builder()
        .name("PerformanceCycleName")
        .description("PerformanceCycle description")
        .tenantId("tenantId")
        .companyId(companyId)
        .isActive(true)
        .isTimeSensitive(false)
        .build();
  }

  public static AssessmentMatrix createMockedAssessmentMatrix(String dependenciesId, String id, Set<Pillar> pillars) {
    return cloneWithId(createMockedAssessmentMatrixWithDependenciesId(dependenciesId, pillars), id);
  }

  public static AssessmentMatrix createMockedAssessmentMatrixWithDependenciesId(String dependenciesId, Set<Pillar> pillars) {
    return AssessmentMatrix.builder()
        .name("AssessmentMatrixName")
        .description("AssessmentMatrix description")
        .tenantId("tenantId")
        .performanceCycleId(dependenciesId)
        .pillars(pillars)
        .build();
  }

  public static PerformanceCycle createMockedPerformanceCycle(String companyId, String id) {
    return cloneWithId(createMockedPerformanceCycleWithDependenciesId(companyId), id);
  }

  public static Pillar createMockedPillar(@NonNull String name, @NonNull String description, Set<Category> categories) {
    return Pillar.builder()
        .name(name)
        .description(description)
        .categories(categories)
        .build();
  }

  public static Set<Pillar> createMockedPillarSet(Integer pillarSize, Integer categorySize, String pillarPrefix, String categoryPrefix) {
    return IntStream.range(1, pillarSize + 1)
        .mapToObj(i -> createMockedPillar(
            strName(pillarPrefix, i),
            strDescription(pillarPrefix, i),
            IntStream.range(1, categorySize + 1)
                .mapToObj(ci -> createMockedCategory(strName(categoryPrefix + i, ci), strDescription(categoryPrefix + i, ci)))
                .collect(Collectors.toSet())
        )).collect(Collectors.toSet());
  }

  public static Set<Category> createMockedCategorySet(Integer size, String prefix) {
    return IntStream.range(1, size + 1).mapToObj(i -> createMockedCategory(strName(prefix, i), strDescription(prefix, i))).collect(Collectors.toSet());
  }

  private static String strName(String prefix, Integer i) {
    return str(prefix + " Name - ", i);
  }

  private static String strDescription(String prefix, Integer i) {
    return str(prefix + " Description - ", i);
  }

  private static String str(String prefix, Integer i) {
    return new StringBuilder().append(prefix).append(" ").append(i).toString();
  }

  public static Category createMockedCategory(@NonNull String name, @NonNull String description) {
    return Category.builder()
        .name(name)
        .description(description)
        .build();
  }

  public static <T extends BaseEntity> T cloneWithId(T t, String id) {
    T clonedT = SerializationUtils.clone(t);
    clonedT.setId(id);
    return clonedT;
  }
}
