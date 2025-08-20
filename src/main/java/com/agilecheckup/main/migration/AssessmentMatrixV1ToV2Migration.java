package com.agilecheckup.main.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Migration script to convert AssessmentMatrix records from V1 to V2 format.
 * This handles the complete entity migration including:
 * - Converting V1 entity structure to V2
 * - Maintaining all existing data
 * - Handling potential null values safely
 * - Preserving relationships and references
 */
@Log4j2
public class AssessmentMatrixV1ToV2Migration {

  private static final String ASSESSMENT_MATRIX_TABLE_NAME = "AssessmentMatrix";
  private final AmazonDynamoDB dynamoDBV1Client;
  private final DynamoDbClient dynamoDBV2Client;
  private final ObjectMapper objectMapper;

  public AssessmentMatrixV1ToV2Migration() {
    this.dynamoDBV1Client = AmazonDynamoDBClientBuilder.standard().build();
    this.dynamoDBV2Client = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    this.objectMapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                          .setDateFormat(new StdDateFormat());
  }

  public void migrate(boolean dryRun) {
    log.info("Starting AssessmentMatrix V1 to V2 migration...");
    log.info("Mode: {}", dryRun ? "DRY RUN" : "LIVE MIGRATION");

    AtomicInteger totalCount = new AtomicInteger(0);
    AtomicInteger migratedCount = new AtomicInteger(0);
    AtomicInteger skippedCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);

    try {
      // Scan all assessment matrices
      ScanRequest scanRequest = new ScanRequest().withTableName(ASSESSMENT_MATRIX_TABLE_NAME);

      ScanResult result;
      do {
        result = dynamoDBV1Client.scan(scanRequest);
        List<Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue>> items = result.getItems();

        log.info("Processing {} records in this batch", items.size());

        for (Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> item : items) {
          totalCount.incrementAndGet();

          try {
            if (migrateAssessmentMatrixItem(item, dryRun)) {
              migratedCount.incrementAndGet();
              String matrixId = item.get("id").getS();
              if (dryRun) {
                log.info("[DRY RUN] Would migrate AssessmentMatrix: {}", matrixId);
              }
              else {
                log.info("Migrated AssessmentMatrix: {}", matrixId);
              }
            }
            else {
              skippedCount.incrementAndGet();
            }
          }
          catch (Exception e) {
            errorCount.incrementAndGet();
            String matrixId = item.containsKey("id") ? item.get("id").getS() : "Unknown";
            log.error("Error migrating AssessmentMatrix {}: {}", matrixId, e.getMessage(), e);
          }
        }

        // Set up for next page
        scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());

      } while (result.getLastEvaluatedKey() != null);

      log.info("Migration completed!");
      log.info("Total assessment matrices processed: {}", totalCount.get());
      log.info("Assessment matrices migrated: {}", migratedCount.get());
      log.info("Assessment matrices skipped (already migrated): {}", skippedCount.get());
      log.info("Assessment matrices with errors: {}", errorCount.get());

