package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time migration script to add AssessmentStatus field to existing EmployeeAssessment records.
 * Sets all existing records to INVITED status (default value).
 */
@Log4j2
public class EmployeeAssessmentStatusMigration {
    
    private static final String EMPLOYEE_ASSESSMENT_TABLE_NAME = "EmployeeAssessment";
    private static final String DEFAULT_ASSESSMENT_STATUS = "INVITED";
    private final AmazonDynamoDB dynamoDBClient;
    
    public EmployeeAssessmentStatusMigration() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
    }
    
    public void migrate() {
        log.info("Starting EmployeeAssessment AssessmentStatus migration...");
        
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
            
            log.info("AssessmentStatus migration completed!");
            log.info("Total employee assessments processed: {}", totalCount.get());
            log.info("Employee assessments migrated: {}", migratedCount.get());
            log.info("Employee assessments skipped (already have status): {}", skippedCount.get());
            log.info("Employee assessments with errors: {}", errorCount.get());
            
        } catch (Exception e) {
            log.error("Fatal error during AssessmentStatus migration: {}", e.getMessage(), e);
            throw new RuntimeException("AssessmentStatus migration failed", e);
        }
    }
    
    private boolean migrateEmployeeAssessmentItem(Map<String, AttributeValue> item) {
        String assessmentId = item.get("id").getS();
        
        // Check if already has assessmentStatus
        if (item.containsKey("assessmentStatus")) {
            log.debug("EmployeeAssessment {} already has assessmentStatus", assessmentId);
            return false;
        }
        
        // Create update request to add assessmentStatus
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(assessmentId));
        
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":status", new AttributeValue(DEFAULT_ASSESSMENT_STATUS));
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#assessmentStatus", "assessmentStatus");
        
        UpdateItemRequest updateRequest = new UpdateItemRequest()
            .withTableName(EMPLOYEE_ASSESSMENT_TABLE_NAME)
            .withKey(key)
            .withUpdateExpression("SET #assessmentStatus = :status")
            .withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues)
            .withReturnValues(ReturnValue.ALL_NEW);
        
        UpdateItemResult updateResult = dynamoDBClient.updateItem(updateRequest);
        log.debug("Updated EmployeeAssessment {} with assessmentStatus: {}", assessmentId, DEFAULT_ASSESSMENT_STATUS);
        
        return true;
    }
    
    public static void main(String[] args) {
        log.info("=== EmployeeAssessment AssessmentStatus Migration Tool ===");
        
        if (args.length > 0 && "--dry-run".equals(args[0])) {
            log.info("DRY RUN MODE - No changes will be made");
            log.warn("Dry run not implemented yet. Run without --dry-run to execute migration.");
            return;
        }
        
        log.warn("This will add AssessmentStatus field to all EmployeeAssessment records without this field.");
        log.warn("All existing records will be set to INVITED status.");
        log.warn("Make sure to backup your data before proceeding!");
        log.info("Starting in 5 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Migration cancelled");
            return;
        }
        
        EmployeeAssessmentStatusMigration migration = new EmployeeAssessmentStatusMigration();
        migration.migrate();
    }
}