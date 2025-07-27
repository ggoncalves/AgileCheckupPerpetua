package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time migration script to update AssessmentMatrix records from old pillarMap structure (DynamoDB Map)
 * to new pillarMap structure (JSON String for V2 compatibility).
 * 
 * Old structure (DynamoDB Map):
 * {
 *   "pillarMap": {
 *     "M": {
 *       "pillar-id": {
 *         "M": {
 *           "name": {"S": "Pillar Name"},
 *           "description": {"S": "Description"},
 *           "categoryMap": {
 *             "M": {
 *               "category-id": {
 *                 "M": {
 *                   "name": {"S": "Category Name"},
 *                   "description": {"S": "Category Description"}
 *                 }
 *               }
 *             }
 *           }
 *         }
 *       }
 *     }
 *   }
 * }
 * 
 * New structure (JSON String):
 * {
 *   "pillarMap": {
 *     "S": "{\"pillar-id\":{\"id\":\"pillar-id\",\"name\":\"Pillar Name\",\"description\":\"Description\",\"categoryMap\":{\"category-id\":{\"id\":\"category-id\",\"name\":\"Category Name\",\"description\":\"Category Description\",\"createdDate\":null,\"lastUpdatedDate\":null}},\"createdDate\":null,\"lastUpdatedDate\":null}}"
 *   }
 * }
 */
@Log4j2
public class AssessmentMatrixPillarV2Migration {
    
