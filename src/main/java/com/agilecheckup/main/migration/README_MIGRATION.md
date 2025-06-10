# Team Data Migration Guide

This guide explains how to migrate Team records from the old structure (with embedded department object) to the new structure (with departmentId string).

## Background

**Old Structure:**
```json
{
  "id": "team-123",
  "name": "Backend Team",
  "department": {
    "id": "dept-456",
    "name": "Engineering",
    "companyId": "comp-789"
  }
}
```

**New Structure:**
```json
{
  "id": "team-123",
  "name": "Backend Team",
  "departmentId": "dept-456"
}
```

## Migration Steps

### 1. Build the Project
```bash
cd /Users/ggoncalves/dev/AgileCheckup/AgileCheckupPerpetua
mvn clean compile
```

### 2. Create a Backup (IMPORTANT!)
Always backup your data before migration:

```bash
mvn exec:java@team-backup
```

This will create a file like `team_backup_20240529_143022.json` in the current directory.

### 3. Run the Migration
```bash
mvn exec:java@team-migration
```

The migration will:
- Scan all Team records
- Extract the department ID from the embedded department object
- Add a new `departmentId` field with the extracted ID
- Remove the old `department` object
- Log progress and results

### 4. Verify the Migration
Check a few teams in DynamoDB console to ensure they have:
- `departmentId` field (string)
- No `department` field (object)

## If Something Goes Wrong

### Restore from Backup
If the migration fails and you need to restore:
1. Manually restore the backup JSON file to DynamoDB
2. Or write a custom restore script based on your needs

## Important Notes

1. **Run Once**: This migration should only be run once. Running it multiple times won't hurt (it skips already migrated records), but it's unnecessary.

2. **EmployeeAssessment**: This migration does NOT update the EmployeeAssessment table, which still contains embedded Team objects. That will need a separate migration.

3. **Downtime**: Consider running this during a maintenance window as it modifies production data.

4. **Testing**: Test the migration on a development/staging environment first if possible.

## Monitoring

The migration logs:
- Total teams processed
- Teams successfully migrated
- Teams skipped (already in new format)
- Teams with errors

Check the logs carefully for any errors before deploying the new code.

---

# EmployeeAssessment Data Migration Guide

This guide explains how to migrate EmployeeAssessment records from the old structure (with embedded team object) to the new structure (with teamId string).

## Background

**Old Structure:**
```json
{
  "id": "assessment-123",
  "assessmentMatrixId": "matrix-456",
  "team": {
    "id": "team-789",
    "name": "Backend Team",
    "departmentId": "dept-012"
  },
  "employee": {
    "id": "emp-345",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**New Structure:**
```json
{
  "id": "assessment-123",
  "assessmentMatrixId": "matrix-456",
  "teamId": "team-789",
  "employee": {
    "id": "emp-345",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

## Migration Steps

### 1. Build the Project
```bash
cd /Users/ggoncalves/dev/AgileCheckup/AgileCheckupPerpetua
mvn clean compile
```

### 2. Create a Backup (IMPORTANT!)
Always backup your data before migration:

```bash
mvn exec:java@employeeassessment-backup
```

This will create a file like `employeeassessment_backup_20240529_143022.json` in the current directory.

### 3. Run the Migration
```bash
mvn exec:java@employeeassessment-migration
```

The migration will:
- Scan all EmployeeAssessment records
- Extract the team ID from the embedded team object
- Add a new `teamId` field with the extracted ID
- Remove the old `team` object
- Log progress and results

### 4. Verify the Migration
Check a few employee assessments in DynamoDB console to ensure they have:
- `teamId` field (string)
- No `team` field (object)

## If Something Goes Wrong

### Restore from Backup
If the migration fails and you need to restore:
1. Manually restore the backup JSON file to DynamoDB
2. Or write a custom restore script based on your needs

## Important Notes

1. **Run Once**: This migration should only be run once. Running it multiple times won't hurt (it skips already migrated records), but it's unnecessary.

2. **Dependencies**: The new EmployeeAssessment entity code must be deployed AFTER this migration is complete.

3. **Downtime**: Consider running this during a maintenance window as it modifies production data.

4. **Testing**: Test the migration on a development/staging environment first if possible.

## Monitoring

The migration logs:
- Total employee assessments processed
- Employee assessments successfully migrated
- Employee assessments skipped (already in new format or no team)
- Employee assessments with errors

Check the logs carefully for any errors before deploying the new code.