package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.repository.QuestionRepositoryV2;
import com.agilecheckup.service.exception.InvalidCustomOptionListException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceV2Test {

    @Mock
    private QuestionRepositoryV2 questionRepository;

    @Mock
    private AssessmentMatrixServiceV2 assessmentMatrixService;

    private QuestionServiceV2 service;

    @BeforeEach
    void setUp() {
        service = new QuestionServiceV2(questionRepository, assessmentMatrixService);
    }

    @Test
    void testCreate() {
        String questionTxt = "What is agile methodology?";
        QuestionType questionType = QuestionType.YES_NO;
        String tenantId = "tenant-123";
        Double points = 10.0;
        String assessmentMatrixId = "matrix-123";
        String pillarId = "pillar-456";
        String categoryId = "category-789";
        String extraDescription = "Extra description";

        AssessmentMatrixV2 mockMatrix = createMockAssessmentMatrix(assessmentMatrixId, tenantId);
        QuestionV2 savedQuestion = createMockQuestion("question-123", questionTxt, tenantId, assessmentMatrixId);

        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
        doReturn(Optional.of(savedQuestion)).when(questionRepository).save(any(QuestionV2.class));

        Optional<QuestionV2> result = service.create(questionTxt, questionType, tenantId, points, assessmentMatrixId, pillarId, categoryId, extraDescription);

        assertThat(result).isPresent();
        assertThat(result.get().getQuestion()).isEqualTo(questionTxt);
        assertThat(result.get().getQuestionType()).isEqualTo(questionType);
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        assertThat(result.get().getPoints()).isEqualTo(points);
        verify(assessmentMatrixService).incrementQuestionCount(assessmentMatrixId);
    }

    @Test
    void testCreateCustomQuestion() {
        String questionTxt = "Choose your preferred options";
        QuestionType questionType = QuestionType.CUSTOMIZED;
        String tenantId = "tenant-123";
        boolean isMultipleChoice = true;
        boolean showFlushed = false;
        List<QuestionOption> options = Arrays.asList(
                QuestionOption.builder().id(1).text("Option 1").points(10.0).build(),
                QuestionOption.builder().id(2).text("Option 2").points(20.0).build()
        );
        String assessmentMatrixId = "matrix-123";
        String pillarId = "pillar-456";
        String categoryId = "category-789";
        String extraDescription = "Custom question description";

        AssessmentMatrixV2 mockMatrix = createMockAssessmentMatrix(assessmentMatrixId, tenantId);
        QuestionV2 savedQuestion = createMockCustomQuestion("question-123", questionTxt, tenantId, assessmentMatrixId, isMultipleChoice, showFlushed, options);

        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
        doReturn(Optional.of(savedQuestion)).when(questionRepository).save(any(QuestionV2.class));

        Optional<QuestionV2> result = service.createCustomQuestion(questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options, assessmentMatrixId, pillarId, categoryId, extraDescription);

        assertThat(result).isPresent();
        assertThat(result.get().getQuestion()).isEqualTo(questionTxt);
        assertThat(result.get().getQuestionType()).isEqualTo(questionType);
        assertThat(result.get().getOptionGroup()).isNotNull();
        assertThat(result.get().getOptionGroup().isMultipleChoice()).isTrue();
        verify(assessmentMatrixService).incrementQuestionCount(assessmentMatrixId);
    }

    @Test
    void testUpdate() {
        String questionId = "question-123";
        String questionTxt = "Updated question text";
        QuestionType questionType = QuestionType.ONE_TO_TEN;
        String tenantId = "tenant-123";
        Double points = 15.0;
        String assessmentMatrixId = "matrix-123";
        String pillarId = "pillar-456";
        String categoryId = "category-789";
        String extraDescription = "Updated description";

        QuestionV2 existingQuestion = createMockQuestion(questionId, "Old question", tenantId, assessmentMatrixId);
        AssessmentMatrixV2 mockMatrix = createMockAssessmentMatrix(assessmentMatrixId, tenantId);
        QuestionV2 updatedQuestion = createMockQuestion(questionId, questionTxt, tenantId, assessmentMatrixId);

        doReturn(Optional.of(existingQuestion)).when(questionRepository).findById(questionId);
        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
        doReturn(Optional.of(updatedQuestion)).when(questionRepository).save(any(QuestionV2.class));

        Optional<QuestionV2> result = service.update(questionId, questionTxt, questionType, tenantId, points, assessmentMatrixId, pillarId, categoryId, extraDescription);

        assertThat(result).isPresent();
        assertThat(result.get().getQuestion()).isEqualTo(questionTxt);
        verify(questionRepository).findById(questionId);
        verify(questionRepository).save(any(QuestionV2.class));
    }

    @Test
    void testUpdateCustomQuestion() {
        String questionId = "question-123";
        String questionTxt = "Updated custom question";
        QuestionType questionType = QuestionType.CUSTOMIZED;
        String tenantId = "tenant-123";
        boolean isMultipleChoice = false;
        boolean showFlushed = true;
        List<QuestionOption> options = Arrays.asList(
                QuestionOption.builder().id(1).text("New Option 1").points(15.0).build(),
                QuestionOption.builder().id(2).text("New Option 2").points(25.0).build(),
                QuestionOption.builder().id(3).text("New Option 3").points(35.0).build()
        );
        String assessmentMatrixId = "matrix-123";
        String pillarId = "pillar-456";
        String categoryId = "category-789";
        String extraDescription = "Updated custom description";

        QuestionV2 existingQuestion = createMockQuestion(questionId, "Old custom question", tenantId, assessmentMatrixId);
        AssessmentMatrixV2 mockMatrix = createMockAssessmentMatrix(assessmentMatrixId, tenantId);
        QuestionV2 updatedQuestion = createMockCustomQuestion(questionId, questionTxt, tenantId, assessmentMatrixId, isMultipleChoice, showFlushed, options);

        doReturn(Optional.of(existingQuestion)).when(questionRepository).findById(questionId);
        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
        doReturn(Optional.of(updatedQuestion)).when(questionRepository).save(any(QuestionV2.class));

        Optional<QuestionV2> result = service.updateCustomQuestion(questionId, questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options, assessmentMatrixId, pillarId, categoryId, extraDescription);

        assertThat(result).isPresent();
        assertThat(result.get().getQuestion()).isEqualTo(questionTxt);
        assertThat(result.get().getOptionGroup()).isNotNull();
        assertThat(result.get().getOptionGroup().isMultipleChoice()).isFalse();
        assertThat(result.get().getOptionGroup().isShowFlushed()).isTrue();
    }

    @Test
    void testFindByAssessmentMatrixId() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        List<QuestionV2> expectedQuestions = Arrays.asList(
                createMockQuestion("question-1", "Question 1", tenantId, matrixId),
                createMockQuestion("question-2", "Question 2", tenantId, matrixId)
        );

        doReturn(expectedQuestions).when(questionRepository).findByAssessmentMatrixId(matrixId, tenantId);

        List<QuestionV2> result = service.findByAssessmentMatrixId(matrixId, tenantId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedQuestions);
        verify(questionRepository).findByAssessmentMatrixId(matrixId, tenantId);
    }

    @Test
    void testHasCategoryQuestions() {
        String matrixId = "matrix-123";
        String categoryId = "category-456";
        String tenantId = "tenant-123";

        doReturn(true).when(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);

        boolean result = service.hasCategoryQuestions(matrixId, categoryId, tenantId);

        assertThat(result).isTrue();
        verify(questionRepository).existsByCategoryId(matrixId, categoryId, tenantId);
    }

    @Test
    void testDelete() {
        String questionId = "question-123";
        String assessmentMatrixId = "matrix-123";
        QuestionV2 question = createMockQuestion(questionId, "Test question", "tenant-123", assessmentMatrixId);

        service.delete(question);

        verify(questionRepository).deleteById(question.getId());
        verify(assessmentMatrixService).decrementQuestionCount(assessmentMatrixId);
    }

    @Test
    void testDeleteWithNullQuestion() {
        service.delete(null);

        // Nothing should be called for null input
    }

    @Test
    void testCreateWithInvalidAssessmentMatrix() {
        String assessmentMatrixId = "invalid-matrix";

        doReturn(Optional.empty()).when(assessmentMatrixService).findById(assessmentMatrixId);

        assertThatThrownBy(() -> service.create("Question", QuestionType.YES_NO, "tenant-123", 10.0, assessmentMatrixId, "pillar-1", "category-1", "desc"))
                .isInstanceOf(InvalidIdReferenceException.class)
                .hasMessageContaining("AssessmentMatrix");
    }

    @Test
    void testCreateWithInvalidPillar() {
        String assessmentMatrixId = "matrix-123";
        String invalidPillarId = "invalid-pillar";
        AssessmentMatrixV2 mockMatrix = createMockAssessmentMatrixWithoutPillar(assessmentMatrixId, "tenant-123");

        doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);

        assertThatThrownBy(() -> service.create("Question", QuestionType.YES_NO, "tenant-123", 10.0, assessmentMatrixId, invalidPillarId, "category-1", "desc"))
                .isInstanceOf(InvalidIdReferenceException.class)
                .hasMessageContaining("Pillar");
    }

    @Test
    void testValidateQuestionOptions_EmptyOptions() {
        List<QuestionOption> emptyOptions = Arrays.asList();

        assertThatThrownBy(() -> service.createCustomQuestion("Question", QuestionType.CUSTOMIZED, "tenant-123", true, false, emptyOptions, "matrix-123", "pillar-1", "category-1", "desc"))
                .isInstanceOf(InvalidCustomOptionListException.class);
    }

    @Test
    void testValidateQuestionOptions_TooFewOptions() {
        List<QuestionOption> tooFewOptions = Arrays.asList(
                QuestionOption.builder().id(1).text("Only option").points(10.0).build()
        );

        assertThatThrownBy(() -> service.createCustomQuestion("Question", QuestionType.CUSTOMIZED, "tenant-123", true, false, tooFewOptions, "matrix-123", "pillar-1", "category-1", "desc"))
                .isInstanceOf(InvalidCustomOptionListException.class);
    }

    @Test
    void testValidateQuestionOptions_EmptyOptionText() {
        List<QuestionOption> optionsWithEmptyText = Arrays.asList(
                QuestionOption.builder().id(1).text("Valid option").points(10.0).build(),
                QuestionOption.builder().id(2).text("").points(20.0).build()
        );

        assertThatThrownBy(() -> service.createCustomQuestion("Question", QuestionType.CUSTOMIZED, "tenant-123", true, false, optionsWithEmptyText, "matrix-123", "pillar-1", "category-1", "desc"))
                .isInstanceOf(InvalidCustomOptionListException.class);
    }

    @Test
    void testValidateQuestionOptions_InvalidOptionIds() {
        List<QuestionOption> invalidIdOptions = Arrays.asList(
                QuestionOption.builder().id(1).text("Option 1").points(10.0).build(),
                QuestionOption.builder().id(3).text("Option 3").points(30.0).build() // Missing id 2
        );

        assertThatThrownBy(() -> service.createCustomQuestion("Question", QuestionType.CUSTOMIZED, "tenant-123", true, false, invalidIdOptions, "matrix-123", "pillar-1", "category-1", "desc"))
                .isInstanceOf(InvalidCustomOptionListException.class);
    }

    @Test
    void testToOptionMap() {
        List<QuestionOption> options = Arrays.asList(
                QuestionOption.builder().id(1).text("Option 1").points(10.0).build(),
                QuestionOption.builder().id(2).text("Option 2").points(20.0).build(),
                QuestionOption.builder().id(3).text("Option 3").points(30.0).build()
        );

        Map<Integer, QuestionOption> result = service.toOptionMap(options);

        assertThat(result).hasSize(3);
        assertThat(result.get(1).getText()).isEqualTo("Option 1");
        assertThat(result.get(2).getText()).isEqualTo("Option 2");
        assertThat(result.get(3).getText()).isEqualTo("Option 3");
    }

    private AssessmentMatrixV2 createMockAssessmentMatrix(String id, String tenantId) {
        Map<String, CategoryV2> categoryMap = new HashMap<>();
        categoryMap.put("category-789", CategoryV2.builder()
                .id("category-789")
                .name("Test Category")
                .description("Test Category Description")
                .build());

        Map<String, PillarV2> pillarMap = new HashMap<>();
        pillarMap.put("pillar-456", PillarV2.builder()
                .id("pillar-456")
                .name("Test Pillar")
                .description("Test Pillar Description")
                .categoryMap(categoryMap)
                .build());

        return AssessmentMatrixV2.builder()
                .id(id)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .pillarMap(pillarMap)
                .build();
    }

    private AssessmentMatrixV2 createMockAssessmentMatrixWithoutPillar(String id, String tenantId) {
        return AssessmentMatrixV2.builder()
                .id(id)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .pillarMap(new HashMap<>())
                .build();
    }

    private QuestionV2 createMockQuestion(String id, String questionText, String tenantId, String assessmentMatrixId) {
        return QuestionV2.builder()
                .id(id)
                .question(questionText)
                .questionType(QuestionType.YES_NO)
                .tenantId(tenantId)
                .assessmentMatrixId(assessmentMatrixId)
                .pillarId("pillar-456")
                .pillarName("Test Pillar")
                .categoryId("category-789")
                .categoryName("Test Category")
                .points(10.0)
                .build();
    }

    private QuestionV2 createMockCustomQuestion(String id, String questionText, String tenantId, String assessmentMatrixId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
        Map<Integer, QuestionOption> optionMap = new HashMap<>();
        for (QuestionOption option : options) {
            optionMap.put(option.getId(), option);
        }

        OptionGroup optionGroup = OptionGroup.builder()
                .isMultipleChoice(isMultipleChoice)
                .showFlushed(showFlushed)
                .optionMap(optionMap)
                .build();

        return QuestionV2.builder()
                .id(id)
                .question(questionText)
                .questionType(QuestionType.CUSTOMIZED)
                .tenantId(tenantId)
                .assessmentMatrixId(assessmentMatrixId)
                .pillarId("pillar-456")
                .pillarName("Test Pillar")
                .categoryId("category-789")
                .categoryName("Test Category")
                .optionGroup(optionGroup)
                .build();
    }
}