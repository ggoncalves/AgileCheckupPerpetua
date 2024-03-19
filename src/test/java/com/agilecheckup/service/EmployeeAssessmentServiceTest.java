package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

  private final AssessmentMatrix assessmentMatrix = createMockedAssessmentMatrix(GENERIC_ID_1234, DEFAULT_ID, createMockedPillarMap(1, 2, "P", "C"));

  private final Team team = createMockedTeam(DEFAULT_ID);

  private static final String TENANT_ID = "TENANT_ID";
  private static final String EMPLOYEE_ASSESSMENT_ID = "employeeAssessmentId";

  @BeforeEach
  void setUpBefore() {
    originalEmployeeAssessment = createMockedEmployeeAssessment(DEFAULT_ID, "Fernando", assessmentMatrix.getId());
    originalEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);
  }

  @Test
  void create() {
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedEmployeeAssessment, employeeAssessmentRepository);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeam().getId());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun());

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(savedEmployeeAssessment);
    verify(employeeAssessmentService).create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun());
    verify(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
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
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeam().getId());
    if (assessmentMatrixId != null) doReturn(Optional.empty()).when(employeeAssessmentService).findById(any());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
    verify(employeeAssessmentService).create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );
    if (assessmentMatrixId == null) {
      verify(assessmentMatrixService, never()).findById(assessmentMatrixId);
    }
    else {
      verify(assessmentMatrixService).findById(assessmentMatrixId);
    }
  }

  void assertCreateEmployeeAssessmentForTeamId(String teamId) {
    originalEmployeeAssessment.setTeam(null);
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
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
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
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
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
    verify(employeeAssessmentRepository).findById(employeeAssessmentId);
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
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

}