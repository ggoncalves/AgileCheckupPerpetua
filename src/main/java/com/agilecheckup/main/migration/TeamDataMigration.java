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
 * One-time migration script to update Team records from old structure (embedded department object)
 * to new structure (departmentId string).
 * 
 * Old structure:
 * {
 *   "id": "...",
 *   "department": {
 *     "id": "...",
 *     "name": "...",
 *     ...
 *   }
 * }
 * 
 * New structure:
 * {
 *   "id": "...",
 *   "departmentId": "..."
 * }
 */
@Log4j2
public class TeamDataMigration {
    
    private static final String TEAM_TABLE_NAME = "Team";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public TeamDataMigration() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
    }
    
    public void migrate() {
        log.info("Starting Team data migration...");
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger migratedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        try {
            // Scan all teams
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(TEAM_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                for (Map<String, AttributeValue> item : items) {
                    totalCount.incrementAndGet();
                    
                    try {
                        if (migrateTeamItem(item)) {
                            migratedCount.incrementAndGet();
                            String teamId = item.get("id").getS();
                            String teamName = item.containsKey("name") ? item.get("name").getS() : "Unknown";
                            log.info("Migrated team: {} - {}", teamId, teamName);
                        } else {
                            skippedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        String teamId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
                        log.error("Error migrating team {}: {}", teamId, e.getMessage(), e);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            log.info("Migration completed!");
            log.info("Total teams processed: {}", totalCount.get());
            log.info("Teams migrated: {}", migratedCount.get());
            log.info("Teams skipped (already migrated): {}", skippedCount.get());
            log.info("Teams with errors: {}", errorCount.get());
            
        } catch (Exception e) {
            log.error("Fatal error during migration: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    private boolean migrateTeamItem(Map<String, AttributeValue> item) {
        String teamId = item.get("id").getS();
        
        // Check if already migrated
        if (item.containsKey("departmentId") && !item.containsKey("department")) {
            log.debug("Team {} already migrated", teamId);
            return false;
        }
        
        // Check if department exists
        if (!item.containsKey("department")) {
            log.warn("Team {} has no department field", teamId);
            return false;
        }
        
        // Extract department ID from JSON string
        AttributeValue departmentAttr = item.get("department");
        String departmentId;
        
        try {
            if (departmentAttr.getS() != null) {
                // Department is stored as JSON string
                String departmentJson = departmentAttr.getS();
                log.debug("Team {} has department JSON: {}", teamId, departmentJson);
                
                JsonNode departmentNode = objectMapper.readTree(departmentJson);
                if (!departmentNode.has("id")) {
                    log.warn("Team {} has department JSON without id field", teamId);
                    return false;
                }
                
                departmentId = departmentNode.get("id").asText();
                log.debug("Team {} has department ID: {}", teamId, departmentId);
                
            } else if (departmentAttr.getM() != null && departmentAttr.getM().containsKey("id")) {
                // Department is stored as DynamoDB Map (fallback)
                departmentId = departmentAttr.getM().get("id").getS();
                log.debug("Team {} has department ID from Map: {}", teamId, departmentId);
                
            } else {
                log.warn("Team {} has invalid department structure", teamId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error parsing department JSON for team {}: {}", teamId, e.getMessage());
            return false;
        }
        
        // Create update request
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(teamId));
        
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":deptId", new AttributeValue(departmentId));
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#deptId", "departmentId");
        expressionAttributeNames.put("#dept", "department");
        
        UpdateItemRequest updateRequest = new UpdateItemRequest()
            .withTableName(TEAM_TABLE_NAME)
            .withKey(key)
            .withUpdateExpression("SET #deptId = :deptId REMOVE #dept")
            .withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues)
            .withReturnValues(ReturnValue.ALL_NEW);
        
        dynamoDBClient.updateItem(updateRequest);
        
        return true;
    }
    
    public static void main(String[] args) {
        log.info("=== Team Data Migration Tool ===");
        
        if (args.length > 0 && "--dry-run".equals(args[0])) {
            log.info("DRY RUN MODE - No changes will be made");
            // For dry run, we could implement a preview mode
            log.warn("Dry run not implemented yet. Run without --dry-run to execute migration.");
            return;
        }
        
        log.warn("This will migrate all Team records from old structure to new structure.");
        log.warn("Make sure to backup your data before proceeding!");
        log.info("Starting in 5 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Migration cancelled");
            return;
        }
        
        TeamDataMigration migration = new TeamDataMigration();
        migration.migrate();
    }
}