package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.entity.person.Address;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import lombok.NonNull;
import org.apache.commons.lang3.SerializationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestObjectFactory {

  public static final String GENERIC_ID_1234 = "1234";
  public static final String EMPLOYEE_NAME_JOHN = "John";

  public static Answer createMockedAnswer(@NonNull Double score) {
    return Answer.builder()
        .employeeAssessmentId(GENERIC_ID_1234)
        .pillarId(GENERIC_ID_1234)
        .categoryId(GENERIC_ID_1234)
        .questionId(GENERIC_ID_1234)
        .questionType(QuestionType.CUSTOMIZED)
        .question(createMockedQuestion(GENERIC_ID_1234))
        .answeredAt(LocalDateTime.now())
        .value("3")
        .tenantId("tenantId")
        .score(score)
        .build();
  }

  public static Answer createMockedAnswer(@NonNull String employeeAssessmentId,
                                          @NonNull String tenantId, @NonNull Double score,
                                          @NonNull Question question) {
    return Answer.builder()
        .employeeAssessmentId(employeeAssessmentId)
        .pillarId(question.getPillarId())
        .categoryId(question.getCategoryId())
        .questionId(question.getId())
        .questionType(question.getQuestionType())
        .question(question)
        .answeredAt(LocalDateTime.now())
        .value("3")
        .tenantId(tenantId)
        .score(score)
        .build();
  }

  public static Answer createMockedAnswer(String id, @NonNull String dependenciesId,
                                          @NonNull Question question, @NonNull QuestionType questionType,
                                          @NonNull LocalDateTime answeredAt, @NonNull String value, @NonNull Double score) {
    return Answer.builder()
        .id(id)
        .employeeAssessmentId(dependenciesId)
        .pillarId(dependenciesId)
        .categoryId(dependenciesId)
        .questionId(dependenciesId)
        .question(question)
        .questionType(questionType)
        .answeredAt(answeredAt)
        .value(value)
        .tenantId("tenantId")
        .score(score)
        .build();
  }

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
    return createMockedQuestion(id, QuestionType.YES_NO, GENERIC_ID_1234, "Pillar Name", GENERIC_ID_1234, "Category " +
        "Name", points);
  }

  public static Question createMockedQuestion(Double points, QuestionType questionType) {
    return createMockedQuestion(GENERIC_ID_1234, questionType, GENERIC_ID_1234, "Pillar Name", GENERIC_ID_1234, "Category " +
        "Name", points);
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
        .tenantId("tenantId")
        .points(points)
        .build();
  }

  public static Question createMockedQuestion(String id, QuestionType questionType) {
    Question question = createMockedQuestion(id);
    question.setQuestionType(questionType);
    return question;
  }

  public static Question createMockedCustomQuestion(String id) {
    return createMockedCustomQuestion(id, false);
  }

  public static Question createMockedCustomQuestion(String id, boolean isMultipleChoice) {
    return createMockedCustomQuestion(id, isMultipleChoice, 0d, 5d, 10d, 20d, 30d);
  }

  public static Question createMockedCustomQuestion(String id, boolean isMultipleChoice, Double... points) {
    return createMockedCustomQuestion(id, isMultipleChoice, GENERIC_ID_1234, "Pillar Name", GENERIC_ID_1234,
        "Category Name", points);
  }

  public static Question createMockedCustomQuestion(String id, boolean isMultipleChoice, @NonNull String pillarId,
                                                    @NonNull String pillarName, @NonNull String categoryId,
                                                    @NonNull String categoryName,
                                                    Double... points) {
    return Question.builder()
        .id(id)
        .assessmentMatrixId(GENERIC_ID_1234)
        .pillarId(pillarId)
        .pillarName(pillarName)
        .categoryId(categoryId)
        .categoryName(categoryName)
        .question("question")
        .questionType(QuestionType.CUSTOMIZED)
        .tenantId("tenantId")
        .optionGroup(createMockedOptionGroup(isMultipleChoice, points))
        .build();
  }

  public static OptionGroup createMockedOptionGroup(boolean isMultipleChoice, Double... points) {
    return createMockedOptionGroup(isMultipleChoice, "OptionPrefix", points);
  }

  public static OptionGroup createMockedOptionGroup(boolean isMultipleChoice, String prefix, Double... points) {
    return OptionGroup.builder()
        .isMultipleChoice(isMultipleChoice)
        .showFlushed(true)
        .optionMap(createMockedQuestionOptionMap(prefix, points))
        .build();
  }

  public static List<QuestionOption> createMockedQuestionOptionList(String prefix, Double... points) {
    return IntStream.range(0, points.length)
        .mapToObj(index -> createQuestionOption(index + 1, prefix, points[index]))
        .collect(Collectors.toList());
  }

  public static Map<Integer, QuestionOption> createMockedQuestionOptionMap(String prefix, Double... points) {
    return IntStream.range(0, points.length)
        .mapToObj(index -> createQuestionOption(index + 1, prefix, points[index]))
        .collect(Collectors.toMap(QuestionOption::getId, Function.identity()));
  }

  public static QuestionOption createQuestionOption(Integer id, String prefix, Double points) {
    return QuestionOption.builder()
        .id(id)
        .text(buildPrefix(prefix, id))
        .points(points)
        .build();
  }

  // Returns empty if prefix is empty. Otherwise will return "prefix" + id.
  private static String buildPrefix(String prefix, Integer id) {
    return prefix.isEmpty() ? "" : prefix + id;
  }

  public static Question copyQuestionAndAddId(Question question, String id) {
    return cloneWithId(question, id);
  }

  public static Company createMockedCompany() {
    return Company.builder()
        .name("Company Name")
        .description("Company description")
        .tenantId("Random Tenant Id")
        .email("company@email.com")
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber("0001")
        .size(CompanySize.MEDIUM)
        .industry(Industry.TECHNOLOGY)
        .website("https://www.company.com")
        .legalName("Company Legal Name Inc.")
        .contactPerson(createMockedNaturalPerson("Contact Person"))
        .address(createMockedAddress())
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
        .website(company.getWebsite())
        .legalName(company.getLegalName())
        .contactPerson(company.getContactPerson())
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
        .answeredQuestionCount(0)
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

  public static Address createMockedAddress() {
    return Address.builder()
        .street("123 Main Street")
        .city("São Paulo")
        .state("SP")
        .zipcode("01234-567")
        .country("Brazil")
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

  public static AssessmentMatrix createMockedAssessmentMatrix(String dependenciesId, String id, Map<String, Pillar> pillarMap) {
    return cloneWithId(createMockedAssessmentMatrixWithDependenciesId(dependenciesId, pillarMap), id);
  }

  public static AssessmentMatrix createMockedAssessmentMatrixWithDependenciesId(String dependenciesId, Map<String, Pillar> pillarMap) {
    return AssessmentMatrix.builder()
        .id(dependenciesId)
        .name("AsssessmentMatrixName")
        .description("AssessmentMatrix description")
        .tenantId("tenantId")
        .performanceCycleId(dependenciesId)
        .pillarMap(pillarMap)
        .build();
  }

  public static PerformanceCycle createMockedPerformanceCycle(String companyId, String id) {
    return cloneWithId(createMockedPerformanceCycleWithDependenciesId(companyId), id);
  }

  public static Pillar createMockedPillar(@NonNull String name, @NonNull String description, Map<String, Category> categoryMap) {
    return Pillar.builder()
        .name(name)
        .description(description)
        .categoryMap(categoryMap)
        .build();
  }

  public static Map<String, Pillar> createMockedPillarMap(Integer pillarSize, Integer categorySize, String pillarPrefix, String categoryPrefix) {
    return IntStream.range(1, pillarSize + 1)
        .mapToObj(i -> createMockedPillar(
            "Pillar Name", // strName(pillarPrefix, i),
            strDescription(pillarPrefix, i),
            IntStream.range(1, categorySize + 1)
                .mapToObj(ci -> createMockedCategory(
                    "Category Name", //strName(categoryPrefix + i, ci),
                    strDescription(categoryPrefix + i, ci)))
                .collect(Collectors.toMap(Category::getId, Function.identity()))
        )).collect(Collectors.toMap(Pillar::getId, Function.identity()));
  }

  @SuppressWarnings("unused")
  private static String strName(String prefix, Integer i) {
    return str(prefix + " Name - ", i);
  }

  private static String strDescription(String prefix, Integer i) {
    return str(prefix + " Description - ", i);
  }

  private static String str(String prefix, Integer i) {
    return prefix + " " + i;
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
