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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Backup script for EmployeeAssessment table before running AssessmentStatus migration.
 * Creates a JSON backup file with all records and metadata for potential restoration.
 */
@Log4j2
public class EmployeeAssessmentBackupScript {
    
    private static final String EMPLOYEE_ASSESSMENT_TABLE_NAME = "EmployeeAssessment";
    private final AmazonDynamoDB dynamoDBClient;
    private final ObjectMapper objectMapper;
    
    public EmployeeAssessmentBackupScript() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }
    
    public String createBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = String.format("employeeassessment_backup_%s.json", timestamp);
        
        log.info("Starting EmployeeAssessment backup to file: {}", backupFileName);
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Map<String, Object>> backupData = new ArrayList<>();
        
        try {
            // Get table description for metadata
            DescribeTableRequest describeRequest = new DescribeTableRequest()
                .withTableName(EMPLOYEE_ASSESSMENT_TABLE_NAME);
            DescribeTableResult tableDescription = dynamoDBClient.describeTable(describeRequest);
            
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
                        Map<String, Object> backupItem = convertAttributeValueMapToBackupFormat(item);
                        backupData.add(backupItem);
                        
                        if (totalCount.get() % 100 == 0) {
                            log.info("Backed up {} records...", totalCount.get());
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        String assessmentId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
                        log.error("Error backing up employee assessment {}: {}", assessmentId, e.getMessage(), e);
                    }
                }
                
                // Set up for next page
                scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
                
            } while (result.getLastEvaluatedKey() != null);
            
            // Create backup file structure
            Map<String, Object> backup = new HashMap<>();
            backup.put("backupMetadata", createBackupMetadata(timestamp, tableDescription.getTable()));
            backup.put("records", backupData);
            backup.put("totalRecords", totalCount.get());
            backup.put("errors", errorCount.get());
            
            // Write backup to file
            writeBackupToFile(backupFileName, backup);
            
            log.info("Backup completed successfully!");
            log.info("Backup file: {}", backupFileName);
            log.info("Total records backed up: {}", totalCount.get());
            log.info("Records with errors: {}", errorCount.get());
            
            return backupFileName;
            
        } catch (Exception e) {
            log.error("Fatal error during backup: {}", e.getMessage(), e);
            throw new RuntimeException("Backup failed", e);
        }
    }
    
    private Map<String, Object> createBackupMetadata(String timestamp, TableDescription tableDesc) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("backupTimestamp", timestamp);
        metadata.put("backupDateTime", LocalDateTime.now().toString());
        metadata.put("tableName", tableDesc.getTableName());
        metadata.put("tableStatus", tableDesc.getTableStatus());
        metadata.put("itemCount", tableDesc.getItemCount());
        metadata.put("tableArn", tableDesc.getTableArn());
        metadata.put("creationDateTime", tableDesc.getCreationDateTime());
        
        // Add key schema information
        List<Map<String, Object>> keySchema = new ArrayList<>();
        for (KeySchemaElement element : tableDesc.getKeySchema()) {
            Map<String, Object> key = new HashMap<>();
            key.put("attributeName", element.getAttributeName());
            key.put("keyType", element.getKeyType());
            keySchema.add(key);
        }
        metadata.put("keySchema", keySchema);
        
        // Add attribute definitions
        List<Map<String, Object>> attributeDefinitions = new ArrayList<>();
        for (AttributeDefinition attr : tableDesc.getAttributeDefinitions()) {
            Map<String, Object> definition = new HashMap<>();
            definition.put("attributeName", attr.getAttributeName());
            definition.put("attributeType", attr.getAttributeType());
            attributeDefinitions.add(definition);
        }
        metadata.put("attributeDefinitions", attributeDefinitions);
        
        return metadata;
    }
    
    private Map<String, Object> convertAttributeValueMapToBackupFormat(Map<String, AttributeValue> item) {
        Map<String, Object> backupItem = new HashMap<>();
        
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            
            backupItem.put(key, convertAttributeValueToBackupFormat(value));
        }
        
        return backupItem;
    }
    
    private Object convertAttributeValueToBackupFormat(AttributeValue value) {
        Map<String, Object> attributeValue = new HashMap<>();
        
        if (value.getS() != null) {
            attributeValue.put("S", value.getS());
        } else if (value.getN() != null) {
            attributeValue.put("N", value.getN());
        } else if (value.getB() != null) {
            attributeValue.put("B", Base64.getEncoder().encodeToString(value.getB().array()));
        } else if (value.getBOOL() != null) {
            attributeValue.put("BOOL", value.getBOOL());
        } else if (value.getNULL() != null) {
            attributeValue.put("NULL", value.getNULL());
        } else if (value.getSS() != null) {
            attributeValue.put("SS", value.getSS());
        } else if (value.getNS() != null) {
            attributeValue.put("NS", value.getNS());
        } else if (value.getBS() != null) {
            List<String> base64List = new ArrayList<>();
            for (java.nio.ByteBuffer bb : value.getBS()) {
                base64List.add(Base64.getEncoder().encodeToString(bb.array()));
            }
            attributeValue.put("BS", base64List);
        } else if (value.getL() != null) {
            List<Object> list = new ArrayList<>();
            for (AttributeValue av : value.getL()) {
                list.add(convertAttributeValueToBackupFormat(av));
            }
            attributeValue.put("L", list);
        } else if (value.getM() != null) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, AttributeValue> entry : value.getM().entrySet()) {
                map.put(entry.getKey(), convertAttributeValueToBackupFormat(entry.getValue()));
            }
            attributeValue.put("M", map);
        }
        
        return attributeValue;
    }
    
    private void writeBackupToFile(String fileName, Map<String, Object> backup) throws IOException {
        File file = new File(fileName);
        
        try (FileWriter writer = new FileWriter(file)) {
            objectMapper.writeValue(writer, backup);
        }
        
        log.info("Backup written to file: {} (Size: {} bytes)", file.getAbsolutePath(), file.length());
    }
    
    public void validateBackup(String backupFileName) {
        log.info("Validating backup file: {}", backupFileName);
        
        try {
            File backupFile = new File(backupFileName);
            if (!backupFile.exists()) {
                throw new RuntimeException("Backup file does not exist: " + backupFileName);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> backup = objectMapper.readValue(backupFile, Map.class);
            
            // Validate structure
            if (!backup.containsKey("backupMetadata") || 
                !backup.containsKey("records") || 
                !backup.containsKey("totalRecords")) {
                throw new RuntimeException("Invalid backup file structure");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) backup.get("records");
            Integer totalRecords = (Integer) backup.get("totalRecords");
            
            if (records.size() != totalRecords) {
                log.warn("Record count mismatch: expected {}, found {}", totalRecords, records.size());
            }
            
            // Validate a few sample records
            int sampleSize = Math.min(5, records.size());
            for (int i = 0; i < sampleSize; i++) {
                Map<String, Object> record = records.get(i);
                if (!record.containsKey("id")) {
                    throw new RuntimeException("Invalid record structure - missing 'id' field");
                }
            }
            
            log.info("Backup validation successful!");
            log.info("Total records in backup: {}", totalRecords);
            log.info("File size: {} bytes", backupFile.length());
            
        } catch (Exception e) {
            log.error("Backup validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Backup validation failed", e);
        }
    }
    
    public static void main(String[] args) {
        log.info("=== EmployeeAssessment Backup Tool ===");
        
        boolean validateOnly = args.length > 0 && "--validate".equals(args[0]);
        String fileName = args.length > 1 ? args[1] : null;
        
        EmployeeAssessmentBackupScript backupScript = new EmployeeAssessmentBackupScript();
        
        if (validateOnly) {
            if (fileName == null) {
                log.error("Validation requires a backup file name. Usage: --validate <backup-file>");
                System.exit(1);
            }
            backupScript.validateBackup(fileName);
        } else {
            log.warn("This will create a complete backup of the EmployeeAssessment table.");
            log.warn("Ensure you have sufficient disk space and proper AWS permissions.");
            log.info("Starting backup in 3 seconds... Press Ctrl+C to cancel");
            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.info("Backup cancelled");
                return;
            }
            
            String backupFileName = backupScript.createBackup();
            
            // Auto-validate the created backup
            log.info("Validating created backup...");
            backupScript.validateBackup(backupFileName);
            
            log.info("=== Backup Process Complete ===");
            log.info("Backup file: {}", backupFileName);
            log.info("Next steps:");
            log.info("1. Verify the backup file exists and has reasonable size");
            log.info("2. Run the migration: EmployeeAssessmentStatusMigration");
            log.info("3. Keep this backup file for potential restoration");
        }
    }
}