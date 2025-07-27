package com.agilecheckup.persistency.entity;

import com.agilecheckup.util.TestObjectFactoryV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryV2Test {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new StdDateFormat());

    @Test
    void shouldCreateCategoryV2WithAllFields() {
        Instant now = Instant.now();
        CategoryV2 category = CategoryV2.builder()
                .name("Agile Practices")
                .description("Core agile development practices")
                .createdDate(now.minusSeconds(3600))
                .lastUpdatedDate(now)
                .build();

        assertThat(category.getName()).isEqualTo("Agile Practices");
        assertThat(category.getDescription()).isEqualTo("Core agile development practices");
        assertThat(category.getCreatedDate()).isEqualTo(now.minusSeconds(3600));
        assertThat(category.getLastUpdatedDate()).isEqualTo(now);
    }

    @Test
    void shouldCreateCategoryV2UsingTestFactory() {
        CategoryV2 category = TestObjectFactoryV2.createMockedCategoryV2();

        assertThat(category.getName()).isEqualTo("Test Category");
        assertThat(category.getDescription()).isEqualTo("Test category description");
        assertThat(category.getCreatedDate()).isNotNull();
        assertThat(category.getLastUpdatedDate()).isNotNull();
    }

    @Test
    void shouldCreateCategoryV2WithCustomNameAndDescription() {
        CategoryV2 category = TestObjectFactoryV2.createMockedCategoryV2("Custom Category", "Custom description");

        assertThat(category.getName()).isEqualTo("Custom Category");
        assertThat(category.getDescription()).isEqualTo("Custom description");
        assertThat(category.getCreatedDate()).isNotNull();
        assertThat(category.getLastUpdatedDate()).isNotNull();
    }

    @Test
    void shouldSerializeAndDeserializeCategoryV2ToJson() throws Exception {
        CategoryV2 originalCategory = CategoryV2.builder()
                .name("JSON Test")
                .description("JSON serialization test")
                .build();

        String json = OBJECT_MAPPER.writeValueAsString(originalCategory);
        CategoryV2 deserializedCategory = OBJECT_MAPPER.readValue(json, CategoryV2.class);

        assertThat(deserializedCategory.getName()).isEqualTo(originalCategory.getName());
        assertThat(deserializedCategory.getDescription()).isEqualTo(originalCategory.getDescription());
    }

    @Test
    void shouldHandleNullFieldsInCategoryV2() {
        CategoryV2 category = new CategoryV2();

        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
        assertThat(category.getCreatedDate()).isNull();
        assertThat(category.getLastUpdatedDate()).isNull();
    }

    @Test
    void shouldHandleEqualsAndHashCodeForCategoryV2() {
        Instant now = Instant.now();
        String sameId = "test-id-123";
        CategoryV2 category1 = CategoryV2.builder()
                .id(sameId)
                .name("Test Category")
                .description("Test description")
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        CategoryV2 category2 = CategoryV2.builder()
                .id(sameId)
                .name("Test Category")
                .description("Test description")
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        CategoryV2 category3 = CategoryV2.builder()
                .name("Different Category")
                .description("Different description")
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        assertThat(category1).isEqualTo(category2);
        assertThat(category1).isNotEqualTo(category3);
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
        assertThat(category1.hashCode()).isNotEqualTo(category3.hashCode());
    }

    @Test
    void shouldHandleToStringForCategoryV2() {
        CategoryV2 category = TestObjectFactoryV2.createMockedCategoryV2("ToString Test", "Test toString method");
        String toStringResult = category.toString();

        assertThat(toStringResult).contains("ToString Test");
        assertThat(toStringResult).contains("Test toString method");
        assertThat(toStringResult).contains("CategoryV2");
    }

    @Test
    void shouldHandleEmptyStringsInCategoryV2() {
        CategoryV2 category = CategoryV2.builder()
                .name("")
                .description("")
                .createdDate(Instant.now())
                .lastUpdatedDate(Instant.now())
                .build();

        assertThat(category.getName()).isEmpty();
        assertThat(category.getDescription()).isEmpty();
    }

    @Test
    void shouldHandleLongStringsInCategoryV2() {
        String longName = "A".repeat(1000);
        String longDescription = "B".repeat(2000);

        CategoryV2 category = CategoryV2.builder()
                .name(longName)
                .description(longDescription)
                .createdDate(Instant.now())
                .lastUpdatedDate(Instant.now())
                .build();

        assertThat(category.getName()).hasSize(1000);
        assertThat(category.getDescription()).hasSize(2000);
    }

    @Test
    void shouldHandleSpecialCharactersInCategoryV2() {
        CategoryV2 category = CategoryV2.builder()
                .name("Category with special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>?,./")
                .description("Description with unicode: áéíóú ç ñ 中文 🚀 💻")
                .createdDate(Instant.now())
                .lastUpdatedDate(Instant.now())
                .build();

        assertThat(category.getName()).contains("!@#$%^&*()_+-={}[]|\\:;\"'<>?,./");
        assertThat(category.getDescription()).contains("áéíóú ç ñ 中文 🚀 💻");
    }
}