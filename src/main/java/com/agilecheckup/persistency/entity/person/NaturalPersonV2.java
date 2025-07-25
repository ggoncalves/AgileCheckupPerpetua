package com.agilecheckup.persistency.entity.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class NaturalPersonV2 extends PersonV2 {

    @Getter(onMethod_=@__(@DynamoDbAttribute("aliasName")))
    private String aliasName;

    @Getter(onMethod_=@__({@DynamoDbAttribute("gender"), @DynamoDbConvertedBy(GenderConverter.class)}))
    private Gender gender;

    @Getter(onMethod_=@__({@DynamoDbAttribute("genderPronoun"), @DynamoDbConvertedBy(GenderPronounConverter.class)}))
    private GenderPronoun genderPronoun;

    // Converters for enums
    public static class GenderConverter implements AttributeConverter<Gender> {
        @Override
        public AttributeValue transformFrom(Gender input) {
            return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
        }

        @Override
        public Gender transformTo(AttributeValue input) {
            if (input == null || input.nul() != null && input.nul()) {
                return null;
            }
            return Gender.valueOf(input.s());
        }

        @Override
        public EnhancedType<Gender> type() {
            return EnhancedType.of(Gender.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }

    public static class GenderPronounConverter implements AttributeConverter<GenderPronoun> {
        @Override
        public AttributeValue transformFrom(GenderPronoun input) {
            return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
        }

        @Override
        public GenderPronoun transformTo(AttributeValue input) {
            if (input == null || input.nul() != null && input.nul()) {
                return null;
            }
            return GenderPronoun.valueOf(input.s());
        }

        @Override
        public EnhancedType<GenderPronoun> type() {
            return EnhancedType.of(GenderPronoun.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }
}