    private static final String ASSESSMENT_MATRIX_TABLE_NAME = "AssessmentMatrix";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public AssessmentMatrixPillarV2Migration() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat());
    }
    
    public void migrate() {
        log.info("Starting AssessmentMatrix PillarMap V2 migration...");
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger migratedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        try {
            // Scan all assessment matrices
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(ASSESSMENT_MATRIX_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                for (Map<String, AttributeValue> item : items) {
                    totalCount.incrementAndGet();
                    
                    try {
                        if (migrateAssessmentMatrixItem(item)) {
                            migratedCount.incrementAndGet();
                            String matrixId = item.get("id").getS();
                            log.info("Migrated assessment matrix: {}", matrixId);
                        } else {
                            skippedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        String matrixId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
                        log.error("Error migrating assessment matrix {}: {}", matrixId, e.getMessage(), e);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            log.info("Migration completed!");
            log.info("Total assessment matrices processed: {}", totalCount.get());
            log.info("Assessment matrices migrated: {}", migratedCount.get());
            log.info("Assessment matrices skipped (already migrated or no pillarMap): {}", skippedCount.get());
            log.info("Assessment matrices with errors: {}", errorCount.get());
            
        } catch (Exception e) {
            log.error("Fatal error during migration: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    private boolean migrateAssessmentMatrixItem(Map<String, AttributeValue> item) {
        String matrixId = item.get("id").getS();
        
        // Check if pillarMap exists
        if (!item.containsKey("pillarMap")) {
            log.debug("AssessmentMatrix {} has no pillarMap field", matrixId);
            return false;
        }
        
        AttributeValue pillarMapAttr = item.get("pillarMap");
        
        // Check if already migrated (stored as String)
        if (pillarMapAttr.getS() != null) {
            log.debug("AssessmentMatrix {} already migrated to V2 format", matrixId);
            return false;
        }
        
        // Check if it's the old Map format
        if (pillarMapAttr.getM() == null) {
            log.warn("AssessmentMatrix {} has invalid pillarMap structure", matrixId);
            return false;
        }
        
        try {
            // Convert DynamoDB Map to V2 JSON structure
            Map<String, Object> v2PillarMap = convertDynamoDBMapToV2Structure(pillarMapAttr.getM());
            
            // Serialize to JSON string
            String pillarMapJson = objectMapper.writeValueAsString(v2PillarMap);
            
            // Create update request
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue(matrixId));
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":pillarMapJson", new AttributeValue(pillarMapJson));
            
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#pillarMap", "pillarMap");
            
            UpdateItemRequest updateRequest = new UpdateItemRequest()
                .withTableName(ASSESSMENT_MATRIX_TABLE_NAME)
                .withKey(key)
                .withUpdateExpression("SET #pillarMap = :pillarMapJson")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues)
                .withReturnValues(ReturnValue.ALL_NEW);
            
            dynamoDBClient.updateItem(updateRequest);
            
            log.debug("Successfully migrated pillarMap for matrix {}", matrixId);
            return true;
            
        } catch (Exception e) {
            log.error("Error converting pillarMap for matrix {}: {}", matrixId, e.getMessage());
            throw new RuntimeException("Failed to migrate matrix " + matrixId, e);
        }
    }
    
    /**
     * Converts DynamoDB Map structure to V2 JSON structure
     */
    private Map<String, Object> convertDynamoDBMapToV2Structure(Map<String, AttributeValue> dynamoDBMap) {
        Map<String, Object> v2PillarMap = new HashMap<>();
        
        for (Map.Entry<String, AttributeValue> pillarEntry : dynamoDBMap.entrySet()) {
            String pillarId = pillarEntry.getKey();
            Map<String, AttributeValue> pillarData = pillarEntry.getValue().getM();
            
            if (pillarData == null) {
                log.warn("Pillar {} has null data, skipping", pillarId);
                continue;
            }
            
            // Create V2 pillar structure
            Map<String, Object> v2Pillar = new HashMap<>();
            v2Pillar.put("id", pillarId);
            v2Pillar.put("name", pillarData.containsKey("name") ? pillarData.get("name").getS() : "");
            v2Pillar.put("description", pillarData.containsKey("description") ? pillarData.get("description").getS() : "");
            v2Pillar.put("createdDate", null);
            v2Pillar.put("lastUpdatedDate", null);
            
            // Convert categoryMap
            Map<String, Object> v2CategoryMap = new HashMap<>();
            if (pillarData.containsKey("categoryMap") && pillarData.get("categoryMap").getM() != null) {
                Map<String, AttributeValue> categoryMapData = pillarData.get("categoryMap").getM();
                
                for (Map.Entry<String, AttributeValue> categoryEntry : categoryMapData.entrySet()) {
                    String categoryId = categoryEntry.getKey();
                    Map<String, AttributeValue> categoryData = categoryEntry.getValue().getM();
                    
                    if (categoryData == null) {
                        log.warn("Category {} has null data, skipping", categoryId);
                        continue;
                    }
                    
                    // Create V2 category structure
                    Map<String, Object> v2Category = new HashMap<>();
                    v2Category.put("id", categoryId);
                    v2Category.put("name", categoryData.containsKey("name") ? categoryData.get("name").getS() : "");
                    v2Category.put("description", categoryData.containsKey("description") ? categoryData.get("description").getS() : "");
                    v2Category.put("createdDate", null);
                    v2Category.put("lastUpdatedDate", null);
                    
                    v2CategoryMap.put(categoryId, v2Category);
                }
            }
            
            v2Pillar.put("categoryMap", v2CategoryMap);
            v2PillarMap.put(pillarId, v2Pillar);
        }
        
        return v2PillarMap;
    }
    
    public static void main(String[] args) {
        log.info("=== AssessmentMatrix PillarMap V2 Migration Tool ===");
        
        if (args.length > 0 && "--dry-run".equals(args[0])) {
            log.info("DRY RUN MODE - No changes will be made");
            log.warn("Dry run not implemented yet. Run without --dry-run to execute migration.");
            return;
        }
        
        log.warn("This will migrate all AssessmentMatrix pillarMap from old Map structure to new V2 JSON structure.");
        log.warn("This migration is required for V2 PillarV2/CategoryV2 compatibility.");
        log.warn("Make sure to backup your data before proceeding!");
        log.info("Starting in 5 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Migration cancelled");
            return;
        }
        
        AssessmentMatrixPillarV2Migration migration = new AssessmentMatrixPillarV2Migration();
        migration.migrate();
    }
}