package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Address;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Question;
import lombok.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TestObjectFactory {

  public static final String GENERIC_ID_1234 = "1234";
  public static final String GENERIC_TENANT_ID = "tenant-123";
  public static final String GENERIC_COMPANY_ID = "company-456";
  public static final String DEFAULT_COMPANY_NAME = "Company Name";
  public static final String DEFAULT_COMPANY_EMAIL = "company@email.com";
  public static final String DEFAULT_COMPANY_DOCUMENT = "0001";

  // === Department  Factory Methods ===

  public static Department createMockedDepartment() {
    Department department = new Department();
    department.setName("DepartmentName");
    department.setDescription("Department description");
    department.setTenantId(GENERIC_TENANT_ID);
    department.setCompanyId(GENERIC_COMPANY_ID);
    department.setCreatedDate(Instant.now().minusSeconds(86400));
    department.setLastUpdatedDate(Instant.now());
    return department;
  }

  public static Department createMockedDepartmentWithDependenciesId(String companyId) {
    Department department = createMockedDepartment();
    department.setCompanyId(companyId);
    return department;
  }

  public static Department createMockedDepartment(String id) {
    Department department = createMockedDepartment();
    department.setId(id);
    return department;
  }

  public static Department createMockedDepartment(@NonNull String name, @NonNull String description,
                                                    @NonNull String tenantId, @NonNull String companyId) {
    Department department = new Department();
    department.setName(name);
    department.setDescription(description);
    department.setTenantId(tenantId);
    department.setCompanyId(companyId);
    department.setCreatedDate(Instant.now().minusSeconds(86400));
    department.setLastUpdatedDate(Instant.now());
    return department;
  }

  public static Department copyDepartmentAndAddId(Department department, String id) {
    Department copy = new Department();
    copy.setId(id);
    copy.setName(department.getName());
    copy.setDescription(department.getDescription());
    copy.setTenantId(department.getTenantId());
    copy.setCompanyId(department.getCompanyId());
    copy.setCreatedDate(department.getCreatedDate());
    copy.setLastUpdatedDate(department.getLastUpdatedDate());
    return copy;
  }

  // === Team  Factory Methods ===

  public static final String DEFAULT_DEPARTMENT_ID = "dept-789";

  public static Team createMockedTeam() {
    Team team = new Team();
    team.setName("Team Name");
    team.setDescription("Team description");
    team.setTenantId(GENERIC_TENANT_ID);
    team.setDepartmentId(DEFAULT_DEPARTMENT_ID);
    team.setCreatedDate(Instant.now().minusSeconds(86400));
    team.setLastUpdatedDate(Instant.now());
    return team;
  }

  public static Team createMockedTeamWithDependenciesId(String departmentId) {
    Team team = createMockedTeam();
    team.setDepartmentId(departmentId);
    return team;
  }

  public static Team createMockedTeam(String id) {
    Team team = createMockedTeam();
    team.setId(id);
    return team;
  }

  public static Team createMockedTeam(@NonNull String name, @NonNull String description,
                                        @NonNull String tenantId, @NonNull String departmentId) {
    Team team = new Team();
    team.setName(name);
    team.setDescription(description);
    team.setTenantId(tenantId);
    team.setDepartmentId(departmentId);
    team.setCreatedDate(Instant.now().minusSeconds(86400));
    team.setLastUpdatedDate(Instant.now());
    return team;
  }

  public static Team copyTeamAndAddId(Team team, String id) {
    Team copy = new Team();
    copy.setId(id);
    copy.setName(team.getName());
    copy.setDescription(team.getDescription());
    copy.setTenantId(team.getTenantId());
    copy.setDepartmentId(team.getDepartmentId());
    copy.setCreatedDate(team.getCreatedDate());
    copy.setLastUpdatedDate(team.getLastUpdatedDate());
    return copy;
  }

  // === Company  Factory Methods ===

  public static Company createMockedCompany() {
    return Company.builder()
        .name(DEFAULT_COMPANY_NAME)
        .email(DEFAULT_COMPANY_EMAIL)
        .description("Company description")
        .tenantId(GENERIC_TENANT_ID)
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(DEFAULT_COMPANY_DOCUMENT)
        .size(CompanySize.MEDIUM)
        .industry(Industry.TECHNOLOGY)
        .website("https://www.company.com")
        .legalName("Company Legal Name Inc.")
        .contactPerson(createMockedNaturalPerson())
        .address(createMockedAddress())
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Company createMockedCompany(String id) {
    Company company = createMockedCompany();
    company.setId(id);
    return company;
  }

  public static Company createMockedCompany(@NonNull String name, @NonNull String email, @NonNull String description,
                                              @NonNull String tenantId, @NonNull String documentNumber) {
    return Company.builder()
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId)
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .size(CompanySize.STARTUP)
        .industry(Industry.OTHER)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Company copyCompanyAndAddId(Company company, String id) {
    return Company.builder()
        .id(id)
        .name(company.getName())
        .email(company.getEmail())
        .description(company.getDescription())
        .tenantId(company.getTenantId())
        .personDocumentType(company.getPersonDocumentType())
        .documentNumber(company.getDocumentNumber())
        .size(company.getSize())
        .industry(company.getIndustry())
        .website(company.getWebsite())
        .legalName(company.getLegalName())
        .contactPerson(company.getContactPerson())
        .address(company.getAddress())
        .phone(company.getPhone())
        .createdDate(company.getCreatedDate())
        .lastUpdatedDate(company.getLastUpdatedDate())
        .build();
  }

  // === Address  Factory Methods ===

  public static Address createMockedAddress() {
    return Address.builder()
        .street("123 Main Street")
        .city("São Paulo")
        .state("SP")
        .zipcode("01234-567")
        .country("Brazil")
        .build();
  }

  // === NaturalPerson  Factory Methods ===

  public static NaturalPerson createMockedNaturalPerson() {
    return createMockedNaturalPerson("John Doe");
  }

  public static NaturalPerson createMockedNaturalPerson(String name) {
    return NaturalPerson.builder()
        .name(name)
        .email("john.doe@company.com")
        .phone("+55 11 99999-9999")
        .personDocumentType(PersonDocumentType.CPF)
        .documentNumber("1234")
        .aliasName("Johnny")
        .gender(Gender.MALE)
        .genderPronoun(GenderPronoun.HE)
        .address(createMockedAddress())
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Category createMockedCategory() {
    return Category.builder()
        .name("Test Category")
        .description("Test category description")
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Category createMockedCategory(String name, String description) {
    return Category.builder()
        .name(name)
        .description(description)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Pillar createMockedPillar() {
    Map<String, Category> categoryMap = new HashMap<>();
    categoryMap.put("cat1", createMockedCategory("Category 1", "First category"));
    categoryMap.put("cat2", createMockedCategory("Category 2", "Second category"));
    categoryMap.put("cat3", createMockedCategory("Category 3", "Third category"));

    return Pillar.builder()
        .name("Test Pillar")
        .description("Test pillar description")
        .categoryMap(categoryMap)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Pillar createMockedPillar(String name, String description) {
    Map<String, Category> categoryMap = new HashMap<>();
    categoryMap.put("default", createMockedCategory("Default Category", "Default category for " + name));

    return Pillar.builder()
        .name(name)
        .description(description)
        .categoryMap(categoryMap)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Pillar createMockedPillarWithCategories(String name, String description, Map<String, Category> categories) {
    return Pillar.builder()
        .name(name)
        .description(description)
        .categoryMap(categories)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Map<String, Category> createMockedCategoryMap(int categoryCount) {
    Map<String, Category> categoryMap = new HashMap<>();
    for (int i = 1; i <= categoryCount; i++) {
      String key = "category" + i;
      categoryMap.put(key, createMockedCategory("Category " + i, "Description for category " + i));
    }
    return categoryMap;
  }

  public static Map<String, Pillar> createMockedPillarMap(int pillarCount) {
    Map<String, Pillar> pillarMap = new HashMap<>();
    for (int i = 1; i <= pillarCount; i++) {
      String key = "pillar" + i;
      pillarMap.put(key, createMockedPillar("Pillar " + i, "Description for pillar " + i));
    }
    return pillarMap;
  }

  // === Question  Factory Methods ===

  public static Question createMockedQuestion() {
    return createMockedQuestion(5d);
  }

  public static Question createMockedQuestion(Double points) {
    return createMockedQuestion(GENERIC_ID_1234, points);
  }

  public static Question createMockedQuestion(String id) {
    return createMockedQuestion(id, 5d);
  }

  public static Question createMockedQuestion(String id, Double points) {
    return createMockedQuestion(id, QuestionType.YES_NO, GENERIC_ID_1234, "Pillar Name", GENERIC_ID_1234,
        "Category Name", points);
  }

  public static Question createMockedQuestion(Double points, QuestionType questionType) {
    return createMockedQuestion(GENERIC_ID_1234, questionType, GENERIC_ID_1234, "Pillar Name", GENERIC_ID_1234,
        "Category Name", points);
  }

  public static Question createMockedQuestion(String id, QuestionType questionType, @NonNull String pillarId,
                                                @NonNull String pillarName, @NonNull String categoryId,
                                                @NonNull String categoryName, Double points) {
    return Question.builder()
        .id(id)
        .assessmentMatrixId(GENERIC_ID_1234)
        .pillarId(pillarId)
        .pillarName(pillarName)
        .categoryId(categoryId)
        .categoryName(categoryName)
        .question("question")
        .questionType(questionType)
        .points(points)
        .tenantId(GENERIC_TENANT_ID)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static Question createMockedQuestion(String id, QuestionType questionType) {
    Question question = createMockedQuestion(id);
    question.setQuestionType(questionType);
    return question;
  }
}