# EmployeeAssessment Backup & Restore Guide

## Overview

This guide provides comprehensive instructions for backing up and restoring EmployeeAssessment data before and after the AssessmentStatus migration. The backup/restore process ensures data safety during migration operations.

## 🛡️ Safety First - Migration Process Order

**CRITICAL: Follow this exact order to ensure data safety:**

1. **Create Backup** ← Start here
2. **Validate Backup**
3. **Run Migration**
4. **Verify Migration Success**
5. **Keep Backup for Rollback** (if needed)

## 📦 Backup Process

### 1. Create Backup

```bash
cd AgileCheckupPerpetua

# Create backup (recommended before any migration)
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript"
```

**Output Example:**
```
=== EmployeeAssessment Backup Tool ===
This will create a complete backup of the EmployeeAssessment table.
Starting backup in 3 seconds... Press Ctrl+C to cancel

Starting EmployeeAssessment backup to file: employeeassessment_backup_20250610_165500.json
Backed up 100 records...
Backed up 200 records...
...
Backup completed successfully!
Backup file: employeeassessment_backup_20250610_165500.json
Total records backed up: 1245
Records with errors: 0
```

### 2. Validate Backup

```bash
# Validate the backup file integrity
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript" -Dexec.args="--validate employeeassessment_backup_20250610_165500.json"
```

**Expected Output:**
```
=== EmployeeAssessment Backup Tool ===
Validating backup file: employeeassessment_backup_20250610_165500.json
Backup validation successful!
Total records in backup: 1245
File size: 2547893 bytes
```

### 3. Backup File Structure

The backup file contains:
```json
{
  "backupMetadata": {
    "backupTimestamp": "20250610_165500",
    "backupDateTime": "2025-06-10T16:55:00.123",
    "tableName": "EmployeeAssessment",
    "tableStatus": "ACTIVE",
    "itemCount": 1245,
    "tableArn": "arn:aws:dynamodb:region:account:table/EmployeeAssessment",
    "keySchema": [...],
    "attributeDefinitions": [...]
  },
  "records": [
    {
      "id": {"S": "assessment-id-1"},
      "assessmentMatrixId": {"S": "matrix-id"},
      "employee": {"S": "{...employee JSON...}"},
      ...
    }
  ],
  "totalRecords": 1245,
  "errors": 0
}
```

## 🔄 Restoration Process

### When to Restore

Restore from backup in these scenarios:
1. **Migration Failed Partially** - Some records migrated, others didn't
2. **Data Corruption** - Migration caused unexpected data issues
3. **Rollback Required** - Need to return to pre-migration state
4. **Testing** - Restore test environment to known state

### Restoration Options

#### Option 1: Restore Without Deleting (Add Missing Records)
```bash
# Safer option - only adds records that don't exist
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json"
```

#### Option 2: Complete Restoration (Delete All + Restore)
```bash
# DANGER: Deletes ALL existing data first, then restores
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json --delete-existing"
```

### Restoration Process Example

```
=== EmployeeAssessment Restoration Tool ===
⚠️  DANGER: THIS WILL DELETE ALL EXISTING DATA ⚠️

Backup file: employeeassessment_backup_20250610_165500.json
Delete existing data: true

This operation will:
1. DELETE ALL existing EmployeeAssessment records
2. Restore all records from the backup file
3. This action CANNOT be undone easily

Make sure you understand the implications!
Type 'CONFIRM' to proceed or press Ctrl+C to cancel:
CONFIRM

Starting restoration in 5 seconds... Press Ctrl+C to cancel
Loading backup file: employeeassessment_backup_20250610_165500.json (Size: 2547893 bytes)
Backup metadata: table=EmployeeAssessment, timestamp=2025-06-10T16:55:00.123, originalRecords=1245
Deleting existing data from table...
Existing data deletion completed. 1250 items deleted
Starting restoration of 1245 records...
Restored 100 records so far...
Restored 200 records so far...
...
Restoration completed!
Records restored: 1245
Records deleted: 1250
Restoration errors: 0
```

## 🚨 Emergency Rollback Scenarios

### Scenario 1: Migration Failed Midway

**Symptoms:**
- Migration stopped with errors
- Some records have `assessmentStatus`, others don't
- Inconsistent data state

**Solution:**
```bash
# 1. Stop any running migration
# 2. Complete restoration (delete corrupted data + restore clean backup)
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json --delete-existing"

# 3. Verify restoration
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript" -Dexec.args="--validate employeeassessment_backup_20250610_165500.json"

# 4. Fix migration code and retry
```

### Scenario 2: Migration Completed But Data Corrupted

**Symptoms:**
- Migration shows success
- Application errors occur
- Data integrity issues

