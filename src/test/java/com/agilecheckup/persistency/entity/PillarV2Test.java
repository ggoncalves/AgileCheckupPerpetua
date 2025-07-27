package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.converter.CategoryMapAttributeConverter;
import com.agilecheckup.util.TestObjectFactoryV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PillarV2Test {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new StdDateFormat());
    private final CategoryMapAttributeConverter converter = new CategoryMapAttributeConverter();

    @Test
    void shouldCreatePillarV2WithAllFields() {
        Map<String, CategoryV2> categoryMap = TestObjectFactoryV2.createMockedCategoryMap(3);
        Instant now = Instant.now();

        PillarV2 pillar = PillarV2.builder()
                .name("Technical Excellence")
                .description("Technical practices and excellence")
                .categoryMap(categoryMap)
                .createdDate(now.minusSeconds(3600))
                .lastUpdatedDate(now)
                .build();

        assertThat(pillar.getName()).isEqualTo("Technical Excellence");
        assertThat(pillar.getDescription()).isEqualTo("Technical practices and excellence");
        assertThat(pillar.getCategoryMap()).hasSize(3);
        assertThat(pillar.getCreatedDate()).isEqualTo(now.minusSeconds(3600));
        assertThat(pillar.getLastUpdatedDate()).isEqualTo(now);
    }

    @Test
    void shouldCreatePillarV2UsingTestFactory() {
        PillarV2 pillar = TestObjectFactoryV2.createMockedPillarV2();

        assertThat(pillar.getName()).isEqualTo("Test Pillar");
        assertThat(pillar.getDescription()).isEqualTo("Test pillar description");
        assertThat(pillar.getCategoryMap()).hasSize(3);
        assertThat(pillar.getCategoryMap()).containsKeys("cat1", "cat2", "cat3");
        assertThat(pillar.getCreatedDate()).isNotNull();
        assertThat(pillar.getLastUpdatedDate()).isNotNull();
    }

    @Test
    void shouldCreatePillarV2WithCustomNameAndDescription() {
        PillarV2 pillar = TestObjectFactoryV2.createMockedPillarV2("Custom Pillar", "Custom description");

        assertThat(pillar.getName()).isEqualTo("Custom Pillar");
        assertThat(pillar.getDescription()).isEqualTo("Custom description");
        assertThat(pillar.getCategoryMap()).hasSize(1);
        assertThat(pillar.getCategoryMap()).containsKey("default");
    }

    @Test
    void shouldCreatePillarV2WithCustomCategories() {
        Map<String, CategoryV2> customCategories = new HashMap<>();
        customCategories.put("quality", TestObjectFactoryV2.createMockedCategoryV2("Quality", "Code quality"));
        customCategories.put("testing", TestObjectFactoryV2.createMockedCategoryV2("Testing", "Test practices"));
        customCategories.put("deployment", TestObjectFactoryV2.createMockedCategoryV2("Deployment", "Deployment practices"));

        PillarV2 pillar = TestObjectFactoryV2.createMockedPillarV2WithCategories("DevOps", "DevOps practices", customCategories);

        assertThat(pillar.getName()).isEqualTo("DevOps");
        assertThat(pillar.getDescription()).isEqualTo("DevOps practices");
        assertThat(pillar.getCategoryMap()).hasSize(3);
        assertThat(pillar.getCategoryMap()).containsKeys("quality", "testing", "deployment");
    }

    @Test
    void shouldSerializeAndDeserializePillarV2ToJson() throws Exception {
        Map<String, CategoryV2> categoryMap = new HashMap<>();
        categoryMap.put("test", CategoryV2.builder().name("Test Category").description("Test description").build());
        
        PillarV2 originalPillar = PillarV2.builder()
                .name("JSON Test")
                .description("JSON serialization test")
                .categoryMap(categoryMap)
                .build();

        String json = OBJECT_MAPPER.writeValueAsString(originalPillar);
        PillarV2 deserializedPillar = OBJECT_MAPPER.readValue(json, PillarV2.class);

        assertThat(deserializedPillar.getName()).isEqualTo(originalPillar.getName());
        assertThat(deserializedPillar.getDescription()).isEqualTo(originalPillar.getDescription());
        assertThat(deserializedPillar.getCategoryMap()).hasSize(originalPillar.getCategoryMap().size());
    }

    @Test
    void shouldConvertCategoryMapToAttributeValue() {
        Map<String, CategoryV2> categoryMap = new HashMap<>();
        categoryMap.put("test", CategoryV2.builder().name("Test Category").description("Test description").build());

        AttributeValue attributeValue = converter.transformFrom(categoryMap);

        assertThat(attributeValue.s()).isNotNull();
        assertThat(attributeValue.s()).contains("Test Category");
        assertThat(attributeValue.s()).contains("Test description");
    }

    @Test
    void shouldConvertAttributeValueToCategoryMap() {
        Map<String, CategoryV2> originalMap = new HashMap<>();
        originalMap.put("test", CategoryV2.builder().name("Test Category").description("Test description").build());

        AttributeValue attributeValue = converter.transformFrom(originalMap);
        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);

        assertThat(deserializedMap).hasSize(1);
        assertThat(deserializedMap).containsKey("test");
        assertThat(deserializedMap.get("test").getName()).isEqualTo("Test Category");
        assertThat(deserializedMap.get("test").getDescription()).isEqualTo("Test description");
    }

    @Test
    void shouldHandleNullCategoryMapInConverter() {
        AttributeValue attributeValue = converter.transformFrom(null);

        assertThat(attributeValue.nul()).isTrue();

        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);
        assertThat(deserializedMap).isNull();
    }

    @Test
    void shouldHandleEmptyCategoryMapInConverter() {
        Map<String, CategoryV2> emptyMap = new HashMap<>();
        AttributeValue attributeValue = converter.transformFrom(emptyMap);
        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);

        assertThat(deserializedMap).isEmpty();
    }

    @Test
    void shouldHandleLargeCategoryMapInConverter() {
        Map<String, CategoryV2> largeMap = new HashMap<>();
        for (int i = 1; i <= 100; i++) {
            String key = "category" + i;
            largeMap.put(key, CategoryV2.builder().name("Category " + i).description("Description " + i).build());
        }

        AttributeValue attributeValue = converter.transformFrom(largeMap);
        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);

        assertThat(deserializedMap).hasSize(100);
        assertThat(deserializedMap.get("category1").getName()).isEqualTo("Category 1");
        assertThat(deserializedMap.get("category100").getName()).isEqualTo("Category 100");
    }

    @Test
    void shouldThrowExceptionForInvalidJsonInConverter() {
        AttributeValue invalidJsonAttribute = AttributeValue.builder().s("invalid json").build();

        assertThatThrownBy(() -> converter.transformTo(invalidJsonAttribute))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to deserialize CategoryV2 map");
    }

    @Test
    void shouldHandleNullFieldsInPillarV2() {
        PillarV2 pillar = new PillarV2();

        assertThat(pillar.getName()).isNull();
        assertThat(pillar.getDescription()).isNull();
        assertThat(pillar.getCategoryMap()).isNull();
        assertThat(pillar.getCreatedDate()).isNull();
        assertThat(pillar.getLastUpdatedDate()).isNull();
    }

    @Test
    void shouldHandleEqualsAndHashCodeForPillarV2() {
        Map<String, CategoryV2> categoryMap = TestObjectFactoryV2.createMockedCategoryMap(2);
        Instant now = Instant.now();

        String sameId = "test-pillar-id-123";
        PillarV2 pillar1 = PillarV2.builder()
                .id(sameId)
                .name("Test Pillar")
                .description("Test description")
                .categoryMap(categoryMap)
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        PillarV2 pillar2 = PillarV2.builder()
                .id(sameId)
                .name("Test Pillar")
                .description("Test description")
                .categoryMap(categoryMap)
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        PillarV2 pillar3 = PillarV2.builder()
                .name("Different Pillar")
                .description("Different description")
                .categoryMap(categoryMap)
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        assertThat(pillar1).isEqualTo(pillar2);
        assertThat(pillar1).isNotEqualTo(pillar3);
        assertThat(pillar1.hashCode()).isEqualTo(pillar2.hashCode());
    }

    @Test
    void shouldHandleToStringForPillarV2() {
        PillarV2 pillar = TestObjectFactoryV2.createMockedPillarV2("ToString Test", "Test toString method");
        String toStringResult = pillar.toString();

        assertThat(toStringResult).contains("ToString Test");
        assertThat(toStringResult).contains("Test toString method");
        assertThat(toStringResult).contains("PillarV2");
        assertThat(toStringResult).contains("categoryMap");
    }

    @Test
    void shouldHandleCategoryMapWithNullValues() {
        Map<String, CategoryV2> categoryMapWithNull = new HashMap<>();
        categoryMapWithNull.put("valid", CategoryV2.builder().name("Valid").description("Valid category").build());
        categoryMapWithNull.put("null", null);

        PillarV2 pillar = PillarV2.builder()
                .name("Null Test")
                .description("Testing null values in category map")
                .categoryMap(categoryMapWithNull)
                .build();

        assertThat(pillar.getCategoryMap()).hasSize(2);
        assertThat(pillar.getCategoryMap().get("valid")).isNotNull();
        assertThat(pillar.getCategoryMap().get("null")).isNull();
    }

    @Test
    void shouldHandleCategoryMapSerializationWithNullValues() {
        Map<String, CategoryV2> categoryMapWithNull = new HashMap<>();
        categoryMapWithNull.put("valid", CategoryV2.builder().name("Valid").description("Valid category").build());
        categoryMapWithNull.put("null", null);

        AttributeValue attributeValue = converter.transformFrom(categoryMapWithNull);
        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);

        assertThat(deserializedMap).hasSize(2);
        assertThat(deserializedMap.get("valid")).isNotNull();
        assertThat(deserializedMap.get("null")).isNull();
    }

    @Test
    void shouldHandleCategoryMapWithSpecialCharacterKeys() {
        Map<String, CategoryV2> specialKeyMap = new HashMap<>();
        specialKeyMap.put("key-with-dashes", CategoryV2.builder().name("Dashes").description("Category with dashes").build());
        specialKeyMap.put("key_with_underscores", CategoryV2.builder().name("Underscores").description("Category with underscores").build());
        specialKeyMap.put("key.with.dots", CategoryV2.builder().name("Dots").description("Category with dots").build());

        AttributeValue attributeValue = converter.transformFrom(specialKeyMap);
        Map<String, CategoryV2> deserializedMap = converter.transformTo(attributeValue);

        assertThat(deserializedMap).hasSize(3);
        assertThat(deserializedMap).containsKeys("key-with-dashes", "key_with_underscores", "key.with.dots");
    }
}