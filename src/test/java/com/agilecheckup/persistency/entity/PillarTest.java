package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.converter.CategoryMapAttributeConverter;
import com.agilecheckup.util.TestObjectFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ExtendWith(MockitoExtension.class)
class PillarTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).setDateFormat(new StdDateFormat());
  private final CategoryMapAttributeConverter converter = new CategoryMapAttributeConverter();

  @Test
  void shouldCreatePillarWithAllFields() {
    Map<String, Category> categoryMap = TestObjectFactory.createMockedCategoryMap(3);
    Instant now = Instant.now();

    Pillar pillar = Pillar.builder().name("Technical Excellence").description("Technical practices and excellence").categoryMap(categoryMap).createdDate(now.minusSeconds(3600)).lastUpdatedDate(now).build();

    assertThat(pillar.getName()).isEqualTo("Technical Excellence");
    assertThat(pillar.getDescription()).isEqualTo("Technical practices and excellence");
    assertThat(pillar.getCategoryMap()).hasSize(3);
    assertThat(pillar.getCreatedDate()).isEqualTo(now.minusSeconds(3600));
    assertThat(pillar.getLastUpdatedDate()).isEqualTo(now);
  }

  @Test
  void shouldCreatePillarUsingTestFactory() {
    Pillar pillar = TestObjectFactory.createMockedPillar();

    assertThat(pillar.getName()).isEqualTo("Test Pillar");
    assertThat(pillar.getDescription()).isEqualTo("Test pillar description");
    assertThat(pillar.getCategoryMap()).hasSize(3);
    assertThat(pillar.getCategoryMap()).containsKeys("cat1", "cat2", "cat3");
    assertThat(pillar.getCreatedDate()).isNotNull();
    assertThat(pillar.getLastUpdatedDate()).isNotNull();
  }

  @Test
  void shouldCreatePillarWithCustomNameAndDescription() {
    Pillar pillar = TestObjectFactory.createMockedPillar("Custom Pillar", "Custom description");

    assertThat(pillar.getName()).isEqualTo("Custom Pillar");
    assertThat(pillar.getDescription()).isEqualTo("Custom description");
    assertThat(pillar.getCategoryMap()).hasSize(1);
    assertThat(pillar.getCategoryMap()).containsKey("default");
  }

  @Test
  void shouldCreatePillarWithCustomCategories() {
    Map<String, Category> customCategories = new HashMap<>();
    customCategories.put("quality", TestObjectFactory.createMockedCategory("Quality", "Code quality"));
    customCategories.put("testing", TestObjectFactory.createMockedCategory("Testing", "Test practices"));
    customCategories.put("deployment", TestObjectFactory.createMockedCategory("Deployment", "Deployment practices"));

    Pillar pillar = TestObjectFactory.createMockedPillarWithCategories("DevOps", "DevOps practices", customCategories);

    assertThat(pillar.getName()).isEqualTo("DevOps");
    assertThat(pillar.getDescription()).isEqualTo("DevOps practices");
    assertThat(pillar.getCategoryMap()).hasSize(3);
    assertThat(pillar.getCategoryMap()).containsKeys("quality", "testing", "deployment");
  }

  @Test
  void shouldSerializeAndDeserializePillarToJson() throws Exception {
    Map<String, Category> categoryMap = new HashMap<>();
    categoryMap.put("test", Category.builder().name("Test Category").description("Test description").build());

    Pillar originalPillar = Pillar.builder().name("JSON Test").description("JSON serialization test").categoryMap(categoryMap).build();

    String json = OBJECT_MAPPER.writeValueAsString(originalPillar);
    Pillar deserializedPillar = OBJECT_MAPPER.readValue(json, Pillar.class);

    assertThat(deserializedPillar.getName()).isEqualTo(originalPillar.getName());
    assertThat(deserializedPillar.getDescription()).isEqualTo(originalPillar.getDescription());
    assertThat(deserializedPillar.getCategoryMap()).hasSize(originalPillar.getCategoryMap().size());
  }

  @Test
  void shouldConvertCategoryMapToAttributeValue() {
    Map<String, Category> categoryMap = new HashMap<>();
    categoryMap.put("test", Category.builder().name("Test Category").description("Test description").build());

    AttributeValue attributeValue = converter.transformFrom(categoryMap);

    assertThat(attributeValue.s()).isNotNull();
    assertThat(attributeValue.s()).contains("Test Category");
    assertThat(attributeValue.s()).contains("Test description");
  }

  @Test
  void shouldConvertAttributeValueToCategoryMap() {
    Map<String, Category> originalMap = new HashMap<>();
    originalMap.put("test", Category.builder().name("Test Category").description("Test description").build());

    AttributeValue attributeValue = converter.transformFrom(originalMap);
    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);

    assertThat(deserializedMap).hasSize(1);
    assertThat(deserializedMap).containsKey("test");
    assertThat(deserializedMap.get("test").getName()).isEqualTo("Test Category");
    assertThat(deserializedMap.get("test").getDescription()).isEqualTo("Test description");
  }

  @Test
  void shouldHandleNullCategoryMapInConverter() {
    AttributeValue attributeValue = converter.transformFrom(null);

    assertThat(attributeValue.nul()).isTrue();

    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);
    assertThat(deserializedMap).isNull();
  }

  @Test
  void shouldHandleEmptyCategoryMapInConverter() {
    Map<String, Category> emptyMap = new HashMap<>();
    AttributeValue attributeValue = converter.transformFrom(emptyMap);
    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);

    assertThat(deserializedMap).isEmpty();
  }

  @Test
  void shouldHandleLargeCategoryMapInConverter() {
    Map<String, Category> largeMap = new HashMap<>();
    for (int i = 1; i <= 100; i++) {
      String key = "category" + i;
      largeMap.put(key, Category.builder().name("Category " + i).description("Description " + i).build());
    }

    AttributeValue attributeValue = converter.transformFrom(largeMap);
    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);

    assertThat(deserializedMap).hasSize(100);
    assertThat(deserializedMap.get("category1").getName()).isEqualTo("Category 1");
    assertThat(deserializedMap.get("category100").getName()).isEqualTo("Category 100");
  }

  @Test
  void shouldThrowExceptionForInvalidJsonInConverter() {
    AttributeValue invalidJsonAttribute = AttributeValue.builder().s("invalid json").build();

    assertThatThrownBy(() -> converter.transformTo(invalidJsonAttribute)).isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to deserialize Category map");
  }

  @Test
  void shouldHandleNullFieldsInPillar() {
    Pillar pillar = new Pillar();

    assertThat(pillar.getName()).isNull();
    assertThat(pillar.getDescription()).isNull();
    assertThat(pillar.getCategoryMap()).isNull();
    assertThat(pillar.getCreatedDate()).isNull();
    assertThat(pillar.getLastUpdatedDate()).isNull();
  }

  @Test
  void shouldHandleEqualsAndHashCodeForPillar() {
    Map<String, Category> categoryMap = TestObjectFactory.createMockedCategoryMap(2);
    Instant now = Instant.now();

    String sameId = "test-pillar-id-123";
    Pillar pillar1 = Pillar.builder().id(sameId).name("Test Pillar").description("Test description").categoryMap(categoryMap).createdDate(now).lastUpdatedDate(now).build();

    Pillar pillar2 = Pillar.builder().id(sameId).name("Test Pillar").description("Test description").categoryMap(categoryMap).createdDate(now).lastUpdatedDate(now).build();

    Pillar pillar3 = Pillar.builder().name("Different Pillar").description("Different description").categoryMap(categoryMap).createdDate(now).lastUpdatedDate(now).build();

    assertThat(pillar1).isEqualTo(pillar2);
    assertThat(pillar1).isNotEqualTo(pillar3);
    assertThat(pillar1.hashCode()).isEqualTo(pillar2.hashCode());
  }

  @Test
  void shouldHandleToStringForPillar() {
    Pillar pillar = TestObjectFactory.createMockedPillar("ToString Test", "Test toString method");
    String toStringResult = pillar.toString();

    assertThat(toStringResult).contains("ToString Test");
    assertThat(toStringResult).contains("Test toString method");
    assertThat(toStringResult).contains("Pillar");
    assertThat(toStringResult).contains("categoryMap");
  }

  @Test
  void shouldHandleCategoryMapWithNullValues() {
    Map<String, Category> categoryMapWithNull = new HashMap<>();
    categoryMapWithNull.put("valid", Category.builder().name("Valid").description("Valid category").build());
    categoryMapWithNull.put("null", null);

    Pillar pillar = Pillar.builder().name("Null Test").description("Testing null values in category map").categoryMap(categoryMapWithNull).build();

    assertThat(pillar.getCategoryMap()).hasSize(2);
    assertThat(pillar.getCategoryMap().get("valid")).isNotNull();
    assertThat(pillar.getCategoryMap().get("null")).isNull();
  }

  @Test
  void shouldHandleCategoryMapSerializationWithNullValues() {
    Map<String, Category> categoryMapWithNull = new HashMap<>();
    categoryMapWithNull.put("valid", Category.builder().name("Valid").description("Valid category").build());
    categoryMapWithNull.put("null", null);

    AttributeValue attributeValue = converter.transformFrom(categoryMapWithNull);
    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);

    assertThat(deserializedMap).hasSize(2);
    assertThat(deserializedMap.get("valid")).isNotNull();
    assertThat(deserializedMap.get("null")).isNull();
  }

  @Test
  void shouldHandleCategoryMapWithSpecialCharacterKeys() {
    Map<String, Category> specialKeyMap = new HashMap<>();
    specialKeyMap.put("key-with-dashes", Category.builder().name("Dashes").description("Category with dashes").build());
    specialKeyMap.put("key_with_underscores", Category.builder().name("Underscores").description("Category with underscores").build());
    specialKeyMap.put("key.with.dots", Category.builder().name("Dots").description("Category with dots").build());

    AttributeValue attributeValue = converter.transformFrom(specialKeyMap);
    Map<String, Category> deserializedMap = converter.transformTo(attributeValue);

    assertThat(deserializedMap).hasSize(3);
    assertThat(deserializedMap).containsKeys("key-with-dashes", "key_with_underscores", "key.with.dots");
  }
}