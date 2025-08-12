package com.agilecheckup.main.migration;

import lombok.extern.log4j.Log4j2;

/**
 * Central migration runner to execute various data migrations.
 * 
 * Usage:
 * mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.MigrationRunner"
 * -Dexec.args="AssessmentMatrixPillarV2Migration"
 */
@Log4j2
public class MigrationRunner {

  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String migrationName = args[0];
    String[] migrationArgs = new String[args.length - 1];
    System.arraycopy(args, 1, migrationArgs, 0, migrationArgs.length);

    log.info("=== Migration Runner ===");
    log.info("Executing migration: {}", migrationName);

    try {
      switch (migrationName) {
        case "AssessmentMatrixPillarV2Migration":
          AssessmentMatrixPillarV2Migration.main(migrationArgs);
          break;

        // Migration classes removed during V1 cleanup
        case "EmployeeAssessmentDataMigration":
        case "TeamDataMigration":
        case "EmployeeAssessmentStatusMigration":
        case "PerformanceCycleDataMigration":
          log.warn("Migration {} is no longer available after V1 cleanup", migrationName);
          break;

        default:
          log.error("Unknown migration: {}", migrationName);
          printUsage();
          System.exit(1);
      }
    }
    catch (Exception e) {
      log.error("Migration failed: {}", e.getMessage(), e);
      System.exit(1);
    }

    log.info("Migration completed successfully!");
  }

  private static void printUsage() {
    log.info("Usage: MigrationRunner <migration-name> [migration-args]");
    log.info("");
    log.info("Available migrations:");
    log.info("  AssessmentMatrixPillarV2Migration  - Migrate AssessmentMatrix pillarMap from V1 Map to V2 JSON format");
    log.info("  EmployeeAssessmentDataMigration    - Migrate EmployeeAssessment from embedded team to teamId");
    log.info("  TeamDataMigration                  - Migrate Team data structure");
    log.info("  EmployeeAssessmentStatusMigration  - Migrate EmployeeAssessment status fields");
    log.info("  PerformanceCycleDataMigration      - Migrate PerformanceCycle from V1 Date fields to V2 LocalDate");
    log.info("");
    log.info("Example:");
    log.info("  mvn exec:java -Dexec.mainClass=\"com.agilecheckup.main.migration.MigrationRunner\" -Dexec.args=\"AssessmentMatrixPillarV2Migration\"");
  }
}