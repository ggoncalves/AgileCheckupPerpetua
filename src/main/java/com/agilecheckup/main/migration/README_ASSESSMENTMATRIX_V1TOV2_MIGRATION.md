# AssessmentMatrix V1 to V2 Migration

## Overview

This migration tool converts AssessmentMatrix records from AWS SDK V1 format to V2 format. The migration preserves all existing data while updating the entity structure to use V2 SDK features.

## Why This Migration?

1. **SDK Version Compatibility**: V1 entities cannot properly deserialize V2 records with fields like `potentialScore`
2. **Cleaner Architecture**: Eliminates the need for V1 converters and hybrid service approaches
3. **Future-Proofing**: Prepares for complete removal of V1 SDK dependencies

## What Gets Migrated

The migration converts the following:
- Basic fields (id, name, description, tenantId)
- Performance cycle reference
- Pillar and category maps (already using V2 structures)
- Potential score data
- Assessment configuration
- Timestamps (converted from Date to Instant)
- Audit fields (createdBy, etc.)

## Running the Migration

### Prerequisites

1. Ensure AWS credentials are configured
2. Backup your DynamoDB data
3. Verify no active AssessmentMatrix operations are running

### Migration Commands

```bash
# Dry run - shows what would be migrated without making changes
mvn exec:java@assessmentmatrix-v1-to-v2-migration -Dexec.args="--dry-run"

# Live migration - performs the actual migration
mvn exec:java@assessmentmatrix-v1-to-v2-migration

# Alternative using full class name (if execution not configured)
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.AssessmentMatrixV1ToV2Migration" -Dexec.args="--dry-run"
```

## Migration Process

1. **Scanning**: The tool scans all V1 AssessmentMatrix records
2. **Checking**: For each record, it checks if a V2 version already exists
3. **Converting**: V1 records are converted to V2 format
4. **Saving**: V2 records are saved to DynamoDB
5. **Verification**: Migration success is logged for each record

## Post-Migration Steps

1. **Verify Data**: Check that all AssessmentMatrix records are accessible via V2 services
2. **Update Services**: Ensure all services use V2 AssessmentMatrix entities
3. **Test Functionality**: Run integration tests to verify assessment matrix operations
4. **Clean Up**: Once verified, consider running with `--delete-v1` to remove V1 records

## Rollback

If issues occur:
1. V1 records remain intact unless `--delete-v1` was used
2. V2 records can be deleted and migration re-run
3. Services can temporarily revert to V1 entities if needed

## Common Issues

1. **Deserialization Errors**: Usually indicates mixed V1/V2 data - this migration fixes it
2. **Missing Converters**: The migration handles all necessary conversions
3. **Performance**: For large datasets, consider running during off-peak hours

## Safety Features

- Dry run mode for preview
- Skip already migrated records
- Comprehensive error logging
- V1 deletion requires explicit flag
- 10-second warning before destructive operations