# PerformanceCycle V1-to-V2 Data Migration Guide

This guide explains how to migrate PerformanceCycle records from V1 structure to V2 structure.

## Background

The PerformanceCycle entity has been upgraded from V1 to V2 to improve date handling and compatibility with AWS SDK V2.

**Key Differences:**

| Aspect | V1 Format | V2 Format |
|--------|-----------|-----------|
| Date Fields | `java.util.Date` (ISO timestamp) | `java.time.LocalDate` (YYYY-MM-DD) |
| AWS SDK | AWS SDK v1 with DynamoDBMapper | AWS SDK v2 with Enhanced Client |
| Entity Base | `TenantDescribableEntity` | `TenantDescribableEntityV2` |
| Date Storage | `"2025-07-28T18:42:22.416Z"` | `"2025-07-28"` |

**Example Migration:**

**V1 Structure:**
```json
{
  "id": "cycle-123",
  "name": "Q1 2024 Performance Review",
  "description": "First quarter review cycle",
  "companyId": "company-456",
  "isTimeSensitive": true,
  "isActive": true,
  "startDate": "2025-01-01T00:00:00.000Z",
  "endDate": "2025-03-31T23:59:59.999Z",
  "tenantId": "tenant-789"
}
```

**V2 Structure:**
```json
{
  "id": "cycle-123",
  "name": "Q1 2024 Performance Review", 
  "description": "First quarter review cycle",
  "companyId": "company-456",
  "isTimeSensitive": true,
  "isActive": true,
  "startDate": "2025-01-01",
  "endDate": "2025-03-31",
  "tenantId": "tenant-789",
  "migrationVersion": "V2",
  "lastUpdatedDate": "2025-07-28T18:42:22.416Z"
}
```

## Migration Process

The migration performs the following transformations:

1. **Date Conversion**: Converts ISO timestamp strings to LocalDate format (YYYY-MM-DD)
2. **Migration Marker**: Adds `migrationVersion: "V2"` to track migrated records
3. **Timestamp Update**: Updates `lastUpdatedDate` with current timestamp
4. **Idempotency**: Skips already migrated records to allow safe re-runs

## Migration Steps

### 1. Build the Project
```bash
cd /Users/ggoncalves/dev/AgileCheckup/AgileCheckupPerpetua
mvn clean compile
```

### 2. Create a Backup (CRITICAL!)
**Always backup your data before migration:**

```bash
# Using MigrationRunner
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.MigrationRunner" -Dexec.args="PerformanceCycleDataBackup"

# Or run directly
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.PerformanceCycleDataBackup"
```

This will create a file like `performancecycle_backup_20250728_154023.json` in the current directory.

### 3. Run the Migration

```bash
# Using MigrationRunner (recommended)
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.MigrationRunner" -Dexec.args="PerformanceCycleDataMigration"

# Or run directly
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.PerformanceCycleDataMigration"
```

The migration will:
- Scan all PerformanceCycle records
- Convert Date fields from ISO timestamp to LocalDate format
- Add migration version marker
- Update lastUpdatedDate timestamp
- Log detailed progress and results

### 4. Verify the Migration

Check a few performance cycles in DynamoDB console to ensure they have:
- `startDate` and `endDate` in YYYY-MM-DD format (if present)
- `migrationVersion` field with value "V2"
- Updated `lastUpdatedDate` timestamp

Sample verification query:
```bash
# Check a specific record
aws dynamodb get-item --table-name PerformanceCycle --key '{"id":{"S":"your-cycle-id"}}'
```

## Important Notes

### Safety Features
1. **Idempotent**: Safe to run multiple times - skips already migrated records
2. **Non-destructive**: Preserves all original data, only converts formats
3. **Rollback-friendly**: Backup allows full restoration if needed

### Migration Detection
The script detects already migrated records by:
- Checking for `migrationVersion: "V2"` marker
- Validating date format (YYYY-MM-DD vs ISO timestamp)

### Error Handling
- Individual record failures don't stop the migration
- Detailed error logging for troubleshooting
- Comprehensive statistics at completion

## Expected Results

After successful migration:
- **Date Fields**: All `startDate`/`endDate` fields converted to YYYY-MM-DD format
- **Compatibility**: V2 repositories can read all records without DateTimeParseException
- **Performance**: Improved query performance with AWS SDK V2 Enhanced Client
- **Consistency**: All records have V2 migration marker for tracking

## If Something Goes Wrong

### Restore from Backup
If the migration fails and you need to restore:
1. Stop all applications using the PerformanceCycle table
2. Restore the backup JSON file to DynamoDB using AWS CLI or custom script
3. Verify data integrity before resuming operations

### Partial Migration Recovery
If migration partially completes:
1. Review error logs to identify failed records
2. Fix any data inconsistencies manually
3. Re-run migration (it will skip successfully migrated records)

## Testing Recommendations

1. **Test Environment**: Run migration on development/staging environment first
2. **Data Validation**: Compare record counts before/after migration
3. **Application Testing**: Verify both V1 and V2 services work after migration
4. **Performance Testing**: Confirm no query performance degradation

## Monitoring

The migration provides detailed logging:
- Total records processed
- Records successfully migrated
- Records skipped (already migrated)
- Records with errors
- Processing time and performance metrics

## Post-Migration Steps

1. **Deploy V2 Code**: Deploy application code that uses V2 entities
2. **Monitor Errors**: Watch for any DateTimeParseException errors (should be eliminated)
3. **Performance Check**: Verify improved query performance
4. **Cleanup**: Archive backup files after successful verification

---

**⚠️ IMPORTANT REMINDERS:**
- Always backup data before migration
- Test in non-production environment first
- Consider maintenance window for large datasets
- Keep backup files until migration is fully verified
- Monitor application logs for any issues post-migration