      if (dryRun) {
        log.info("DRY RUN completed - no changes were made to the database");
      }

    }
    catch (Exception e) {
      log.error("Fatal error during migration: {}", e.getMessage(), e);
      throw new RuntimeException("Migration failed", e);
    }
  }

  /**
   * Migrates a single AssessmentMatrix item from V1 to V2 format
   */
  private boolean migrateAssessmentMatrixItem(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> item, boolean dryRun) {
    String matrixId = item.get("id").getS();

    // Check if this record needs migration by examining the structure
    if (isAlreadyV2Format(item)) {
      log.debug("AssessmentMatrix {} already in V2 format, skipping", matrixId);
      return false;
    }

    if (dryRun) {
      return true; // Would migrate
    }

    try {
      // Convert V1 DynamoDB item to V2 format using SDK V2
      Map<String, AttributeValue> v2Item = convertV1ItemToV2(item);

      // Use V2 SDK to put the item
      software.amazon.awssdk.services.dynamodb.model.PutItemRequest putRequest = software.amazon.awssdk.services.dynamodb.model.PutItemRequest.builder()
                                                                                                                                              .tableName(ASSESSMENT_MATRIX_TABLE_NAME)
                                                                                                                                              .item(v2Item)
                                                                                                                                              .build();

      dynamoDBV2Client.putItem(putRequest);

      log.debug("Successfully migrated AssessmentMatrix {}", matrixId);
      return true;

    }
    catch (Exception e) {
      log.error("Error converting AssessmentMatrix {} to V2 format: {}", matrixId, e.getMessage());
      throw new RuntimeException("Failed to migrate matrix " + matrixId, e);
    }
  }

  /**
   * Checks if the item is already in V2 format (using enhanced client structure)
   */
  private boolean isAlreadyV2Format(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> item) {
    // Check for V2 characteristics - this is a simplified check
    // In a real scenario, you might check for specific V2 attributes or metadata
    return false; // For now, assume all need migration
  }

  /**
   * Converts V1 DynamoDB item to V2 format
   */
  private Map<String, AttributeValue> convertV1ItemToV2(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> v1Item) {
    Map<String, AttributeValue> v2Item = new HashMap<>();

    // Convert basic attributes
    for (Map.Entry<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> entry : v1Item.entrySet()) {
      String attributeName = entry.getKey();
      com.amazonaws.services.dynamodbv2.model.AttributeValue v1Value = entry.getValue();

      // Convert V1 AttributeValue to V2 AttributeValue
      AttributeValue v2Value = convertV1AttributeToV2(v1Value);
      v2Item.put(attributeName, v2Value);
    }

    return v2Item;
  }

  /**
   * Converts a V1 SDK AttributeValue to V2 SDK AttributeValue
   */
  private AttributeValue convertV1AttributeToV2(com.amazonaws.services.dynamodbv2.model.AttributeValue v1Attr) {
    AttributeValue.Builder v2Builder = AttributeValue.builder();

    if (v1Attr.getS() != null) {
      v2Builder.s(v1Attr.getS());
    }
    else if (v1Attr.getN() != null) {
      v2Builder.n(v1Attr.getN());
    }
    else if (v1Attr.getB() != null) {
      v2Builder.b(software.amazon.awssdk.core.SdkBytes.fromByteBuffer(v1Attr.getB()));
    }
    else if (v1Attr.getSS() != null) {
      v2Builder.ss(v1Attr.getSS());
    }
    else if (v1Attr.getNS() != null) {
      v2Builder.ns(v1Attr.getNS());
    }
    else if (v1Attr.getBS() != null) {
      List<software.amazon.awssdk.core.SdkBytes> v2BS = v1Attr.getBS()
                                                              .stream()
                                                              .map(software.amazon.awssdk.core.SdkBytes::fromByteBuffer)
                                                              .collect(java.util.stream.Collectors.toList());
      v2Builder.bs(v2BS);
    }
    else if (v1Attr.getM() != null) {
      Map<String, AttributeValue> v2Map = new HashMap<>();
      for (Map.Entry<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> mapEntry : v1Attr.getM()
                                                                                                      .entrySet()) {
        v2Map.put(mapEntry.getKey(), convertV1AttributeToV2(mapEntry.getValue()));
      }
      v2Builder.m(v2Map);
    }
    else if (v1Attr.getL() != null) {
      List<AttributeValue> v2List = v1Attr.getL()
                                          .stream()
                                          .map(this::convertV1AttributeToV2)
                                          .collect(java.util.stream.Collectors.toList());
      v2Builder.l(v2List);
    }
    else if (v1Attr.getNULL() != null && v1Attr.getNULL()) {
      v2Builder.nul(true);
    }
    else if (v1Attr.getBOOL() != null) {
      v2Builder.bool(v1Attr.getBOOL());
    }

    return v2Builder.build();
  }

  /**
   * Optional: Clean up - this migration simply re-writes data in V2 format
   * Since we're using the same table, no cleanup is needed
   */
  public void verifyMigration() {
    log.info("Migration verification would go here in a production system");
    log.info("For AssessmentMatrix, the migration re-writes existing records with V2 SDK compatibility");
  }

  public static void main(String[] args) {
    log.info("=== AssessmentMatrix V1 to V2 Migration Tool ===");

    boolean dryRun = false;

    // Parse command line arguments
    for (String arg : args) {
      if ("--dry-run".equals(arg)) {
        dryRun = true;
      }
    }

    if (!dryRun) {
      log.warn("This will re-write all AssessmentMatrix records using V2 SDK format.");
      log.warn("This ensures compatibility with V2 services and eliminates deserialization errors.");
      log.warn("Make sure to backup your data before proceeding!");
      log.info("Starting in 5 seconds... Press Ctrl+C to cancel");

      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
        log.info("Migration cancelled");
        return;
      }
    }

    AssessmentMatrixV1ToV2Migration migration = new AssessmentMatrixV1ToV2Migration();

    // Run migration
    migration.migrate(dryRun);

    log.info("Migration completed successfully!");
  }
}