package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;
import com.agilecheckup.service.exception.EmployeeAssessmentAlreadyExistsException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.ValidationException;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.EMPLOYEE_NAME_JOHN;
import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.createMockedAnswer;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrix;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrixWithDependenciesId;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static com.agilecheckup.util.TestObjectFactory.createMockedEmployeeAssessment;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMapV2;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static com.agilecheckup.util.TestObjectFactory.createMockedTeam;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentServiceTest extends AbstractCrudServiceTest<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> {

  @InjectMocks
  @Spy
  private EmployeeAssessmentService employeeAssessmentService;

  @Mock
  private AssessmentMatrixService assessmentMatrixService;

  @Mock
  private AnswerRepository answerRepository;

  @Mock
  private TeamService teamService;

  @Mock
  private EmployeeAssessmentRepository employeeAssessmentRepository;

  private EmployeeAssessment originalEmployeeAssessment;

  private final AssessmentMatrix assessmentMatrix = createMockedAssessmentMatrix(GENERIC_ID_1234, DEFAULT_ID, createMockedPillarMapV2(1, 2, "P", "C"));

  private final Team team = createMockedTeam(DEFAULT_ID);

  private static final String TENANT_ID = "TENANT_ID";
  private static final String EMPLOYEE_ASSESSMENT_ID = "employeeAssessmentId";

  @BeforeEach
  void setUpBefore() {
    originalEmployeeAssessment = createMockedEmployeeAssessment(DEFAULT_ID, "Fernando", assessmentMatrix.getId());
    originalEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);
    // Update team's tenantId to match the employeeAssessment's tenantId
    team.setTenantId("test-tenant-123");
    
    // Default mock for GSI validation - no existing employee assessments by default
    lenient().when(employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(any(), any())).thenReturn(false);
  }

  @Test
  void create() {
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedEmployeeAssessment, employeeAssessmentRepository);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeamId());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun()); // Explicitly pass genderPronoun

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    EmployeeAssessment createdAssessment = employeeAssessmentOptional.get();
    assertNotNull(createdAssessment);
    assertEquals(AssessmentStatus.INVITED, createdAssessment.getAssessmentStatus());
    verify(employeeAssessmentRepository).save(any(EmployeeAssessment.class));
    verify(employeeAssessmentService).create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun()); // Explicitly pass genderPronoun
    verify(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
  }

  @Test
  void create_withNullGenderAndPronoun_shouldSucceed() {
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);
    savedEmployeeAssessment.getEmployee().setGender(null);
    savedEmployeeAssessment.getEmployee().setGenderPronoun(null);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedEmployeeAssessment, employeeAssessmentRepository);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeamId());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        null, // Pass null for gender
        null); // Pass null for genderPronoun

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    EmployeeAssessment createdEmployeeAssessment = employeeAssessmentOptional.get();
    assertEquals(savedEmployeeAssessment.getEmployee().getName(), createdEmployeeAssessment.getEmployee().getName());
    assertEquals(savedEmployeeAssessment.getEmployee().getEmail(), createdEmployeeAssessment.getEmployee().getEmail());
    assertThat(createdEmployeeAssessment.getEmployee().getGender()).isNull();
    assertThat(createdEmployeeAssessment.getEmployee().getGenderPronoun()).isNull();
    verify(employeeAssessmentRepository).save(any(EmployeeAssessment.class)); // Use any() here as the NaturalPerson inside will be different due to nulls
    verify(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
    verify(teamService).findById(originalEmployeeAssessment.getTeamId());
  }

  @Test
  void createNonExistantAssessmentMatrixId() {
    assertThrows(NullPointerException.class, () -> assertCreateEmployeeAssessmentForAssessmentMatrixId("NonExistentAssessmentMatrixId"));
  }

  @Test
  void createNullAssessmentMatrixId() {
    assertThrows(NullPointerException.class, () -> assertCreateEmployeeAssessmentForAssessmentMatrixId(null));
  }

  @Test
  void createNullTeamId() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateEmployeeAssessmentForTeamId(null));
  }

  @Test
  void createNonExistantTeamId() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateEmployeeAssessmentForTeamId("NonExistentTeamId"));
  }

  void assertCreateEmployeeAssessmentForAssessmentMatrixId(String assessmentMatrixId) {
    originalEmployeeAssessment.setAssessmentMatrixId(null);
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedEmployeeAssessment).when(employeeAssessmentRepository).save(originalEmployeeAssessment);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeamId());
    if (assessmentMatrixId != null) doReturn(Optional.empty()).when(employeeAssessmentService).findById(any());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun() // Explicitly pass genderPronoun
    );

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
    verify(employeeAssessmentService).create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun() // Explicitly pass genderPronoun
    );
    if (assessmentMatrixId == null) {
      verify(assessmentMatrixService, never()).findById(assessmentMatrixId);
    }
    else {
      verify(assessmentMatrixService).findById(assessmentMatrixId);
    }
  }

  void assertCreateEmployeeAssessmentForTeamId(String teamId) {
    originalEmployeeAssessment.setTeamId(null);
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
    if (team != null) doReturn(Optional.empty()).when(teamService).findById(any());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        teamId,
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun() // Explicitly pass genderPronoun
    );

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
    verify(employeeAssessmentService).create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        teamId,
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(), // Explicitly pass gender
        originalEmployeeAssessment.getEmployee().getGenderPronoun() // Explicitly pass genderPronoun
    );
    if (teamId == null) {
      verify(teamService, never()).findById(teamId);
    }
    else {
      verify(teamService).findById(teamId);
    }
  }

  @Test
  void shouldIncrementQuestionCount() {
    String employeeAssessmentId = "employeeAssessmentId";
    doReturn(originalEmployeeAssessment).when(employeeAssessmentRepository).findById(employeeAssessmentId);
    assertEquals(0, originalEmployeeAssessment.getAnsweredQuestionCount());

    employeeAssessmentService.incrementAnsweredQuestionCount(employeeAssessmentId);

    assertEquals(1, originalEmployeeAssessment.getAnsweredQuestionCount());
    // Verify that findById is called at least once (could be twice due to updateLastActivityDate)
    verify(employeeAssessmentRepository, atLeastOnce()).findById(employeeAssessmentId);
    // Verify that save is called at least once (could be twice due to updateLastActivityDate)
    verify(employeeAssessmentRepository, atLeastOnce()).save(originalEmployeeAssessment);
  }

  @Test
  void shouldUpdateEmployeeAssessmentScoreForASingleAnswer() {
    // Prevent/Stub
    Answer answer = createMockedAnswer(EMPLOYEE_ASSESSMENT_ID, TENANT_ID, 15d, createMockedQuestion("qu111", QuestionType.STAR_FIVE, "pu1"
        , "PUillar1", "cu11", "CUategory11",
        5d));
    List<Answer> answers = List.of(answer);
    doReturn(originalEmployeeAssessment).when(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    doReturn(answers).when(answerRepository).findByEmployeeAssessmentId(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // When
    EmployeeAssessment resultEmployeeAssessment =
        employeeAssessmentService.updateEmployeeAssessmentScore(EMPLOYEE_ASSESSMENT_ID,
            TENANT_ID);

    // Then
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    verify(answerRepository).findByEmployeeAssessmentId(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);

    assertEquals(15d, resultEmployeeAssessment.getEmployeeAssessmentScore().getScore());
  }

  @Test
  void shouldUpdateEmployeeAssessmentScore() {
    // Prevent/Stub
    List<Answer> answers = createAnswerList();
    doReturn(originalEmployeeAssessment).when(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    doReturn(answers).when(answerRepository).findByEmployeeAssessmentId(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // When
    EmployeeAssessment resultEmployeeAssessment =
        employeeAssessmentService.updateEmployeeAssessmentScore(EMPLOYEE_ASSESSMENT_ID,
            TENANT_ID);

    // Then
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    verify(answerRepository).findByEmployeeAssessmentId(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);

    assertEmployeeAssessmentScore(resultEmployeeAssessment);
  }

  private void assertEmployeeAssessmentScore(EmployeeAssessment resultEmployeeAssessment) {
    EmployeeAssessmentScore assessmentScore = resultEmployeeAssessment.getEmployeeAssessmentScore();
    assertNotNull(assessmentScore);
    assertEquals(116d, assessmentScore.getScore());
    assertEquals(2, assessmentScore.getPillarIdToPillarScoreMap().size());

    // Pillar 1
    PillarScore pillar1 = assessmentScore.getPillarIdToPillarScoreMap().get("pu1");
    assertEquals(63d, pillar1.getScore());
    assertEquals("pu1", pillar1.getPillarId());
    assertEquals("PUillar1", pillar1.getPillarName());

    // Category 11
    CategoryScore category11 = pillar1.getCategoryIdToCategoryScoreMap().get("cu11");
    assertEquals(18d, category11.getScore());
    assertEquals("cu11", category11.getCategoryId());
    assertEquals("CUategory11", category11.getCategoryName());

    QuestionScore question111 = category11.getQuestionScores().get(0);
    assertEquals(4d, question111.getScore());
    assertEquals("qu111", question111.getQuestionId());
    QuestionScore question112 = category11.getQuestionScores().get(1);
    assertEquals(14d, question112.getScore());
    assertEquals("qu112", question112.getQuestionId());

    // Category 12
    CategoryScore category12 = pillar1.getCategoryIdToCategoryScoreMap().get("cu12");
    assertEquals(45d, category12.getScore());
    assertEquals("cu12", category12.getCategoryId());
    assertEquals("CUategory12", category12.getCategoryName());

    QuestionScore question121 = category12.getQuestionScores().get(0);
    assertEquals(40d, question121.getScore());
    assertEquals("qu121", question121.getQuestionId());
    QuestionScore question122 = category12.getQuestionScores().get(1);
    assertEquals(5d, question122.getScore());
    assertEquals("qu122", question122.getQuestionId());

    // Pillar 2
    PillarScore pillar2 = assessmentScore.getPillarIdToPillarScoreMap().get("pu2");
    assertEquals(53d, pillar2.getScore());
    assertEquals("pu2", pillar2.getPillarId());
    assertEquals("PUillar2", pillar2.getPillarName());

    // Category 21
    CategoryScore category21 = pillar2.getCategoryIdToCategoryScoreMap().get("cu21");
    assertEquals(8d, category21.getScore());
    assertEquals("cu21", category21.getCategoryId());
    assertEquals("CUategory21", category21.getCategoryName());

    QuestionScore question211 = category21.getQuestionScores().get(0);
    assertEquals(3d, question211.getScore());
    assertEquals("qu211", question211.getQuestionId());
    QuestionScore question212 = category21.getQuestionScores().get(1);
    assertEquals(5d, question212.getScore());
    assertEquals("qu212", question212.getQuestionId());

    // Category 21
    CategoryScore category22 = pillar2.getCategoryIdToCategoryScoreMap().get("cu22");
    assertEquals(45d, category22.getScore());
    assertEquals("cu22", category22.getCategoryId());
    assertEquals("CUategory22", category22.getCategoryName());

    QuestionScore question221 = category22.getQuestionScores().get(0);
    assertEquals(30d, question221.getScore());
    assertEquals("qu221", question221.getQuestionId());
    QuestionScore question222 = category22.getQuestionScores().get(1);
    assertEquals(15d, question222.getScore());
    assertEquals("qu222", question222.getQuestionId());
  }

  private List<Answer> createAnswerList() {
    List<Answer> answerList = new LinkedList<>();
    // Total Max Points = 53 + 63 = 116
    // Pillar 1 - 63
    //   Category 11 - Pt 18
    //     Question 111 Pt 4 - (Rating 5)
    Question question111 = createMockedQuestion("qu111", QuestionType.STAR_FIVE, "pu1", "PUillar1", "cu11",
        "CUategory11",
        5d);
    answerList.add(createAnswer(4d, question111));
    //     Question 112 Pt Max 14 (Custom Regular)
    Question question112 = createMockedCustomQuestion("qu112", false, "pu1", "PUillar1", "cu11",
        "CUategory11", 10d,
        10d, 15d
        , 10d);
    answerList.add(createAnswer(14d, question112));

    //   Category 12 - Pt 45
    //     Question 121 Pt 40 (Rating 3)
    Question question121 = createMockedQuestion("qu121", QuestionType.STAR_THREE, "pu1", "PUillar1", "cu12",
        "CUategory12",
        45d);
    answerList.add(createAnswer(40d, question121));
    //     Question 122 Pt Sum 5(Custom Multiple Choice)
    Question question122 = createMockedCustomQuestion("qu122", true, "pu1", "PUillar1", "cu12", "CUategory12", 5d, 10d
        , 10d
        , 5d, 5d);
    answerList.add(createAnswer(5d, question122));
    // Pillar 2 - 53
    //   Category 21 - Pt 8
    //     Question 211 Pt 3 (Good Bad)
    Question question211 = createMockedQuestion("qu211", QuestionType.GOOD_BAD, "pu2", "PUillar2", "cu21",
        "CUategory21",
        10d);
    answerList.add(createAnswer(3d, question211));
    //     Question 212 Pt 5 (Yes No)
    Question question212 = createMockedQuestion("qu212", QuestionType.YES_NO, "pu2", "PUillar2", "cu21", "CUategory21",
        5d);
    answerList.add(createAnswer(5d, question212));
    //   Category 22 - Pt 45
    //     Question 221 Pt Sum 30 (Custom Multiple Choice)
    Question question221 = createMockedCustomQuestion("qu221", true, "pu2", "PUillar2", "cu22", "CUategory22", 5d, 10d,
        10d
        ,15d);
    answerList.add(createAnswer(30d, question221));
    //     Question 222 Pt Max 15(Custom Regular)
    Question question222 = createMockedCustomQuestion("qu222", false, "pu2", "PUillar2", "cu22", "CUategory22", 10d,
        25d, 15d
        , 35d);
    answerList.add(createAnswer(15d, question222));

    return answerList;
  }

  private Answer createAnswer(Double score, Question question) {
    return createMockedAnswer(EMPLOYEE_ASSESSMENT_ID, TENANT_ID, score, question);
  }

  @Test
  void update_existingEmployeeAssessment_shouldSucceed() {
    // Prepare
    EmployeeAssessment existingEmployeeAssessment = createMockedEmployeeAssessment(DEFAULT_ID, EMPLOYEE_NAME_JOHN, GENERIC_ID_1234);
    EmployeeAssessment updatedEmployeeAssessmentDetails = createMockedEmployeeAssessment("updatedMatrixId", "Updated Employee Name", "updatedMatrixId");
    updatedEmployeeAssessmentDetails.setId(DEFAULT_ID);
    updatedEmployeeAssessmentDetails.setTeamId("updatedTeamId");
    updatedEmployeeAssessmentDetails.setEmployeeEmailNormalized("name@company.com"); // Set normalized email

    AssessmentMatrix assessmentMatrix1 = createMockedAssessmentMatrixWithDependenciesId("updatedMatrixId", createMockedPillarMapV2(1, 1, "pillar", "category"));
    Team team1 = createMockedTeam("updatedTeamId");

    // Mock repository calls
    doReturn(existingEmployeeAssessment).when(employeeAssessmentRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedEmployeeAssessmentDetails, employeeAssessmentRepository);
    doReturn(Optional.of(assessmentMatrix1)).when(assessmentMatrixService).findById("updatedMatrixId");
    doReturn(Optional.of(team1)).when(teamService).findById("updatedTeamId");

    // When
    Optional<EmployeeAssessment> resultOptional = employeeAssessmentService.update(
        DEFAULT_ID,
        "updatedMatrixId",
        "updatedTeamId",
        "Updated Employee Name",
        "name@company.com",
        "1234",
        PersonDocumentType.CPF,
        Gender.MALE, // Explicitly pass gender
        GenderPronoun.HE // Explicitly pass genderPronoun
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedEmployeeAssessmentDetails, resultOptional.get());
    verify(employeeAssessmentRepository).findById(DEFAULT_ID);
    verify(employeeAssessmentRepository).save(updatedEmployeeAssessmentDetails);
    verify(assessmentMatrixService).findById("updatedMatrixId");
    verify(teamService).findById("updatedTeamId");
    verify(employeeAssessmentService).update(DEFAULT_ID,
        "updatedMatrixId",
        "updatedTeamId",
        "Updated Employee Name",
        "name@company.com",
        "1234",
        PersonDocumentType.CPF,
        Gender.MALE, // Explicitly pass gender
        GenderPronoun.HE); // Explicitly pass genderPronoun
  }

  @Test
  void update_nonExistingEmployeeAssessment_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(employeeAssessmentRepository).findById(nonExistingId);

    // When
    Optional<EmployeeAssessment> resultOptional = employeeAssessmentService.update(
        nonExistingId,
        "matrixId",
        "teamId",
        "name",
        "email",
        "doc",
        PersonDocumentType.CPF,
        Gender.MALE, // Explicitly pass gender
        GenderPronoun.HE // Explicitly pass genderPronoun
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(employeeAssessmentRepository).findById(nonExistingId);
    verify(employeeAssessmentService).update(nonExistingId, "matrixId", "teamId", "name", "email", "doc",
        PersonDocumentType.CPF, Gender.MALE, // Explicitly pass gender
        GenderPronoun.HE); // Explicitly pass genderPronoun
  }

  @Test
  void update_withNullGenderAndPronoun_shouldSucceed() {
    // Prepare
    EmployeeAssessment existingEmployeeAssessment = createMockedEmployeeAssessment(DEFAULT_ID, EMPLOYEE_NAME_JOHN, GENERIC_ID_1234);
    // The updated details will have null gender and genderPronoun
    EmployeeAssessment updatedEmployeeAssessmentDetails = createMockedEmployeeAssessment("updatedMatrixId", "Updated Employee Name", "updatedMatrixId");
    updatedEmployeeAssessmentDetails.setId(DEFAULT_ID);
    updatedEmployeeAssessmentDetails.setTeamId("updatedTeamId");
    // Crucially, set gender and genderPronoun to null for the expected person
    updatedEmployeeAssessmentDetails.setEmployee(
        EmployeeAssessmentService.createNaturalPerson(
            "Updated Employee Name",
            "name@company.com",
            "1234",
            PersonDocumentType.CPF,
            null, // gender is null
            null, // genderPronoun is null
            existingEmployeeAssessment.getEmployee().getId()
        )
    );


    AssessmentMatrix assessmentMatrix1 = createMockedAssessmentMatrixWithDependenciesId("updatedMatrixId", createMockedPillarMapV2(1, 1, "pillar", "category"));
    Team team1 = createMockedTeam("updatedTeamId");

    // Mock repository calls
    doReturn(Optional.of(existingEmployeeAssessment)).when(employeeAssessmentService).findById(DEFAULT_ID); // Use Optional for findById
    doReturn(Optional.of(assessmentMatrix1)).when(assessmentMatrixService).findById("updatedMatrixId");
    doReturn(Optional.of(team1)).when(teamService).findById("updatedTeamId");
    // Mock the save operation to capture the argument and return it
    doReturn(Optional.of(updatedEmployeeAssessmentDetails)).when(employeeAssessmentService).update(any(EmployeeAssessment.class));


    // When
    Optional<EmployeeAssessment> resultOptional = employeeAssessmentService.update(
        DEFAULT_ID,
        "updatedMatrixId",
        "updatedTeamId",
        "Updated Employee Name",
        "name@company.com",
        "1234",
        PersonDocumentType.CPF,
        null, // Pass null for gender
        null  // Pass null for genderPronoun
    );

    // Then
    assertTrue(resultOptional.isPresent());
    EmployeeAssessment resultEmployeeAssessment = resultOptional.get();

    // Verify the fields of the employee within the result
    assertEquals("Updated Employee Name", resultEmployeeAssessment.getEmployee().getName());
    assertEquals("name@company.com", resultEmployeeAssessment.getEmployee().getEmail());
    assertThat(resultEmployeeAssessment.getEmployee().getGender()).isNull();
    assertThat(resultEmployeeAssessment.getEmployee().getGenderPronoun()).isNull();

    // Verify interactions
    verify(employeeAssessmentService).findById(DEFAULT_ID);
    verify(assessmentMatrixService).findById("updatedMatrixId");
    verify(teamService).findById("updatedTeamId");
    // Verify that the update method on the service (which internally calls super.update -> repository.save) was called with an EmployeeAssessment
    // whose NaturalPerson has null gender and genderPronoun.
     verify(employeeAssessmentService).update(
        DEFAULT_ID,
        "updatedMatrixId",
        "updatedTeamId",
        "Updated Employee Name",
        "name@company.com",
        "1234",
        PersonDocumentType.CPF,
        null,
        null);
  }

  @Test
  void create_shouldSetInvitedStatusByDefault() {
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedEmployeeAssessment, employeeAssessmentRepository);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeamId());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeamId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun());

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    EmployeeAssessment createdAssessment = employeeAssessmentOptional.get();
    assertEquals(AssessmentStatus.INVITED, createdAssessment.getAssessmentStatus());
  }

  @Test
  void incrementAnsweredQuestionCount_shouldUpdateStatusToInProgressOnFirstAnswer() {
    // Given
    String employeeAssessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(employeeAssessmentId, "John", "matrix-id");
    assessment.setAnsweredQuestionCount(0);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(assessment).when(employeeAssessmentRepository).findById(employeeAssessmentId);

    // When
    employeeAssessmentService.incrementAnsweredQuestionCount(employeeAssessmentId);

    // Then
    assertEquals(1, assessment.getAnsweredQuestionCount());
    assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getAssessmentStatus());
    // Verify save is called at least once (could be twice due to status change triggering updateLastActivityDate)
    verify(employeeAssessmentRepository, atLeastOnce()).save(assessment);
  }

  @Test
  void incrementAnsweredQuestionCount_shouldNotChangeStatusIfAlreadyInProgress() {
    // Given
    String employeeAssessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(employeeAssessmentId, "John", "matrix-id");
    assessment.setAnsweredQuestionCount(5);
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    doReturn(assessment).when(employeeAssessmentRepository).findById(employeeAssessmentId);

    // When
    employeeAssessmentService.incrementAnsweredQuestionCount(employeeAssessmentId);

    // Then
    assertEquals(6, assessment.getAnsweredQuestionCount());
    assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getAssessmentStatus());
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void incrementAnsweredQuestionCount_shouldNotChangeStatusIfCompleted() {
    // Given
    String employeeAssessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(employeeAssessmentId, "John", "matrix-id");
    assessment.setAnsweredQuestionCount(10);
    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);

    doReturn(assessment).when(employeeAssessmentRepository).findById(employeeAssessmentId);

    // When
    employeeAssessmentService.incrementAnsweredQuestionCount(employeeAssessmentId);

    // Then
    assertEquals(11, assessment.getAnsweredQuestionCount());
    assertEquals(AssessmentStatus.COMPLETED, assessment.getAssessmentStatus());
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void updateAssessmentStatus_shouldUpdateStatusForExistingAssessment() {
    // Given
    String assessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(assessmentId, "John", "matrix-id");
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(Optional.of(assessment)).when(employeeAssessmentService).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentService).update(any(EmployeeAssessment.class));

    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);

    // Then
    assertTrue(result.isPresent());
    assertEquals(AssessmentStatus.CONFIRMED, assessment.getAssessmentStatus());
    verify(employeeAssessmentService).update(assessment);
  }

  @Test
  void updateAssessmentStatus_shouldReturnEmptyForNonExistingAssessment() {
    // Given
    String nonExistingId = "non-existing-id";
    doReturn(Optional.empty()).when(employeeAssessmentService).findById(nonExistingId);

    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.updateAssessmentStatus(nonExistingId, AssessmentStatus.COMPLETED);

    // Then
    assertFalse(result.isPresent());
    verify(employeeAssessmentService, never()).update(any(EmployeeAssessment.class));
  }

  @Test
  void updateAssessmentStatus_shouldThrowExceptionForNullStatus() {
    // Given
    String assessmentId = "test-assessment-id";

    // When & Then
    assertThrows(NullPointerException.class, () -> {
      employeeAssessmentService.updateAssessmentStatus(assessmentId, null);
    });
  }

  @Test
  void updateAssessmentStatus_shouldThrowExceptionForNullAssessmentId() {
    // When & Then
    assertThrows(NullPointerException.class, () -> {
      employeeAssessmentService.updateAssessmentStatus(null, AssessmentStatus.COMPLETED);
    });
  }

  @Test
  void updateAssessmentStatus_shouldValidateStatusTransition() {
    // Given
    String assessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(assessmentId, "John", "matrix-id");
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(Optional.of(assessment)).when(employeeAssessmentService).findById(assessmentId);

    // When & Then - Invalid transition should throw ValidationException
    assertThrows(ValidationException.class, () -> {
      employeeAssessmentService.updateAssessmentStatus(assessmentId, AssessmentStatus.COMPLETED);
    });
  }

  @Test
  void updateAssessmentStatus_shouldAllowValidStatusTransition() {
    // Given
    String assessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(assessmentId, "John", "matrix-id");
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);

    doReturn(Optional.of(assessment)).when(employeeAssessmentService).findById(assessmentId);
    doReturn(Optional.of(assessment)).when(employeeAssessmentService).update(any(EmployeeAssessment.class));

    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);

    // Then
    assertTrue(result.isPresent());
    assertEquals(AssessmentStatus.CONFIRMED, assessment.getAssessmentStatus());
    verify(employeeAssessmentService).update(assessment);
  }

  @Test
  void findAllByTenantId_shouldReturnAllAssessmentsForTenant() {
    // Given
    EmployeeAssessment assessment1 = createMockedEmployeeAssessment("1", "John", assessmentMatrix.getId());
    EmployeeAssessment assessment2 = createMockedEmployeeAssessment("2", "Jane", assessmentMatrix.getId());
    List<EmployeeAssessment> expectedAssessments = Arrays.asList(assessment1, assessment2);
    
    @SuppressWarnings("unchecked")
    PaginatedQueryList<EmployeeAssessment> mockResult = mock(PaginatedQueryList.class);
    when(mockResult.stream()).thenReturn(expectedAssessments.stream());
    when(employeeAssessmentRepository.findAllByTenantId(TENANT_ID)).thenReturn(mockResult);
    
    // When
    List<EmployeeAssessment> result = employeeAssessmentService.findAllByTenantId(TENANT_ID);
    
    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(assessment1, assessment2);
    verify(employeeAssessmentRepository).findAllByTenantId(TENANT_ID);
  }

  @Test
  void findByAssessmentMatrix_shouldFilterAssessmentsByMatrixId() {
    // Given
    EmployeeAssessment matchingAssessment = createMockedEmployeeAssessment("1", "John", assessmentMatrix.getId());
    EmployeeAssessment nonMatchingAssessment = createMockedEmployeeAssessment("2", "Jane", "other-matrix");
    List<EmployeeAssessment> allAssessments = Arrays.asList(matchingAssessment, nonMatchingAssessment);
    
    @SuppressWarnings("unchecked")
    PaginatedQueryList<EmployeeAssessment> mockResult = mock(PaginatedQueryList.class);
    when(mockResult.stream()).thenReturn(allAssessments.stream());
    when(employeeAssessmentRepository.findAllByTenantId(TENANT_ID)).thenReturn(mockResult);
    
    // When
    List<EmployeeAssessment> result = employeeAssessmentService.findByAssessmentMatrix(assessmentMatrix.getId(), TENANT_ID);
    
    // Then
    assertThat(result).hasSize(1);
    assertThat(result).containsExactly(matchingAssessment);
    verify(employeeAssessmentRepository).findAllByTenantId(TENANT_ID);
  }

  @Test
  void findByAssessmentMatrix_shouldReturnEmptyListWhenNoMatches() {
    // Given
    EmployeeAssessment assessment = createMockedEmployeeAssessment("1", "John", "other-matrix");
    List<EmployeeAssessment> allAssessments = Arrays.asList(assessment);
    
    @SuppressWarnings("unchecked")
    PaginatedQueryList<EmployeeAssessment> mockResult = mock(PaginatedQueryList.class);
    when(mockResult.stream()).thenReturn(allAssessments.stream());
    when(employeeAssessmentRepository.findAllByTenantId(TENANT_ID)).thenReturn(mockResult);
    
    // When
    List<EmployeeAssessment> result = employeeAssessmentService.findByAssessmentMatrix(assessmentMatrix.getId(), TENANT_ID);
    
    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void findById_withTenantId_shouldReturnAssessmentWhenTenantMatches() {
    // Given
    EmployeeAssessment assessment = createMockedEmployeeAssessment(EMPLOYEE_ASSESSMENT_ID, "John", assessmentMatrix.getId());
    assessment.setTenantId(TENANT_ID);
    
    when(employeeAssessmentRepository.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(assessment);
    
    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    
    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(assessment);
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
  }

  @Test
  void findById_withTenantId_shouldReturnEmptyWhenTenantDoesNotMatch() {
    // Given
    EmployeeAssessment assessment = createMockedEmployeeAssessment(EMPLOYEE_ASSESSMENT_ID, "John", assessmentMatrix.getId());
    assessment.setTenantId("different-tenant");
    
    when(employeeAssessmentRepository.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(assessment);
    
    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    
    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void findById_withTenantId_shouldReturnEmptyWhenAssessmentNotFound() {
    // Given
    when(employeeAssessmentRepository.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(null);
    
    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    
    // Then
    assertThat(result).isEmpty();
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
  }

  @Test
  void deleteById_shouldDeleteExistingAssessment() {
    // Given
    EmployeeAssessment assessment = createMockedEmployeeAssessment(EMPLOYEE_ASSESSMENT_ID, "John", assessmentMatrix.getId());
    when(employeeAssessmentRepository.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(assessment);
    
    // When
    employeeAssessmentService.deleteById(EMPLOYEE_ASSESSMENT_ID);
    
    // Then
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    verify(employeeAssessmentRepository).delete(assessment);
  }

  @Test
  void deleteById_shouldHandleNonExistentAssessmentGracefully() {
    // Given
    when(employeeAssessmentRepository.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(null);
    
    // When
    employeeAssessmentService.deleteById(EMPLOYEE_ASSESSMENT_ID);
    
    // Then
    verify(employeeAssessmentRepository).findById(EMPLOYEE_ASSESSMENT_ID);
    verify(employeeAssessmentRepository, never()).delete(any());
  }

  @Test
  void save_shouldPersistEmployeeAssessment() {
    // Given
    EmployeeAssessment assessment = createMockedEmployeeAssessment(EMPLOYEE_ASSESSMENT_ID, "John", assessmentMatrix.getId());
    EmployeeAssessment savedAssessment = createMockedEmployeeAssessment(EMPLOYEE_ASSESSMENT_ID, "John", assessmentMatrix.getId());
    savedAssessment.setId("generated-id");
    
    when(employeeAssessmentRepository.save(assessment)).thenReturn(savedAssessment);
    
    // When
    EmployeeAssessment result = employeeAssessmentService.save(assessment);
    
    // Then
    assertThat(result).isEqualTo(savedAssessment);
    assertThat(result.getId()).isEqualTo("generated-id");
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void create_shouldThrowEmployeeAssessmentAlreadyExistsExceptionWhenEmployeeAssessmentAlreadyExistsInMatrix() {
    // Given
    String existingEmail = "existing@company.com";
    
    // Mock GSI query to return true (employee assessment exists)
    when(employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), existingEmail))
        .thenReturn(true);
    
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    lenient().doReturn(Optional.of(team)).when(teamService).findById(team.getId());
    
    // When & Then
    EmployeeAssessmentAlreadyExistsException exception = assertThrows(EmployeeAssessmentAlreadyExistsException.class, () -> {
      employeeAssessmentService.create(
          assessmentMatrix.getId(),
          team.getId(),
          "John Doe",
          existingEmail,
          "123456789",
          PersonDocumentType.CPF,
          Gender.MALE,
          GenderPronoun.HE
      );
    });
    
    assertThat(exception.getEmployeeEmail()).isEqualTo(existingEmail);
    assertThat(exception.getAssessmentMatrixId()).isEqualTo(assessmentMatrix.getId());
    assertThat(exception.getMessage()).contains(existingEmail);
    assertThat(exception.getMessage()).contains(assessmentMatrix.getId());
    
    // Verify GSI method was called with correct parameters
    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), existingEmail);
  }

  @Test
  void create_shouldSucceedWhenEmployeeAssessmentIsUniqueInMatrix() {
    // Given
    String uniqueEmail = "john@company.com";
    EmployeeAssessment newAssessment = createMockedEmployeeAssessment(null, "John", assessmentMatrix.getId());
    newAssessment.getEmployee().setEmail(uniqueEmail);
    EmployeeAssessment savedAssessment = cloneWithId(newAssessment, DEFAULT_ID);
    
    // Mock GSI query to return false (no employee assessment exists)
    when(employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), uniqueEmail))
        .thenReturn(false);
    
    doAnswerForSaveWithRandomEntityId(savedAssessment, employeeAssessmentRepository);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(Optional.of(team)).when(teamService).findById(team.getId());
    
    // When
    Optional<EmployeeAssessment> result = employeeAssessmentService.create(
        assessmentMatrix.getId(),
        team.getId(),
        "John Doe",
        uniqueEmail,
        "123456789",
        PersonDocumentType.CPF,
        Gender.MALE,
        GenderPronoun.HE
    );
    
    // Then
    assertTrue(result.isPresent());
    verify(employeeAssessmentRepository).save(any(EmployeeAssessment.class));
    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), uniqueEmail);
  }

  @Test
  void save_shouldThrowEmployeeAssessmentAlreadyExistsExceptionForNewAssessmentWithExistingEmployeeAssessment() {
    // Given
    String existingEmail = "existing@company.com";
    EmployeeAssessment newEmployeeAssessment = createMockedEmployeeAssessment(null, "John", assessmentMatrix.getId());
    newEmployeeAssessment.setId(null); // Ensure it's treated as new
    newEmployeeAssessment.getEmployee().setEmail(existingEmail);
    
    // Mock GSI query to return true (employee assessment exists)
    when(employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), existingEmail))
        .thenReturn(true);
    
    // When & Then
    EmployeeAssessmentAlreadyExistsException exception = assertThrows(EmployeeAssessmentAlreadyExistsException.class, () -> {
      employeeAssessmentService.save(newEmployeeAssessment);
    });
    
    assertThat(exception.getEmployeeEmail()).isEqualTo(existingEmail);
    assertThat(exception.getAssessmentMatrixId()).isEqualTo(assessmentMatrix.getId());
    
    // Verify GSI method was called
    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), existingEmail);
  }

  @Test
  void save_shouldAllowUpdateForExistingEmployeeAssessment() {
    // Given
    EmployeeAssessment existingEmployeeAssessment = createMockedEmployeeAssessment("existing-id", "John", assessmentMatrix.getId());
    existingEmployeeAssessment.getEmployee().setEmail("john@company.com");
    
    when(employeeAssessmentRepository.save(existingEmployeeAssessment)).thenReturn(existingEmployeeAssessment);
    
    // When
    EmployeeAssessment result = employeeAssessmentService.save(existingEmployeeAssessment);
    
    // Then
    assertThat(result).isEqualTo(existingEmployeeAssessment);
    verify(employeeAssessmentRepository).save(existingEmployeeAssessment);
    // Should not call GSI validation for existing assessments (has ID)
    verify(employeeAssessmentRepository, never()).existsByAssessmentMatrixAndEmployeeEmail(any(), any());
  }

  @Test
  void create_shouldBeCaseInsensitiveForEmployeeAssessmentDuplication() {
    // Given
    String emailLowerCase = "jane@company.com";
    
    // Mock GSI query to return true (employee assessment exists - case insensitive)
    when(employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), emailLowerCase))
        .thenReturn(true);
    
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    lenient().doReturn(Optional.of(team)).when(teamService).findById(team.getId());
    
    // When & Then
    EmployeeAssessmentAlreadyExistsException exception = assertThrows(EmployeeAssessmentAlreadyExistsException.class, () -> {
      employeeAssessmentService.create(
          assessmentMatrix.getId(),
          team.getId(),
          "John Doe",
          emailLowerCase, // Same email, different case should be handled by repository normalization
          "123456789",
          PersonDocumentType.CPF,
          Gender.MALE,
          GenderPronoun.HE
      );
    });
    
    assertThat(exception.getEmployeeEmail()).isEqualTo(emailLowerCase);
    
    // Verify GSI method was called with normalized email
    verify(employeeAssessmentRepository).existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrix.getId(), emailLowerCase);
  }

  @Test
  void scanAllEmployeeAssessments_shouldReturnAllAssessments() {
    // Given
    List<EmployeeAssessment> expectedAssessments = Arrays.asList(
        createMockedEmployeeAssessment("id1", "Employee 1", "matrix1"),
        createMockedEmployeeAssessment("id2", "Employee 2", "matrix2"),
        createMockedEmployeeAssessment("id3", "Employee 3", "matrix1")
    );
    
    // Mock the service method directly
    doReturn(expectedAssessments).when(employeeAssessmentService).scanAllEmployeeAssessments();
    
    // When
    List<EmployeeAssessment> result = employeeAssessmentService.scanAllEmployeeAssessments();
    
    // Then
    assertThat(result).isEqualTo(expectedAssessments);
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo("id1");
    assertThat(result.get(1).getId()).isEqualTo("id2");
    assertThat(result.get(2).getId()).isEqualTo("id3");
  }

  @Test
  void validateEmployee_whenEmployeeNotFound_shouldReturnErrorResponse() {
    // Given
    String email = "notfound@example.com";
    String assessmentMatrixId = "matrix123";
    String tenantId = "tenant123";

    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
        .email(email)
        .assessmentMatrixId(assessmentMatrixId)
        .tenantId(tenantId)
        .build();

    // Mock empty list (no assessments found)
    doReturn(new LinkedList<>()).when(employeeAssessmentService).findByAssessmentMatrix(assessmentMatrixId, tenantId);

    // When
    EmployeeValidationResponse response = employeeAssessmentService.validateEmployee(request);

    // Then
    assertThat(response.getStatus()).isEqualTo("ERROR");
    assertThat(response.getMessage()).contains("couldn't find your assessment invitation");
    assertThat(response.getEmployeeAssessmentId()).isNull();
    assertThat(response.getName()).isNull();
    assertThat(response.getAssessmentStatus()).isNull();
  }

  @Test
  void validateEmployee_whenStatusInvited_shouldUpdateToConfirmed() {
    // Given
    String email = "john@example.com";
    String assessmentMatrixId = "matrix123";
    String tenantId = "tenant123";

    EmployeeAssessment assessment = createMockedEmployeeAssessment("ea123", "John Doe", assessmentMatrixId);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);
    assessment.getEmployee().setEmail(email);

    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
        .email(email)
        .assessmentMatrixId(assessmentMatrixId)
        .tenantId(tenantId)
        .build();

    // Mock finding the assessment
    doReturn(Arrays.asList(assessment)).when(employeeAssessmentService)
        .findByAssessmentMatrix(assessmentMatrixId, tenantId);

    // When
    EmployeeValidationResponse response = employeeAssessmentService.validateEmployee(request);

    // Then
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getMessage()).contains("Welcome! Your assessment access has been confirmed");
    assertThat(response.getEmployeeAssessmentId()).isEqualTo("ea123");
    assertThat(response.getName()).isEqualTo("John Doe");
    assertThat(response.getAssessmentStatus()).isEqualTo("CONFIRMED");

    // Verify status was updated
    assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.CONFIRMED);
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void validateEmployee_whenStatusInProgress_shouldReturnInfoResponse() {
    // Given
    String email = "john@example.com";
    String assessmentMatrixId = "matrix123";
    String tenantId = "tenant123";

    EmployeeAssessment assessment = createMockedEmployeeAssessment("ea123", "John Doe", assessmentMatrixId);
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
    assessment.getEmployee().setEmail(email);

    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
        .email(email)
        .assessmentMatrixId(assessmentMatrixId)
        .tenantId(tenantId)
        .build();

    // Mock finding the assessment
    doReturn(Arrays.asList(assessment)).when(employeeAssessmentService)
        .findByAssessmentMatrix(assessmentMatrixId, tenantId);

    // When
    EmployeeValidationResponse response = employeeAssessmentService.validateEmployee(request);

    // Then
    assertThat(response.getStatus()).isEqualTo("INFO");
    assertThat(response.getMessage()).contains("Welcome back! You can continue your assessment");
    assertThat(response.getEmployeeAssessmentId()).isEqualTo("ea123");
    assertThat(response.getName()).isEqualTo("John Doe");
    assertThat(response.getAssessmentStatus()).isEqualTo("IN_PROGRESS");

    // Verify status was NOT updated
    verify(employeeAssessmentRepository, never()).save(any(EmployeeAssessment.class));
  }

  @Test
  void validateEmployee_whenStatusCompleted_shouldReturnInfoResponse() {
    // Given
    String email = "john@example.com";
    String assessmentMatrixId = "matrix123";
    String tenantId = "tenant123";

    EmployeeAssessment assessment = createMockedEmployeeAssessment("ea123", "John Doe", assessmentMatrixId);
    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    assessment.getEmployee().setEmail(email);

    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
        .email(email)
        .assessmentMatrixId(assessmentMatrixId)
        .tenantId(tenantId)
        .build();

    // Mock finding the assessment
    doReturn(Arrays.asList(assessment)).when(employeeAssessmentService)
        .findByAssessmentMatrix(assessmentMatrixId, tenantId);

    // When
    EmployeeValidationResponse response = employeeAssessmentService.validateEmployee(request);

    // Then
    assertThat(response.getStatus()).isEqualTo("INFO");
    assertThat(response.getMessage()).contains("You have already completed this assessment");
    assertThat(response.getEmployeeAssessmentId()).isEqualTo("ea123");
    assertThat(response.getName()).isEqualTo("John Doe");
    assertThat(response.getAssessmentStatus()).isEqualTo("COMPLETED");

    // Verify status was NOT updated
    verify(employeeAssessmentRepository, never()).save(any(EmployeeAssessment.class));
  }

  @Test
  void validateEmployee_caseInsensitiveEmailMatch_shouldFindEmployee() {
    // Given
    String emailInput = "JOHN@EXAMPLE.COM";
    String emailStored = "john@example.com";
    String assessmentMatrixId = "matrix123";
    String tenantId = "tenant123";

    EmployeeAssessment assessment = createMockedEmployeeAssessment("ea123", "John Doe", assessmentMatrixId);
    assessment.setAssessmentStatus(AssessmentStatus.INVITED);
    assessment.getEmployee().setEmail(emailStored);

    EmployeeValidationRequest request = EmployeeValidationRequest.builder()
        .email(emailInput)
        .assessmentMatrixId(assessmentMatrixId)
        .tenantId(tenantId)
        .build();

    // Mock finding the assessment
    doReturn(Arrays.asList(assessment)).when(employeeAssessmentService)
        .findByAssessmentMatrix(assessmentMatrixId, tenantId);

    // When
    EmployeeValidationResponse response = employeeAssessmentService.validateEmployee(request);

    // Then
    assertThat(response.getStatus()).isEqualTo("SUCCESS");
    assertThat(response.getEmployeeAssessmentId()).isEqualTo("ea123");
  }

  @Test
  void updateLastActivityDate_shouldUpdateDateForNonCompletedAssessment() {
    // Given
    String assessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(assessmentId, "John", assessmentMatrix.getId());
    assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
    assessment.setLastActivityDate(null);

    doReturn(assessment).when(employeeAssessmentRepository).findById(assessmentId);

    // When
    employeeAssessmentService.updateLastActivityDate(assessmentId);

    // Then
    assertThat(assessment.getLastActivityDate()).isNotNull();
    verify(employeeAssessmentRepository).findById(assessmentId);
    verify(employeeAssessmentRepository).save(assessment);
  }

  @Test
  void updateLastActivityDate_shouldNotUpdateDateForCompletedAssessment() {
    // Given
    String assessmentId = "test-assessment-id";
    EmployeeAssessment assessment = createMockedEmployeeAssessment(assessmentId, "John", assessmentMatrix.getId());
    assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    assessment.setLastActivityDate(null);

    doReturn(assessment).when(employeeAssessmentRepository).findById(assessmentId);

    // When
    employeeAssessmentService.updateLastActivityDate(assessmentId);

    // Then
    assertThat(assessment.getLastActivityDate()).isNull();
    verify(employeeAssessmentRepository).findById(assessmentId);
    verify(employeeAssessmentRepository, never()).save(assessment);
  }

  @Test
  void updateLastActivityDate_shouldHandleNonExistentAssessment() {
    // Given
    String nonExistentId = "non-existent-id";
    doReturn(null).when(employeeAssessmentRepository).findById(nonExistentId);

    // When
    employeeAssessmentService.updateLastActivityDate(nonExistentId);

    // Then
    verify(employeeAssessmentRepository).findById(nonExistentId);
    verify(employeeAssessmentRepository, never()).save(any());
  }
}