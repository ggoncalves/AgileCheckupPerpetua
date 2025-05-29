package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backup utility for Team table data before migration.
 * Creates a JSON file with all current Team records.
 */
@Log4j2
public class TeamDataBackup {
    
    private static final String TEAM_TABLE_NAME = "Team";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public TeamDataBackup() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public String backup() {
        log.info("Starting Team table backup...");
        
        List<Map<String, AttributeValue>> allTeams = new ArrayList<>();
        int count = 0;
        
        try {
            // Scan all teams
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(TEAM_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                allTeams.addAll(items);
                count += items.size();
                
                if (count % 10 == 0) {
                    log.info("Backed up {} teams...", count);
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("team_backup_%s.json", timestamp);
            
            // Convert AttributeValue format to plain JSON
            List<Map<String, Object>> plainTeams = new ArrayList<>();
            for (Map<String, AttributeValue> team : allTeams) {
                plainTeams.add(convertToPlainMap(team));
            }
            
            // Write to file
            File backupFile = new File(filename);
            try (FileWriter writer = new FileWriter(backupFile)) {
                objectMapper.writeValue(writer, plainTeams);
            }
            
            log.info("Backup completed successfully!");
            log.info("Total teams backed up: {}", count);
            log.info("Backup file: {}", backupFile.getAbsolutePath());
            
            return backupFile.getAbsolutePath();
            
        } catch (IOException e) {
            log.error("Failed to write backup file: {}", e.getMessage(), e);
            throw new RuntimeException("Backup failed", e);
        } catch (Exception e) {
            log.error("Fatal error during backup: {}", e.getMessage(), e);
            throw new RuntimeException("Backup failed", e);
        }
    }
    
    /**
     * Convert DynamoDB AttributeValue map to plain Java map
     */
    private Map<String, Object> convertToPlainMap(Map<String, AttributeValue> item) {
        Map<String, Object> plainMap = new HashMap<>();
        
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            plainMap.put(key, convertAttributeValue(value));
        }
        
        return plainMap;
    }
    
    private Object convertAttributeValue(AttributeValue value) {
        if (value.getS() != null) {
            return value.getS();
        } else if (value.getN() != null) {
            return value.getN();
        } else if (value.getBOOL() != null) {
            return value.getBOOL();
        } else if (value.getM() != null) {
            return convertToPlainMap(value.getM());
        } else if (value.getL() != null) {
            List<Object> list = new ArrayList<>();
            for (AttributeValue item : value.getL()) {
                list.add(convertAttributeValue(item));
            }
            return list;
        } else if (value.getNULL() != null && value.getNULL()) {
            return null;
        }
        return null;
    }
    
    public static void main(String[] args) {
        log.info("=== Team Data Backup Tool ===");
        
        TeamDataBackup backup = new TeamDataBackup();
        String backupPath = backup.backup();
        
        log.info("Backup completed: {}", backupPath);
        log.info("You can now safely run the migration script.");
    }
}