package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.question.OptionGroupV2;
import com.agilecheckup.persistency.entity.question.QuestionOptionV2;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuestionV2Test {

    @Test
    void testEntityCreation() {
        QuestionV2 question = QuestionV2.builder()
                .question("What is agile methodology?")
                .questionType(QuestionType.YES_NO)
                .tenantId("tenant-123")
                .points(10.0)
                .assessmentMatrixId("matrix-123")
                .pillarId("pillar-456")
                .pillarName("Pillar Test")
                .categoryId("category-789")
                .categoryName("Category Test")
                .extraDescription("Extra description")
                .build();

        assertThat(question.getQuestion()).isEqualTo("What is agile methodology?");
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.YES_NO);
        assertThat(question.getTenantId()).isEqualTo("tenant-123");
        assertThat(question.getPoints()).isEqualTo(10.0);
        assertThat(question.getAssessmentMatrixId()).isEqualTo("matrix-123");
        assertThat(question.getPillarId()).isEqualTo("pillar-456");
        assertThat(question.getPillarName()).isEqualTo("Pillar Test");
        assertThat(question.getCategoryId()).isEqualTo("category-789");
        assertThat(question.getCategoryName()).isEqualTo("Category Test");
        assertThat(question.getExtraDescription()).isEqualTo("Extra description");
    }

    @Test
    void testCustomQuestionWithOptions() {
        Map<Integer, QuestionOptionV2> optionMap = new HashMap<>();
        optionMap.put(1, QuestionOptionV2.builder().id(1).text("Option 1").points(10.0).build());
        optionMap.put(2, QuestionOptionV2.builder().id(2).text("Option 2").points(20.0).build());

        OptionGroupV2 optionGroup = OptionGroupV2.builder()
                .isMultipleChoice(true)
                .showFlushed(false)
                .optionMap(optionMap)
                .build();

        QuestionV2 question = QuestionV2.builder()
                .question("Choose your preferred options")
                .questionType(QuestionType.CUSTOMIZED)
                .tenantId("tenant-456")
                .assessmentMatrixId("matrix-456")
                .pillarId("pillar-789")
                .pillarName("Custom Pillar")
                .categoryId("category-101")
                .categoryName("Custom Category")
                .optionGroup(optionGroup)
                .build();

        assertThat(question.getQuestion()).isEqualTo("Choose your preferred options");
        assertThat(question.getQuestionType()).isEqualTo(QuestionType.CUSTOMIZED);
        assertThat(question.getOptionGroup()).isNotNull();
        assertThat(question.getOptionGroup().isMultipleChoice()).isTrue();
        assertThat(question.getOptionGroup().isShowFlushed()).isFalse();
        assertThat(question.getOptionGroup().getOptionMap()).hasSize(2);
    }

    @Test
    void testDefaultValues() {
        QuestionV2 question = QuestionV2.builder()
                .question("Basic question")
                .questionType(QuestionType.ONE_TO_TEN)
                .tenantId("tenant-789")
                .assessmentMatrixId("matrix-789")
                .pillarId("pillar-101")
                .pillarName("Basic Pillar")
                .categoryId("category-202")
                .categoryName("Basic Category")
                .build();

        assertThat(question.getPoints()).isNull();
        assertThat(question.getOptionGroup()).isNull();
        assertThat(question.getExtraDescription()).isNull();
    }

    @Test
    void testBuilderPattern() {
        QuestionV2 question = QuestionV2.builder()
                .question("Builder pattern test")
                .questionType(QuestionType.YES_NO)
                .tenantId("tenant-builder")
                .assessmentMatrixId("matrix-builder")
                .pillarId("pillar-builder")
                .pillarName("Builder Pillar")
                .categoryId("category-builder")
                .categoryName("Builder Category")
                .points(5.0)
                .build();

        assertThat(question).isNotNull();
        assertThat(question.getQuestion()).isEqualTo("Builder pattern test");
        assertThat(question.getPoints()).isEqualTo(5.0);
    }

    @Test
    void testInheritanceFromTenantableEntity() {
        QuestionV2 question = QuestionV2.builder()
                .question("Inheritance test")
                .questionType(QuestionType.YES_NO)
                .tenantId("tenant-inheritance")
                .assessmentMatrixId("matrix-inheritance")
                .pillarId("pillar-inheritance")
                .pillarName("Inheritance Pillar")
                .categoryId("category-inheritance")
                .categoryName("Inheritance Category")
                .build();

        assertThat(question.getTenantId()).isEqualTo("tenant-inheritance");
        // ID is auto-generated so we check it's not null
        assertThat(question.getId()).isNotNull();
        assertThat(question.getCreatedDate()).isNull();
        assertThat(question.getLastUpdatedDate()).isNull();
    }
}