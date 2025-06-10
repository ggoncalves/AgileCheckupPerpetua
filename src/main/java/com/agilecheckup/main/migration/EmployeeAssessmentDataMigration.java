package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time migration script to update EmployeeAssessment records from old structure (embedded team object)
 * to new structure (teamId string).
 * 
 * Old structure:
 * {
 *   "id": "...",
 *   "team": {
 *     "id": "...",
 *     "name": "...",
 *     "departmentId": "...",
 *     ...
 *   }
 * }
 * 
 * New structure:
 * {
 *   "id": "...",
 *   "teamId": "..."
 * }
 */
@Log4j2
public class EmployeeAssessmentDataMigration {
    
    private static final String EMPLOYEE_ASSESSMENT_TABLE_NAME = "EmployeeAssessment";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public EmployeeAssessmentDataMigration() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
    }
    
    public void migrate() {
        log.info("Starting EmployeeAssessment data migration...");
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger migratedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        try {
            // Scan all employee assessments
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(EMPLOYEE_ASSESSMENT_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                for (Map<String, AttributeValue> item : items) {
                    totalCount.incrementAndGet();
                    
                    try {
                        if (migrateEmployeeAssessmentItem(item)) {
                            migratedCount.incrementAndGet();
                            String assessmentId = item.get("id").getS();
                            log.info("Migrated employee assessment: {}", assessmentId);
                        } else {
                            skippedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        String assessmentId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
                        log.error("Error migrating employee assessment {}: {}", assessmentId, e.getMessage(), e);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            log.info("Migration completed!");
            log.info("Total employee assessments processed: {}", totalCount.get());
            log.info("Employee assessments migrated: {}", migratedCount.get());
            log.info("Employee assessments skipped (already migrated or no team): {}", skippedCount.get());
            log.info("Employee assessments with errors: {}", errorCount.get());
            
        } catch (Exception e) {
            log.error("Fatal error during migration: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    private boolean migrateEmployeeAssessmentItem(Map<String, AttributeValue> item) {
        String assessmentId = item.get("id").getS();
        
        // Check if already migrated
        if (item.containsKey("teamId") && !item.containsKey("team")) {
            log.debug("EmployeeAssessment {} already migrated", assessmentId);
            return false;
        }
        
        // Check if team exists
        if (!item.containsKey("team")) {
            log.warn("EmployeeAssessment {} has no team field", assessmentId);
            return false;
        }
        
        // Extract team ID from JSON string
        AttributeValue teamAttr = item.get("team");
        String teamId;
        
        try {
            if (teamAttr.getS() != null) {
                // Team is stored as JSON string
                String teamJson = teamAttr.getS();
                log.debug("EmployeeAssessment {} has team JSON: {}", assessmentId, teamJson);
                
                JsonNode teamNode = objectMapper.readTree(teamJson);
                if (!teamNode.has("id")) {
                    log.warn("EmployeeAssessment {} has team JSON without id field", assessmentId);
                    return false;
                }
                
                teamId = teamNode.get("id").asText();
                log.debug("EmployeeAssessment {} has team ID: {}", assessmentId, teamId);
                
            } else if (teamAttr.getM() != null && teamAttr.getM().containsKey("id")) {
                // Team is stored as DynamoDB Map (fallback)
                teamId = teamAttr.getM().get("id").getS();
                log.debug("EmployeeAssessment {} has team ID from Map: {}", assessmentId, teamId);
                
            } else {
                log.warn("EmployeeAssessment {} has invalid team structure", assessmentId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error parsing team JSON for employee assessment {}: {}", assessmentId, e.getMessage());
            return false;
        }
        
        // Create update request
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(assessmentId));
        
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":teamId", new AttributeValue(teamId));
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#teamId", "teamId");
        expressionAttributeNames.put("#team", "team");
        
        UpdateItemRequest updateRequest = new UpdateItemRequest()
            .withTableName(EMPLOYEE_ASSESSMENT_TABLE_NAME)
            .withKey(key)
            .withUpdateExpression("SET #teamId = :teamId REMOVE #team")
            .withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues)
            .withReturnValues(ReturnValue.ALL_NEW);
        
        dynamoDBClient.updateItem(updateRequest);
        
        return true;
    }
    
    public static void main(String[] args) {
        log.info("=== EmployeeAssessment Data Migration Tool ===");
        
        if (args.length > 0 && "--dry-run".equals(args[0])) {
            log.info("DRY RUN MODE - No changes will be made");
            // For dry run, we could implement a preview mode
            log.warn("Dry run not implemented yet. Run without --dry-run to execute migration.");
            return;
        }
        
        log.warn("This will migrate all EmployeeAssessment records from old structure to new structure.");
        log.warn("Make sure to backup your data before proceeding!");
        log.info("Starting in 5 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Migration cancelled");
            return;
        }
        
        EmployeeAssessmentDataMigration migration = new EmployeeAssessmentDataMigration();
        migration.migrate();
    }
}