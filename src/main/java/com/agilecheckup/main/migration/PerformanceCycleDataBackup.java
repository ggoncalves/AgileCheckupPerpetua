package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backup script for PerformanceCycle data before V1-to-V2 migration.
 * 
 * This script creates a JSON backup of all PerformanceCycle records to allow for
 * recovery in case the migration goes wrong.
 */
@Log4j2
public class PerformanceCycleDataBackup {
    
    private static final String PERFORMANCE_CYCLE_TABLE_NAME = "PerformanceCycle";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public PerformanceCycleDataBackup() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public void backup() {
        log.info("Starting PerformanceCycle data backup...");
        
        try {
            List<Map<String, Object>> backupData = new ArrayList<>();
            int totalCount = 0;
            
            // Scan all performance cycles
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(PERFORMANCE_CYCLE_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                for (Map<String, AttributeValue> item : items) {
                    totalCount++;
                    Map<String, Object> backupItem = convertAttributeValueMapToObject(item);
                    backupData.add(backupItem);
                    
                    if (totalCount % 100 == 0) {
                        log.info("Processed {} PerformanceCycle records...", totalCount);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            // Create backup file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFileName = String.format("performancecycle_backup_%s.json", timestamp);
            
            Map<String, Object> backupDocument = new HashMap<>();
            backupDocument.put("timestamp", new Date().toString());
            backupDocument.put("tableName", PERFORMANCE_CYCLE_TABLE_NAME);
            backupDocument.put("totalRecords", totalCount);
            backupDocument.put("records", backupData);
            
            // Write to file
            File backupFile = new File(backupFileName);
            try (FileWriter writer = new FileWriter(backupFile)) {
                objectMapper.writeValue(writer, backupDocument);
            }
            
            log.info("PerformanceCycle backup completed!");
            log.info("Total records backed up: {}", totalCount);
            log.info("Backup file: {}", backupFile.getAbsolutePath());
            log.info("File size: {} MB", String.format("%.2f", backupFile.length() / (1024.0 * 1024.0)));
            
        } catch (Exception e) {
            log.error("Fatal error during PerformanceCycle backup: {}", e.getMessage(), e);
            throw new RuntimeException("PerformanceCycle backup failed", e);
        }
    }
    
    /**
     * Convert DynamoDB AttributeValue map to plain Java objects for JSON serialization
     */
    private Map<String, Object> convertAttributeValueMapToObject(Map<String, AttributeValue> attributeMap) {
        Map<String, Object> objectMap = new HashMap<>();
        
        for (Map.Entry<String, AttributeValue> entry : attributeMap.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            
            if (value.getS() != null) {
                objectMap.put(key, value.getS());
            } else if (value.getN() != null) {
                objectMap.put(key, value.getN());
            } else if (value.getBOOL() != null) {
                objectMap.put(key, value.getBOOL());
            } else if (value.getSS() != null) {
                objectMap.put(key, value.getSS());
            } else if (value.getNS() != null) {
                objectMap.put(key, value.getNS());
            } else if (value.getM() != null) {
                objectMap.put(key, convertAttributeValueMapToObject(value.getM()));
            } else if (value.getL() != null) {
                List<Object> list = new ArrayList<>();
                for (AttributeValue listItem : value.getL()) {
                    Map<String, AttributeValue> tempMap = new HashMap<>();
                    tempMap.put("item", listItem);
                    list.add(convertAttributeValueMapToObject(tempMap).get("item"));
                }
                objectMap.put(key, list);
            } else if (value.getNULL() != null && value.getNULL()) {
                objectMap.put(key, null);
            }
        }
        
        return objectMap;
    }
    
    public static void main(String[] args) {
        log.info("=== PerformanceCycle Data Backup Tool ===");
        log.info("This will create a JSON backup of all PerformanceCycle records.");
        log.info("The backup file will be created in the current directory.");
        log.info("");
        log.info("Starting backup in 3 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.info("Backup cancelled");
            return;
        }
        
        PerformanceCycleDataBackup backup = new PerformanceCycleDataBackup();
        backup.backup();
    }
}