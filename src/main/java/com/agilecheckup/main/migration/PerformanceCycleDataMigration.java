package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time migration script to update PerformanceCycle records from V1 structure to V2 structure.
 * 
 * Key differences between V1 and V2:
 * 1. Date fields: V1 uses java.util.Date (stored as ISO timestamp), V2 uses LocalDate (YYYY-MM-DD format)
 * 2. Field naming: V1 uses lastUpdatedDate, V2 uses lastUpdatedDate (consistent naming)
 * 3. Entity inheritance: V1 extends TenantDescribableEntity, V2 extends TenantDescribableEntityV2
 * 
 * Migration process:
 * - Convert Date fields (startDate/endDate) from ISO timestamp to LocalDate format
 * - Update field naming to match V2 conventions
 * - Ensure all required V2 fields are present
 */
@Log4j2
public class PerformanceCycleDataMigration {
    
    private static final String PERFORMANCE_CYCLE_TABLE_NAME = "PerformanceCycle";
    private final AmazonDynamoDB dynamoDBClient;
    
    public PerformanceCycleDataMigration() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
    }
    
    public void migrate() {
        log.info("Starting PerformanceCycle V1-to-V2 data migration...");
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger migratedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        try {
            // Scan all performance cycles
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(PERFORMANCE_CYCLE_TABLE_NAME);
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                for (Map<String, AttributeValue> item : items) {
                    totalCount.incrementAndGet();
                    
                    try {
                        if (migratePerformanceCycleItem(item)) {
                            migratedCount.incrementAndGet();
                            String cycleId = item.get("id").getS();
                            String cycleName = item.containsKey("name") ? item.get("name").getS() : "Unknown";
                            log.info("Migrated PerformanceCycle: {} - {}", cycleId, cycleName);
                        } else {
                            skippedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        String cycleId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
                        log.error("Error migrating PerformanceCycle {}: {}", cycleId, e.getMessage(), e);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            log.info("PerformanceCycle migration completed!");
            log.info("Total cycles processed: {}", totalCount.get());
            log.info("Cycles migrated: {}", migratedCount.get());
            log.info("Cycles skipped (already migrated or no changes needed): {}", skippedCount.get());
            log.info("Cycles with errors: {}", errorCount.get());
            
        } catch (Exception e) {
            log.error("Fatal error during PerformanceCycle migration: {}", e.getMessage(), e);
            throw new RuntimeException("PerformanceCycle migration failed", e);
        }
    }
    
    private boolean migratePerformanceCycleItem(Map<String, AttributeValue> item) {
        String cycleId = item.get("id").getS();
        
        // Check if already migrated (V2 format detection)
        if (isAlreadyMigratedToV2(item)) {
            log.debug("PerformanceCycle {} already migrated to V2 format", cycleId);
            return false;
        }
        
        // Build update expression and attribute values
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();
        StringBuilder updateExpression = new StringBuilder("SET ");
        boolean hasUpdates = false;
        
        // Convert startDate from V1 format (ISO timestamp) to V2 format (LocalDate)
        if (item.containsKey("startDate") && item.get("startDate").getS() != null) {
            String v1StartDate = item.get("startDate").getS();
            String v2StartDate = convertDateToLocalDate(v1StartDate, cycleId, "startDate");
            if (v2StartDate != null) {
                expressionAttributeValues.put(":startDate", new AttributeValue(v2StartDate));
                updateExpression.append("startDate = :startDate, ");
                hasUpdates = true;
                log.debug("Converting startDate for cycle {}: {} -> {}", cycleId, v1StartDate, v2StartDate);
            }
        }
        
        // Convert endDate from V1 format (ISO timestamp) to V2 format (LocalDate)
        if (item.containsKey("endDate") && item.get("endDate").getS() != null) {
            String v1EndDate = item.get("endDate").getS();
            String v2EndDate = convertDateToLocalDate(v1EndDate, cycleId, "endDate");
            if (v2EndDate != null) {
                expressionAttributeValues.put(":endDate", new AttributeValue(v2EndDate));
                updateExpression.append("endDate = :endDate, ");
                hasUpdates = true;
                log.debug("Converting endDate for cycle {}: {} -> {}", cycleId, v1EndDate, v2EndDate);
            }
        }
        
        // Add migration marker to indicate V2 format
        expressionAttributeValues.put(":migrationVersion", new AttributeValue("V2"));
        updateExpression.append("migrationVersion = :migrationVersion, ");
        hasUpdates = true;
        
        // Add lastUpdatedDate with current timestamp for V2 compatibility
        String currentTimestamp = Instant.now().toString();
        expressionAttributeValues.put(":lastUpdated", new AttributeValue(currentTimestamp));
        updateExpression.append("lastUpdatedDate = :lastUpdated");
        
        if (!hasUpdates) {
            log.debug("No updates needed for PerformanceCycle {}", cycleId);
            return false;
        }
        
        // Create update request
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(cycleId));
        
        UpdateItemRequest updateRequest = new UpdateItemRequest()
            .withTableName(PERFORMANCE_CYCLE_TABLE_NAME)
            .withKey(key)
            .withUpdateExpression(updateExpression.toString())
            .withExpressionAttributeValues(expressionAttributeValues)
            .withReturnValues(ReturnValue.ALL_NEW);
        
        // Add attribute names if needed
        if (!expressionAttributeNames.isEmpty()) {
            updateRequest.withExpressionAttributeNames(expressionAttributeNames);
        }
        
        dynamoDBClient.updateItem(updateRequest);
        
        return true;
    }
    
    /**
     * Check if the item is already migrated to V2 format
     */
    private boolean isAlreadyMigratedToV2(Map<String, AttributeValue> item) {
        // Check for migration marker
        if (item.containsKey("migrationVersion") && 
            "V2".equals(item.get("migrationVersion").getS())) {
            return true;
        }
        
        // Check if dates are already in LocalDate format (YYYY-MM-DD)
        if (item.containsKey("startDate")) {
            String startDate = item.get("startDate").getS();
            if (startDate != null && isLocalDateFormat(startDate)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Convert V1 Date format (ISO timestamp) to V2 LocalDate format (YYYY-MM-DD)
     */
    private String convertDateToLocalDate(String isoTimestamp, String cycleId, String fieldName) {
        if (isoTimestamp == null || isoTimestamp.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Handle various ISO timestamp formats
            Instant instant;
            
            if (isoTimestamp.contains("T")) {
                // ISO timestamp format: 2025-07-28T18:42:22.416Z
                instant = Instant.parse(isoTimestamp);
            } else {
                // Try parsing as epoch milliseconds (fallback)
                long epochMilli = Long.parseLong(isoTimestamp);
                instant = Instant.ofEpochMilli(epochMilli);
            }
            
            // Convert to LocalDate (using system default timezone)
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            
            // Format as YYYY-MM-DD
            return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
        } catch (DateTimeParseException | NumberFormatException e) {
            log.warn("Failed to convert {} for cycle {}: {} - {}", fieldName, cycleId, isoTimestamp, e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if the string is already in LocalDate format (YYYY-MM-DD)
     */
    private boolean isLocalDateFormat(String dateString) {
        if (dateString == null) {
            return false;
        }
        
        try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public static void main(String[] args) {
        log.info("=== PerformanceCycle V1-to-V2 Data Migration Tool ===");
        
        if (args.length > 0 && "--dry-run".equals(args[0])) {
            log.info("DRY RUN MODE - No changes will be made");
            log.warn("Dry run not implemented yet. Run without --dry-run to execute migration.");
            return;
        }
        
        log.warn("This will migrate all PerformanceCycle records from V1 to V2 format.");
        log.warn("Key changes:");
        log.warn("  - Date fields: ISO timestamp → LocalDate (YYYY-MM-DD)");
        log.warn("  - Adds migrationVersion marker for V2 compatibility");
        log.warn("  - Updates lastUpdatedDate timestamp");
        log.warn("");
        log.warn("Make sure to backup your data before proceeding!");
        log.info("Starting in 5 seconds... Press Ctrl+C to cancel");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Migration cancelled");
            return;
        }
        
        PerformanceCycleDataMigration migration = new PerformanceCycleDataMigration();
        migration.migrate();
    }
}