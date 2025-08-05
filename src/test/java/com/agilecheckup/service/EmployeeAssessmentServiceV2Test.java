package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.AnswerV2;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.repository.AnswerRepositoryV2;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;
import com.agilecheckup.service.exception.EmployeeAssessmentAlreadyExistsException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentServiceV2Test {

    @Mock
    private EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2;

    @Mock
    private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;

    @Mock
    private TeamServiceV2 teamService;

    @Mock
    private AnswerRepositoryV2 answerRepository;

    private EmployeeAssessmentServiceV2 service;

    private static final String TENANT_ID = "tenant-123";
    private static final String TEAM_ID = "team-123";
    private static final String ASSESSMENT_MATRIX_ID = "matrix-123";
    private static final String EMPLOYEE_EMAIL = "john.doe@example.com";
    private static final String EMPLOYEE_NAME = "John Doe";

    @BeforeEach
    void setUp() {
        service = new EmployeeAssessmentServiceV2(
            employeeAssessmentRepositoryV2,
            assessmentMatrixServiceV2,
            teamService,
            answerRepository
        );

        // Setup default mocks
        setupMockTeam();
        setupMockAssessmentMatrix();
    }

    private void setupMockTeam() {
        TeamV2 mockTeam = TeamV2.builder()
            .id(TEAM_ID)
            .tenantId(TENANT_ID)
            .name("Test Team")
            .description("Test team description")
            .departmentId("dept-123")
            .build();
        
        lenient().doReturn(Optional.of(mockTeam)).when(teamService).findById(TEAM_ID);
    }

    private void setupMockAssessmentMatrix() {
        Map<String, com.agilecheckup.persistency.entity.PillarV2> emptyPillarMap = new HashMap<>();
        
        AssessmentMatrixV2 mockMatrix = AssessmentMatrixV2.builder()
            .id(ASSESSMENT_MATRIX_ID)
            .tenantId(TENANT_ID)
            .name("Test Matrix")
            .description("Test matrix description")
            .performanceCycleId("cycle-123")
            .pillarMap(emptyPillarMap)
            .build();
        
        lenient().doReturn(Optional.of(mockMatrix)).when(assessmentMatrixServiceV2).findById(ASSESSMENT_MATRIX_ID);
    }

    @Test
    void testCreateEmployeeAssessment_Success() {
        doReturn(false).when(employeeAssessmentRepositoryV2)
            .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
        
        EmployeeAssessmentV2 savedAssessment = createMockEmployeeAssessment();
        doReturn(Optional.of(savedAssessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        Optional<EmployeeAssessmentV2> result = service.create(
            ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL,
            "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
        );

        assertThat(result).isPresent();
        assertThat(result.get().getEmployee().getEmail()).isEqualTo(EMPLOYEE_EMAIL);
        assertThat(result.get().getEmployeeEmailNormalized()).isEqualTo(EMPLOYEE_EMAIL.toLowerCase());
        assertThat(result.get().getAssessmentStatus()).isEqualTo(AssessmentStatus.INVITED);
        assertThat(result.get().getAnsweredQuestionCount()).isEqualTo(0);

        verify(employeeAssessmentRepositoryV2).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
        verify(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));
    }

    @Test
    void testCreateEmployeeAssessment_ThrowsExceptionWhenAlreadyExists() {
        doReturn(true).when(employeeAssessmentRepositoryV2)
            .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);

        assertThatThrownBy(() -> service.create(
            ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL,
            "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
        )).isInstanceOf(EmployeeAssessmentAlreadyExistsException.class);

        verify(employeeAssessmentRepositoryV2).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
        verify(employeeAssessmentRepositoryV2, never()).save(any(EmployeeAssessmentV2.class));
    }

    @Test
    void testCreateEmployeeAssessment_ThrowsExceptionWhenTeamNotFound() {
        doReturn(Optional.empty()).when(teamService).findById(TEAM_ID);

        assertThatThrownBy(() -> service.create(
            ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL,
            "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
        )).isInstanceOf(InvalidIdReferenceException.class);
    }

    @Test
    void testCreateEmployeeAssessment_ThrowsExceptionWhenAssessmentMatrixNotFound() {
        doReturn(Optional.empty()).when(assessmentMatrixServiceV2).findById(ASSESSMENT_MATRIX_ID);

        assertThatThrownBy(() -> service.create(
            ASSESSMENT_MATRIX_ID, TEAM_ID, EMPLOYEE_NAME, EMPLOYEE_EMAIL,
            "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
        )).isInstanceOf(InvalidIdReferenceException.class);
    }

    @Test
    void testUpdateEmployeeAssessment_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 existingAssessment = createMockEmployeeAssessment();
        existingAssessment.setId(assessmentId);

        doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        Optional<EmployeeAssessmentV2> result = service.update(
            assessmentId, ASSESSMENT_MATRIX_ID, TEAM_ID, "Jane Smith", "jane.smith@example.com",
            "987654321", PersonDocumentType.CPF, Gender.FEMALE, GenderPronoun.SHE
        );

        assertThat(result).isPresent();
        assertThat(result.get().getEmployee().getName()).isEqualTo("Jane Smith");
        assertThat(result.get().getEmployee().getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(result.get().getEmployeeEmailNormalized()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void testUpdateEmployeeAssessment_ReturnsEmptyWhenNotFound() {
        String assessmentId = "assessment-123";
        doReturn(Optional.empty()).when(employeeAssessmentRepositoryV2).findById(assessmentId);

        Optional<EmployeeAssessmentV2> result = service.update(
            assessmentId, ASSESSMENT_MATRIX_ID, TEAM_ID, "Jane Smith", "jane.smith@example.com",
            "987654321", PersonDocumentType.CPF, Gender.FEMALE, GenderPronoun.SHE
        );

        assertThat(result).isEmpty();
    }

    @Test
    void testIncrementAnsweredQuestionCount_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAnsweredQuestionCount(0);
        assessment.setAssessmentStatus(AssessmentStatus.INVITED);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        service.incrementAnsweredQuestionCount(assessmentId);

        assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(1);
        assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
        verify(employeeAssessmentRepositoryV2, atLeast(1)).save(assessment);
    }

    @Test
    void testIncrementAnsweredQuestionCount_DoesNotChangeStatusWhenAlreadyInProgress() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAnsweredQuestionCount(5);
        assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        service.incrementAnsweredQuestionCount(assessmentId);

        assertThat(assessment.getAnsweredQuestionCount()).isEqualTo(6);
        assertThat(assessment.getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
        verify(employeeAssessmentRepositoryV2).save(assessment);
    }

    @Test
    void testUpdateAssessmentStatus_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAssessmentStatus(AssessmentStatus.INVITED);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        Optional<EmployeeAssessmentV2> result = service.updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);

        assertThat(result).isPresent();
        assertThat(result.get().getAssessmentStatus()).isEqualTo(AssessmentStatus.CONFIRMED);
        verify(employeeAssessmentRepositoryV2, atLeast(1)).save(assessment);
    }

    @Test
    void testUpdateEmployeeAssessmentScore_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);

        List<AnswerV2> mockAnswers = createMockAnswers();

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(mockAnswers).when(answerRepository).findByEmployeeAssessmentId(assessmentId, TENANT_ID);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        EmployeeAssessmentV2 result = service.updateEmployeeAssessmentScore(assessmentId, TENANT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeAssessmentScore()).isNotNull();
        assertThat(result.getEmployeeAssessmentScore().getScore()).isGreaterThan(0);
        verify(employeeAssessmentRepositoryV2).save(assessment);
    }

    @Test
    void testFindAllByTenantId() {
        List<EmployeeAssessmentV2> expectedAssessments = Arrays.asList(createMockEmployeeAssessment());
        doReturn(expectedAssessments).when(employeeAssessmentRepositoryV2).findAllByTenantId(TENANT_ID);

        List<EmployeeAssessmentV2> result = service.findAllByTenantId(TENANT_ID);

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedAssessments);
        verify(employeeAssessmentRepositoryV2).findAllByTenantId(TENANT_ID);
    }

    @Test
    void testFindByAssessmentMatrix() {
        List<EmployeeAssessmentV2> expectedAssessments = Arrays.asList(createMockEmployeeAssessment());
        doReturn(expectedAssessments).when(employeeAssessmentRepositoryV2)
            .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

        List<EmployeeAssessmentV2> result = service.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expectedAssessments);
        verify(employeeAssessmentRepositoryV2).findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);
    }

    @Test
    void testFindByIdWithTenantId_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setTenantId(TENANT_ID);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);

        Optional<EmployeeAssessmentV2> result = service.findById(assessmentId, TENANT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(assessmentId);
    }

    @Test
    void testFindByIdWithTenantId_ReturnsEmptyWhenTenantMismatch() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setTenantId("different-tenant");

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);

        Optional<EmployeeAssessmentV2> result = service.findById(assessmentId, TENANT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void testDeleteById_Success() {
        String assessmentId = "assessment-123";

        service.deleteById(assessmentId);

        verify(employeeAssessmentRepositoryV2).deleteById(assessmentId);
    }

    @Test
    void testSave_NewAssessment_ValidatesUniqueness() {
        EmployeeAssessmentV2 newAssessment = createMockEmployeeAssessment();
        newAssessment.setId(null); // New assessment

        doReturn(false).when(employeeAssessmentRepositoryV2)
            .existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
        doReturn(Optional.of(newAssessment)).when(employeeAssessmentRepositoryV2).save(newAssessment);

        EmployeeAssessmentV2 result = service.save(newAssessment);

        assertThat(result).isEqualTo(newAssessment);
        verify(employeeAssessmentRepositoryV2).existsByAssessmentMatrixAndEmployeeEmail(ASSESSMENT_MATRIX_ID, EMPLOYEE_EMAIL);
        verify(employeeAssessmentRepositoryV2).save(newAssessment);
    }

    @Test
    void testValidateEmployee_Success_InvitedToConfirmed() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAssessmentStatus(AssessmentStatus.INVITED);

        List<EmployeeAssessmentV2> assessments = Arrays.asList(assessment);
        doReturn(assessments).when(employeeAssessmentRepositoryV2)
            .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        EmployeeValidationRequest request = new EmployeeValidationRequest();
        request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
        request.setTenantId(TENANT_ID);
        request.setEmail(EMPLOYEE_EMAIL);

        EmployeeValidationResponse response = service.validateEmployee(request);

        assertThat("SUCCESS".equals(response.getStatus())).isTrue();
        assertThat(response.getEmployeeAssessmentId()).isEqualTo(assessmentId);
        assertThat(response.getName()).isEqualTo(EMPLOYEE_NAME);
        assertThat(response.getAssessmentStatus()).isEqualTo(AssessmentStatus.CONFIRMED.toString());
        verify(employeeAssessmentRepositoryV2).save(assessment);
    }

    @Test
    void testValidateEmployee_NotFound() {
        doReturn(Arrays.asList()).when(employeeAssessmentRepositoryV2)
            .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

        EmployeeValidationRequest request = new EmployeeValidationRequest();
        request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
        request.setTenantId(TENANT_ID);
        request.setEmail(EMPLOYEE_EMAIL);

        EmployeeValidationResponse response = service.validateEmployee(request);

        assertThat("SUCCESS".equals(response.getStatus())).isFalse();
        assertThat(response.getMessage()).contains("couldn't find your assessment invitation");
    }

    @Test
    void testValidateEmployee_AlreadyCompleted() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);

        List<EmployeeAssessmentV2> assessments = Arrays.asList(assessment);
        doReturn(assessments).when(employeeAssessmentRepositoryV2)
            .findByAssessmentMatrixId(ASSESSMENT_MATRIX_ID, TENANT_ID);

        EmployeeValidationRequest request = new EmployeeValidationRequest();
        request.setAssessmentMatrixId(ASSESSMENT_MATRIX_ID);
        request.setTenantId(TENANT_ID);
        request.setEmail(EMPLOYEE_EMAIL);

        EmployeeValidationResponse response = service.validateEmployee(request);

        assertThat("INFO".equals(response.getStatus())).isTrue();
        assertThat(response.getMessage()).contains("already completed this assessment");
        assertThat(response.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED.toString());
    }

    @Test
    void testUpdateLastActivityDate_Success() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        service.updateLastActivityDate(assessmentId);

        assertThat(assessment.getLastActivityDate()).isNotNull();
        verify(employeeAssessmentRepositoryV2).save(assessment);
    }

    @Test
    void testUpdateLastActivityDate_DoesNotUpdateWhenCompleted() {
        String assessmentId = "assessment-123";
        EmployeeAssessmentV2 assessment = createMockEmployeeAssessment();
        assessment.setId(assessmentId);
        assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
        Date originalDate = new Date();
        assessment.setLastActivityDate(originalDate);

        doReturn(Optional.of(assessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);

        service.updateLastActivityDate(assessmentId);

        assertThat(assessment.getLastActivityDate()).isEqualTo(originalDate);
        verify(employeeAssessmentRepositoryV2, never()).save(assessment);
    }

    @Test
    void testScanAllEmployeeAssessments() {
        List<EmployeeAssessmentV2> expectedAssessments = Arrays.asList(createMockEmployeeAssessment());
        doReturn(expectedAssessments).when(employeeAssessmentRepositoryV2).findAll();

        List<EmployeeAssessmentV2> result = service.scanAllEmployeeAssessments();

        assertThat(result).isEqualTo(expectedAssessments);
        verify(employeeAssessmentRepositoryV2).findAll();
    }

    private EmployeeAssessmentV2 createMockEmployeeAssessment() {
        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name(EMPLOYEE_NAME)
            .email(EMPLOYEE_EMAIL)
            .documentNumber("123456789")
            .personDocumentType(PersonDocumentType.CPF)
            .gender(Gender.MALE)
            .genderPronoun(GenderPronoun.HE)
            .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
            .teamId(TEAM_ID)
            .employee(employee)
            .employeeEmailNormalized(EMPLOYEE_EMAIL.toLowerCase())
            .answeredQuestionCount(0)
            .assessmentStatus(AssessmentStatus.INVITED)
            .build();
        assessment.setTenantId(TENANT_ID);
        return assessment;
    }

    private List<AnswerV2> createMockAnswers() {
        QuestionV2 mockQuestion = QuestionV2.builder()
            .id("question-123")
            .assessmentMatrixId("matrix-123")
            .pillarId("pillar-123")
            .pillarName("Test Pillar")
            .categoryId("category-123")
            .categoryName("Test Category")
            .question("Test Question")
            .questionType(QuestionType.ONE_TO_TEN)
            .points(10.0)
            .tenantId(TENANT_ID)
            .build();

        AnswerV2 answer1 = AnswerV2.builder()
            .questionId("question-123")
            .pillarId("pillar-123")
            .categoryId("category-123")
            .score(8.5)
            .question(mockQuestion)
            .questionType(QuestionType.ONE_TO_TEN)
            .employeeAssessmentId("assessment-123")
            .answeredAt(LocalDateTime.now())
            .value("8")
            .tenantId(TENANT_ID)
            .build();

        AnswerV2 answer2 = AnswerV2.builder()
            .questionId("question-124")
            .pillarId("pillar-123")
            .categoryId("category-123")
            .score(7.0)
            .question(mockQuestion)
            .questionType(QuestionType.ONE_TO_TEN)
            .employeeAssessmentId("assessment-123")
            .answeredAt(LocalDateTime.now())
            .value("7")
            .tenantId(TENANT_ID)
            .build();

        return Arrays.asList(answer1, answer2);
    }

    @Test
    void shouldCreateEmployeeAssessmentWithNullTeamId() {
        // Given
        String assessmentMatrixId = "matrix-123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        String documentNumber = "123456789";
        PersonDocumentType documentType = PersonDocumentType.CPF;
        Gender gender = Gender.MALE;
        GenderPronoun genderPronoun = GenderPronoun.HE;

        AssessmentMatrixV2 mockMatrix = AssessmentMatrixV2.builder()
                .id(assessmentMatrixId)
                .name("Test Matrix")
                .description("Test Description")
                .performanceCycleId("cycle-123")
                .build();
        mockMatrix.setTenantId(TENANT_ID);

        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixServiceV2).findById(assessmentMatrixId);
        doReturn(Optional.of(createMockEmployeeAssessment())).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        // When
        Optional<EmployeeAssessmentV2> result = service.create(assessmentMatrixId, null, name, email, documentNumber, documentType, gender, genderPronoun);

        // Then
        assertThat(result).isPresent();
        
        ArgumentCaptor<EmployeeAssessmentV2> captor = ArgumentCaptor.forClass(EmployeeAssessmentV2.class);
        verify(employeeAssessmentRepositoryV2).save(captor.capture());
        
        EmployeeAssessmentV2 createdAssessment = captor.getValue();
        assertThat(createdAssessment.getTeamId()).isNull();
        assertThat(createdAssessment.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(createdAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
        assertThat(createdAssessment.getEmployee().getName()).isEqualTo(name);
        assertThat(createdAssessment.getEmployee().getEmail()).isEqualTo(email);
    }

    @Test
    void shouldUpdateEmployeeAssessmentWithNullTeamId() {
        // Given
        String assessmentId = "assessment-123";
        String assessmentMatrixId = "matrix-123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        String documentNumber = "123456789";
        PersonDocumentType documentType = PersonDocumentType.CPF;
        Gender gender = Gender.MALE;
        GenderPronoun genderPronoun = GenderPronoun.HE;

        EmployeeAssessmentV2 existingAssessment = createMockEmployeeAssessment();
        existingAssessment.setId(assessmentId);

        AssessmentMatrixV2 mockMatrix = AssessmentMatrixV2.builder()
                .id(assessmentMatrixId)
                .name("Test Matrix")
                .description("Test Description")
                .performanceCycleId("cycle-123")
                .build();
        mockMatrix.setTenantId(TENANT_ID);

        doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepositoryV2).findById(assessmentId);
        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixServiceV2).findById(assessmentMatrixId);
        doReturn(Optional.of(existingAssessment)).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));

        // When
        Optional<EmployeeAssessmentV2> result = service.update(assessmentId, assessmentMatrixId, null, name, email, documentNumber, documentType, gender, genderPronoun);

        // Then
        assertThat(result).isPresent();
        
        ArgumentCaptor<EmployeeAssessmentV2> captor = ArgumentCaptor.forClass(EmployeeAssessmentV2.class);
        verify(employeeAssessmentRepositoryV2).save(captor.capture());
        
        EmployeeAssessmentV2 updatedAssessment = captor.getValue();
        assertThat(updatedAssessment.getTeamId()).isNull();
        assertThat(updatedAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
        assertThat(updatedAssessment.getEmployee().getName()).isEqualTo(name);
        assertThat(updatedAssessment.getEmployee().getEmail()).isEqualTo(email);
    }

    @Test
    void testCreateEmployeeAssessment_ShouldSaveTeamIdWhenTeamProvided() {
        // Given
        String assessmentMatrixId = ASSESSMENT_MATRIX_ID;
        String teamId = TEAM_ID;
        String name = EMPLOYEE_NAME;
        String email = EMPLOYEE_EMAIL;
        
        // Setup mock team with DIFFERENT tenant ID to expose bug
        TeamV2 mockTeam = TeamV2.builder()
            .id(teamId)
            .tenantId("team-tenant-123") // Different from matrix tenant
            .name("Test Team")
            .description("Test team description")
            .departmentId("dept-123")
            .build();
        doReturn(Optional.of(mockTeam)).when(teamService).findById(teamId);
        
        // Setup mock assessment matrix
        AssessmentMatrixV2 mockMatrix = AssessmentMatrixV2.builder()
            .id(assessmentMatrixId)
            .tenantId(TENANT_ID) // Matrix tenant should be used
            .name("Test Matrix")
            .description("Test matrix description")
            .performanceCycleId("cycle-123")
            .pillarMap(new HashMap<>())
            .build();
        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixServiceV2).findById(assessmentMatrixId);
        
        doReturn(false).when(employeeAssessmentRepositoryV2).existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, email);
        doReturn(Optional.of(createMockEmployeeAssessment())).when(employeeAssessmentRepositoryV2).save(any(EmployeeAssessmentV2.class));
        
        // When
        Optional<EmployeeAssessmentV2> result = service.create(
            assessmentMatrixId, teamId, name, email,
            "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
        );
        
        // Then
        assertThat(result).isPresent();
        
        ArgumentCaptor<EmployeeAssessmentV2> captor = ArgumentCaptor.forClass(EmployeeAssessmentV2.class);
        verify(employeeAssessmentRepositoryV2).save(captor.capture());
        
        EmployeeAssessmentV2 createdAssessment = captor.getValue();
        
        // BUG EXPOSED: These assertions should pass but will fail due to the bug
        assertThat(createdAssessment.getTeamId()).isEqualTo(teamId); // teamId should be saved
        assertThat(createdAssessment.getTenantId()).isEqualTo(TENANT_ID); // should use matrix tenantId, not team tenantId
        assertThat(createdAssessment.getAssessmentMatrixId()).isEqualTo(assessmentMatrixId);
        assertThat(createdAssessment.getEmployee().getName()).isEqualTo(name);
        assertThat(createdAssessment.getEmployee().getEmail()).isEqualTo(email);
    }
}