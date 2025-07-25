package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.entity.base.AuditableEntityV2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
public abstract class PersonV2 extends AuditableEntityV2 {

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("name")))
    private String name;

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("email")))
    private String email;

    @Getter(onMethod_=@__(@DynamoDbAttribute("phone")))
    private String phone;

    @Getter(onMethod_=@__({@DynamoDbAttribute("address"), @DynamoDbConvertedBy(AddressV2AttributeConverter.class)}))
    private AddressV2 address;

    @Getter(onMethod_=@__({@DynamoDbAttribute("personDocumentType"), @DynamoDbConvertedBy(PersonDocumentTypeConverter.class)}))
    private PersonDocumentType personDocumentType;

    @Getter(onMethod_=@__(@DynamoDbAttribute("documentNumber")))
    private String documentNumber;

    // Converters for enums and embedded objects
    public static class PersonDocumentTypeConverter implements AttributeConverter<PersonDocumentType> {
        @Override
        public AttributeValue transformFrom(PersonDocumentType input) {
            return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
        }

        @Override
        public PersonDocumentType transformTo(AttributeValue input) {
            if (input == null || input.nul() != null && input.nul()) {
                return null;
            }
            return PersonDocumentType.valueOf(input.s());
        }

        @Override
        public EnhancedType<PersonDocumentType> type() {
            return EnhancedType.of(PersonDocumentType.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }

    public static class AddressV2AttributeConverter implements AttributeConverter<AddressV2> {
        @Override
        public AttributeValue transformFrom(AddressV2 input) {
            if (input == null) {
                return AttributeValue.builder().nul(true).build();
            }
            // Convert AddressV2 to JSON string for storage
            return AttributeValue.builder().s(String.format(
                "{\"id\":\"%s\",\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zipcode\":\"%s\",\"country\":\"%s\"}",
                input.getId() != null ? input.getId() : "",
                input.getStreet() != null ? input.getStreet().replace("\"", "\\\"") : "",
                input.getCity() != null ? input.getCity().replace("\"", "\\\"") : "",
                input.getState() != null ? input.getState().replace("\"", "\\\"") : "",
                input.getZipcode() != null ? input.getZipcode().replace("\"", "\\\"") : "",
                input.getCountry() != null ? input.getCountry().replace("\"", "\\\"") : ""
            )).build();
        }

        @Override
        public AddressV2 transformTo(AttributeValue input) {
            if (input == null || input.nul() != null && input.nul()) {
                return null;
            }
            // Simple JSON parsing for AddressV2
            String json = input.s();
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            
            // Parse JSON manually (simple implementation)
            AddressV2 address = new AddressV2();
            if (json.contains("\"id\":\"")) {
                String id = extractJsonValue(json, "id");
                if (!id.isEmpty()) address.setId(id);
            }
            if (json.contains("\"street\":\"")) {
                address.setStreet(extractJsonValue(json, "street"));
            }
            if (json.contains("\"city\":\"")) {
                address.setCity(extractJsonValue(json, "city"));
            }
            if (json.contains("\"state\":\"")) {
                address.setState(extractJsonValue(json, "state"));
            }
            if (json.contains("\"zipcode\":\"")) {
                address.setZipcode(extractJsonValue(json, "zipcode"));
            }
            if (json.contains("\"country\":\"")) {
                address.setCountry(extractJsonValue(json, "country"));
            }
            return address;
        }

        private String extractJsonValue(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int startIndex = json.indexOf(pattern);
            if (startIndex == -1) return "";
            startIndex += pattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return "";
            return json.substring(startIndex, endIndex).replace("\\\"", "\"");
        }

        @Override
        public EnhancedType<AddressV2> type() {
            return EnhancedType.of(AddressV2.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }
}