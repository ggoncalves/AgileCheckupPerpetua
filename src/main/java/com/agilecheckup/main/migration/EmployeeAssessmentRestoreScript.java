package com.agilecheckup.main.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Restoration script for EmployeeAssessment table from backup file.
 * DANGER: This will DELETE all current data and restore from backup.
 * Use only in case of migration failure or rollback scenarios.
 */
@Log4j2
public class EmployeeAssessmentRestoreScript {
    
    private static final String EMPLOYEE_ASSESSMENT_TABLE_NAME = "EmployeeAssessment";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public EmployeeAssessmentRestoreScript() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
    }
    
    public void restoreFromBackup(String backupFileName, boolean deleteExistingData) {
        log.info("Starting restoration from backup file: {}", backupFileName);
        
        if (deleteExistingData) {
            log.warn("WARNING: This will DELETE ALL existing data in the table!");
        }
        
        AtomicInteger restoredCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger deletedCount = new AtomicInteger(0);
        
        try {
            // Load and validate backup file
            Map<String, Object> backup = loadBackupFile(backupFileName);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) backup.get("records");
            Integer totalRecords = (Integer) backup.get("totalRecords");
            
            log.info("Backup contains {} records", totalRecords);
            
            if (deleteExistingData) {
                deleteExistingData(deletedCount);
            }
            
            // Restore records in batches
            restoreRecordsInBatches(records, restoredCount, errorCount);
            
            log.info("Restoration completed!");
            log.info("Records restored: {}", restoredCount.get());
            log.info("Records deleted: {}", deletedCount.get());
            log.info("Restoration errors: {}", errorCount.get());
            
            if (errorCount.get() > 0) {
                log.warn("Some records failed to restore. Check logs for details.");
            }
            
        } catch (Exception e) {
            log.error("Fatal error during restoration: {}", e.getMessage(), e);
            throw new RuntimeException("Restoration failed", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadBackupFile(String backupFileName) throws Exception {
        File backupFile = new File(backupFileName);
        if (!backupFile.exists()) {
            throw new RuntimeException("Backup file does not exist: " + backupFileName);
        }
        
        log.info("Loading backup file: {} (Size: {} bytes)", backupFile.getAbsolutePath(), backupFile.length());
        
        Map<String, Object> backup = objectMapper.readValue(backupFile, Map.class);
        
        // Validate backup structure
        if (!backup.containsKey("backupMetadata") || 
            !backup.containsKey("records") || 
            !backup.containsKey("totalRecords")) {
            throw new RuntimeException("Invalid backup file structure");
        }
        
        Map<String, Object> metadata = (Map<String, Object>) backup.get("backupMetadata");
        log.info("Backup metadata: table={}, timestamp={}, originalRecords={}", 
                metadata.get("tableName"), 
                metadata.get("backupDateTime"),
                metadata.get("itemCount"));
        
        return backup;
    }
    
    private void deleteExistingData(AtomicInteger deletedCount) {
        log.info("Deleting existing data from table...");
        
        try {
            // Scan all items to get their keys
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(EMPLOYEE_ASSESSMENT_TABLE_NAME)
                .withProjectionExpression("id"); // Only get the key attribute
            
            ScanResult result;
            do {
                result = dynamoDBClient.scan(scanRequest);
                List<Map<String, AttributeValue>> items = result.getItems();
                
                // Delete in batches
                if (!items.isEmpty()) {
                    deleteItemsBatch(items, deletedCount);
                }
                
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            log.info("Existing data deletion completed. {} items deleted", deletedCount.get());
            
        } catch (Exception e) {
            log.error("Error deleting existing data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete existing data", e);
        }
    }
    
    private void deleteItemsBatch(List<Map<String, AttributeValue>> items, AtomicInteger deletedCount) {
        List<WriteRequest> deleteRequests = new ArrayList<>();
        
        for (Map<String, AttributeValue> item : items) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", item.get("id"));
            
            DeleteRequest deleteRequest = new DeleteRequest().withKey(key);
            deleteRequests.add(new WriteRequest().withDeleteRequest(deleteRequest));
            
            // Process in batches of 25 (DynamoDB limit)
            if (deleteRequests.size() == 25) {
                processBatchDelete(deleteRequests, deletedCount);
                deleteRequests.clear();
            }
        }
        
        // Process remaining requests
        if (!deleteRequests.isEmpty()) {
            processBatchDelete(deleteRequests, deletedCount);
        }
    }
    
    private void processBatchDelete(List<WriteRequest> deleteRequests, AtomicInteger deletedCount) {
        Map<String, List<WriteRequest>> requestItems = new HashMap<>();
        requestItems.put(EMPLOYEE_ASSESSMENT_TABLE_NAME, new ArrayList<>(deleteRequests));
        
        BatchWriteItemRequest batchRequest = new BatchWriteItemRequest().withRequestItems(requestItems);
        
        try {
            BatchWriteItemResult result = dynamoDBClient.batchWriteItem(batchRequest);
            deletedCount.addAndGet(deleteRequests.size() - result.getUnprocessedItems().size());
            
            // Handle unprocessed items
            if (!result.getUnprocessedItems().isEmpty()) {
                log.warn("Some delete requests were unprocessed. Retrying...");
                // Simple retry - in production, you might want exponential backoff
                Thread.sleep(100);
                BatchWriteItemRequest retryRequest = new BatchWriteItemRequest()
                    .withRequestItems(result.getUnprocessedItems());
                dynamoDBClient.batchWriteItem(retryRequest);
            }
            
        } catch (Exception e) {
            log.error("Error in batch delete: {}", e.getMessage(), e);
            // Continue with other batches
        }
    }
    
    @SuppressWarnings("unchecked")
    private void restoreRecordsInBatches(List<Map<String, Object>> records, 
                                       AtomicInteger restoredCount, 
                                       AtomicInteger errorCount) {
        log.info("Starting restoration of {} records...", records.size());
        
        List<WriteRequest> putRequests = new ArrayList<>();
        
        for (Map<String, Object> record : records) {
            try {
                Map<String, AttributeValue> item = convertBackupFormatToAttributeValueMap(record);
                
                PutRequest putRequest = new PutRequest().withItem(item);
                putRequests.add(new WriteRequest().withPutRequest(putRequest));
                
                // Process in batches of 25 (DynamoDB limit)
                if (putRequests.size() == 25) {
                    processBatchPut(putRequests, restoredCount, errorCount);
                    putRequests.clear();
                }
                
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("Error converting record: {}", e.getMessage(), e);
            }
        }
        
        // Process remaining requests
        if (!putRequests.isEmpty()) {
            processBatchPut(putRequests, restoredCount, errorCount);
        }
    }
    
    private void processBatchPut(List<WriteRequest> putRequests, 
                               AtomicInteger restoredCount, 
                               AtomicInteger errorCount) {
        Map<String, List<WriteRequest>> requestItems = new HashMap<>();
        requestItems.put(EMPLOYEE_ASSESSMENT_TABLE_NAME, new ArrayList<>(putRequests));
        
        BatchWriteItemRequest batchRequest = new BatchWriteItemRequest().withRequestItems(requestItems);
        
        try {
            BatchWriteItemResult result = dynamoDBClient.batchWriteItem(batchRequest);
            restoredCount.addAndGet(putRequests.size() - result.getUnprocessedItems().size());
            
            // Handle unprocessed items
            if (!result.getUnprocessedItems().isEmpty()) {
                log.warn("Some put requests were unprocessed. Retrying...");
                Thread.sleep(100);
                BatchWriteItemRequest retryRequest = new BatchWriteItemRequest()
                    .withRequestItems(result.getUnprocessedItems());
                BatchWriteItemResult retryResult = dynamoDBClient.batchWriteItem(retryRequest);
                restoredCount.addAndGet(result.getUnprocessedItems().size() - retryResult.getUnprocessedItems().size());
            }
            
            if (restoredCount.get() % 100 == 0) {
                log.info("Restored {} records so far...", restoredCount.get());
            }
            
        } catch (Exception e) {
            errorCount.addAndGet(putRequests.size());
            log.error("Error in batch put: {}", e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, AttributeValue> convertBackupFormatToAttributeValueMap(Map<String, Object> record) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            item.put(key, convertBackupFormatToAttributeValue(value));
        }
        
        return item;
    }
    
    @SuppressWarnings("unchecked")
    private AttributeValue convertBackupFormatToAttributeValue(Object value) {
        if (!(value instanceof Map)) {
            throw new RuntimeException("Invalid backup format - expected Map");
        }
        
        Map<String, Object> attributeValue = (Map<String, Object>) value;
        AttributeValue av = new AttributeValue();
        
        if (attributeValue.containsKey("S")) {
            av.setS((String) attributeValue.get("S"));
        } else if (attributeValue.containsKey("N")) {
            av.setN((String) attributeValue.get("N"));
        } else if (attributeValue.containsKey("B")) {
            String base64 = (String) attributeValue.get("B");
            av.setB(ByteBuffer.wrap(Base64.getDecoder().decode(base64)));
        } else if (attributeValue.containsKey("BOOL")) {
            av.setBOOL((Boolean) attributeValue.get("BOOL"));
        } else if (attributeValue.containsKey("NULL")) {
            av.setNULL((Boolean) attributeValue.get("NULL"));
        } else if (attributeValue.containsKey("SS")) {
            av.setSS((List<String>) attributeValue.get("SS"));
        } else if (attributeValue.containsKey("NS")) {
            av.setNS((List<String>) attributeValue.get("NS"));
        } else if (attributeValue.containsKey("BS")) {
            List<String> base64List = (List<String>) attributeValue.get("BS");
            List<ByteBuffer> byteBuffers = new ArrayList<>();
            for (String base64 : base64List) {
                byteBuffers.add(ByteBuffer.wrap(Base64.getDecoder().decode(base64)));
            }
            av.setBS(byteBuffers);
        } else if (attributeValue.containsKey("L")) {
            List<Object> list = (List<Object>) attributeValue.get("L");
            List<AttributeValue> attributeValueList = new ArrayList<>();
            for (Object item : list) {
                attributeValueList.add(convertBackupFormatToAttributeValue(item));
            }
            av.setL(attributeValueList);
        } else if (attributeValue.containsKey("M")) {
            Map<String, Object> map = (Map<String, Object>) attributeValue.get("M");
            Map<String, AttributeValue> attributeValueMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                attributeValueMap.put(entry.getKey(), convertBackupFormatToAttributeValue(entry.getValue()));
            }
            av.setM(attributeValueMap);
        }
        
        return av;
    }
    
    public static void main(String[] args) {
        log.info("=== EmployeeAssessment Restoration Tool ===");
        log.error("⚠️  DANGER: THIS WILL DELETE ALL EXISTING DATA ⚠️");
        
        if (args.length < 1) {
            log.error("Usage: java EmployeeAssessmentRestoreScript <backup-file> [--delete-existing]");
            log.error("  backup-file: Path to the backup JSON file");
            log.error("  --delete-existing: (Optional) Delete existing data before restore");
            System.exit(1);
        }
        
        String backupFileName = args[0];
        boolean deleteExisting = args.length > 1 && "--delete-existing".equals(args[1]);
        
        log.warn("Backup file: {}", backupFileName);
        log.warn("Delete existing data: {}", deleteExisting);
        log.warn("");
        log.warn("This operation will:");
        if (deleteExisting) {
            log.warn("1. DELETE ALL existing EmployeeAssessment records");
        }
        log.warn("2. Restore all records from the backup file");
        log.warn("3. This action CANNOT be undone easily");
        log.warn("");
        log.warn("Make sure you understand the implications!");
        log.warn("Type 'CONFIRM' to proceed or press Ctrl+C to cancel:");
        
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine();
        
        if (!"CONFIRM".equals(confirmation)) {
            log.info("Restoration cancelled");
            return;
        }
        
        log.warn("Starting restoration in 5 seconds... Press Ctrl+C to cancel");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.info("Restoration cancelled");
            return;
        }
        
        EmployeeAssessmentRestoreScript restoreScript = new EmployeeAssessmentRestoreScript();
        restoreScript.restoreFromBackup(backupFileName, deleteExisting);
        
        log.info("=== Restoration Process Complete ===");
        log.info("Verify the restored data before proceeding with normal operations");
    }
}