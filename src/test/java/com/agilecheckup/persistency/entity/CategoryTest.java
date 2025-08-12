package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.util.TestObjectFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@ExtendWith(MockitoExtension.class)
class CategoryTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).setDateFormat(new StdDateFormat());

  @Test
  void shouldCreateCategoryWithAllFields() {
    Instant now = Instant.now();
    Category category = Category.builder().name("Agile Practices").description("Core agile development practices").createdDate(now.minusSeconds(3600)).lastUpdatedDate(now).build();

    assertThat(category.getName()).isEqualTo("Agile Practices");
    assertThat(category.getDescription()).isEqualTo("Core agile development practices");
    assertThat(category.getCreatedDate()).isEqualTo(now.minusSeconds(3600));
    assertThat(category.getLastUpdatedDate()).isEqualTo(now);
  }

  @Test
  void shouldCreateCategoryUsingTestFactory() {
    Category category = TestObjectFactory.createMockedCategory();

    assertThat(category.getName()).isEqualTo("Test Category");
    assertThat(category.getDescription()).isEqualTo("Test category description");
    assertThat(category.getCreatedDate()).isNotNull();
    assertThat(category.getLastUpdatedDate()).isNotNull();
  }

  @Test
  void shouldCreateCategoryWithCustomNameAndDescription() {
    Category category = TestObjectFactory.createMockedCategory("Custom Category", "Custom description");

    assertThat(category.getName()).isEqualTo("Custom Category");
    assertThat(category.getDescription()).isEqualTo("Custom description");
    assertThat(category.getCreatedDate()).isNotNull();
    assertThat(category.getLastUpdatedDate()).isNotNull();
  }

  @Test
  void shouldSerializeAndDeserializeCategoryToJson() throws Exception {
    Category originalCategory = Category.builder().name("JSON Test").description("JSON serialization test").build();

    String json = OBJECT_MAPPER.writeValueAsString(originalCategory);
    Category deserializedCategory = OBJECT_MAPPER.readValue(json, Category.class);

    assertThat(deserializedCategory.getName()).isEqualTo(originalCategory.getName());
    assertThat(deserializedCategory.getDescription()).isEqualTo(originalCategory.getDescription());
  }

  @Test
  void shouldHandleNullFieldsInCategory() {
    Category category = new Category();

    assertThat(category.getName()).isNull();
    assertThat(category.getDescription()).isNull();
    assertThat(category.getCreatedDate()).isNull();
    assertThat(category.getLastUpdatedDate()).isNull();
  }

  @Test
  void shouldHandleEqualsAndHashCodeForCategory() {
    Instant now = Instant.now();
    String sameId = "test-id-123";
    Category category1 = Category.builder().id(sameId).name("Test Category").description("Test description").createdDate(now).lastUpdatedDate(now).build();

    Category category2 = Category.builder().id(sameId).name("Test Category").description("Test description").createdDate(now).lastUpdatedDate(now).build();

    Category category3 = Category.builder().name("Different Category").description("Different description").createdDate(now).lastUpdatedDate(now).build();

    assertThat(category1).isEqualTo(category2);
    assertThat(category1).isNotEqualTo(category3);
    assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    assertThat(category1.hashCode()).isNotEqualTo(category3.hashCode());
  }

  @Test
  void shouldHandleToStringForCategory() {
    Category category = TestObjectFactory.createMockedCategory("ToString Test", "Test toString method");
    String toStringResult = category.toString();

    assertThat(toStringResult).contains("ToString Test");
    assertThat(toStringResult).contains("Test toString method");
    assertThat(toStringResult).contains("Category");
  }

  @Test
  void shouldHandleEmptyStringsInCategory() {
    Category category = Category.builder().name("").description("").createdDate(Instant.now()).lastUpdatedDate(Instant.now()).build();

    assertThat(category.getName()).isEmpty();
    assertThat(category.getDescription()).isEmpty();
  }

  @Test
  void shouldHandleLongStringsInCategory() {
    String longName = "A".repeat(1000);
    String longDescription = "B".repeat(2000);

    Category category = Category.builder().name(longName).description(longDescription).createdDate(Instant.now()).lastUpdatedDate(Instant.now()).build();

    assertThat(category.getName()).hasSize(1000);
    assertThat(category.getDescription()).hasSize(2000);
  }

  @Test
  void shouldHandleSpecialCharactersInCategory() {
    Category category = Category.builder().name("Category with special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>?,./").description("Description with unicode: áéíóú ç ñ 中文 🚀 💻").createdDate(Instant.now()).lastUpdatedDate(Instant.now()).build();

    assertThat(category.getName()).contains("!@#$%^&*()_+-={}[]|\\:;\"'<>?,./");
    assertThat(category.getDescription()).contains("áéíóú ç ñ 中文 🚀 💻");
  }
}