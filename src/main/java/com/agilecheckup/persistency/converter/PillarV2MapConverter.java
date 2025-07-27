package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.PillarV2;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.util.Map;

/**
 * DynamoDB V1 type converter for Map<String, PillarV2>
 * This converter allows V1 AssessmentMatrix entities to store V2 PillarV2 objects as JSON
 */
public class PillarV2MapConverter implements DynamoDBTypeConverter<String, Map<String, PillarV2>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new StdDateFormat());
    
    private static final TypeReference<Map<String, PillarV2>> TYPE_REFERENCE = 
            new TypeReference<Map<String, PillarV2>>() {};

    @Override
    public String convert(Map<String, PillarV2> object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PillarV2 map to JSON", e);
        }
    }

    @Override
    public Map<String, PillarV2> unconvert(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize PillarV2 map from JSON: " + json, e);
        }
    }
}