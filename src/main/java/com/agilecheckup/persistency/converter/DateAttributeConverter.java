package com.agilecheckup.persistency.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAttributeConverter implements AttributeConverter<Date> {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    @Override
    public AttributeValue transformFrom(Date date) {
        if (date == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        synchronized (DATE_FORMAT) {
            return AttributeValue.builder().s(DATE_FORMAT.format(date)).build();
        }
    }
    
    @Override
    public Date transformTo(AttributeValue attributeValue) {
        if (attributeValue.nul() != null && attributeValue.nul()) {
            return null;
        }
        
        String dateString = attributeValue.s();
        if (dateString == null) {
            return null;
        }
        
        try {
            synchronized (DATE_FORMAT) {
                return DATE_FORMAT.parse(dateString);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse date string: " + dateString, e);
        }
    }
    
    @Override
    public EnhancedType<Date> type() {
        return EnhancedType.of(Date.class);
    }
    
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}