**Solution:**
```bash
# 1. Immediate rollback
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json --delete-existing"

# 2. Investigate issues in development environment
# 3. Fix problems and re-test migration
```

### Scenario 3: Partial Data Loss

**Symptoms:**
- Some records missing after migration
- Record count decreased unexpectedly

**Solution:**
```bash
# Option 1: Add missing records (safer)
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json"

# Option 2: Complete restoration if corruption is extensive
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_20250610_165500.json --delete-existing"
```

## 🔍 Verification Steps

### After Backup
```bash
# 1. Check backup file exists and has reasonable size
ls -la employeeassessment_backup_*.json

# 2. Validate backup integrity
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript" -Dexec.args="--validate employeeassessment_backup_20250610_165500.json"

# 3. Sample a few records
aws dynamodb scan --table-name EmployeeAssessment --limit 5 --projection-expression "id,assessmentMatrixId"
```

### After Restoration
```bash
# 1. Check record count
aws dynamodb describe-table --table-name EmployeeAssessment --query 'Table.ItemCount'

# 2. Sample records to verify structure
aws dynamodb get-item --table-name EmployeeAssessment --key '{"id":{"S":"known-test-id"}}'

# 3. Run application tests
mvn test -Dtest=EmployeeAssessmentServiceTest

# 4. Check for assessmentStatus field presence/absence
aws dynamodb scan --table-name EmployeeAssessment --limit 5 --filter-expression "attribute_exists(assessmentStatus)"
```

## ⚡ Performance Considerations

### Backup Performance
- **Large tables (>10k records)**: May take 5-15 minutes
- **Very large tables (>100k records)**: May take 30+ minutes
- **Disk space**: Approximately 2-5KB per record in JSON format
- **Memory usage**: Minimal - processes records in batches

### Restoration Performance
- **Deletion phase**: ~1000 records/second
- **Restoration phase**: ~100-200 records/second (batch writes)
- **Total time**: Roughly 2x backup time

### Optimization Tips
```bash
# Monitor DynamoDB metrics during operations
aws cloudwatch get-metric-statistics --namespace AWS/DynamoDB --metric-name ConsumedReadCapacityUnits --dimensions Name=TableName,Value=EmployeeAssessment --start-time 2025-06-10T16:00:00Z --end-time 2025-06-10T17:00:00Z --period 300 --statistics Sum

# Check for throttling
aws logs filter-log-events --log-group-name /aws/dynamodb/table --filter-pattern "throttling"
```

## 🛠️ Troubleshooting

### Common Issues

#### 1. Permission Errors
```
Error: User: arn:aws:iam::account:user/username is not authorized to perform: dynamodb:Scan
```

**Solution:**
```bash
# Ensure AWS credentials have these permissions:
# - dynamodb:Scan
# - dynamodb:BatchWriteItem
# - dynamodb:DescribeTable
# - dynamodb:PutItem
# - dynamodb:DeleteItem

# Check current permissions
aws sts get-caller-identity
aws iam get-user
```

#### 2. Large File Issues
```
Error: OutOfMemoryError during backup
```

**Solution:**
```bash
# Increase JVM memory
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn exec:java -Dexec.mainClass="..."
```

#### 3. Network Timeouts
```
Error: Unable to execute HTTP request: timeout
```

**Solution:**
```bash
# Retry the operation - scripts are idempotent
# Check AWS service status
# Verify network connectivity
```

#### 4. Corrupted Backup File
```
Error: Invalid backup file structure
```

**Solution:**
```bash
# Re-create backup
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript"

# Validate new backup
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript" -Dexec.args="--validate new_backup_file.json"
```

## 📋 Pre-Migration Checklist

- [ ] Create backup
- [ ] Validate backup file
- [ ] Verify backup contains expected record count
- [ ] Ensure sufficient disk space
- [ ] Confirm AWS permissions
- [ ] Test restoration in development environment
- [ ] Document backup file location
- [ ] Notify team of maintenance window
- [ ] Have rollback plan ready

## 📋 Post-Migration Checklist

- [ ] Verify migration completed successfully
- [ ] Check record count matches expectations
- [ ] Test application functionality
- [ ] Run automated tests
- [ ] Keep backup file for 30+ days
- [ ] Document any issues encountered
- [ ] Update team on completion status

## 🔐 Security Notes

- **Backup files contain sensitive data** - store securely
- **Use encryption at rest** for backup storage
- **Limit access** to backup/restore scripts
- **Audit trail** - log all backup/restore operations
- **Clean up old backups** after verification period

## 📞 Emergency Contacts

In case of critical issues during migration:
1. **Stop all operations immediately**
2. **Document the current state**
3. **Contact team lead/DBA**
4. **Prepare for potential rollback**
5. **Do not attempt fixes without approval**

---

**Remember: When in doubt, restore from backup and investigate in a safe environment!**