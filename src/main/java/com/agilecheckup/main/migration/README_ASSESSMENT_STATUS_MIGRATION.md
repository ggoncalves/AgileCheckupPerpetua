# AssessmentStatus Migration Guide

## Overview

This migration adds the `AssessmentStatus` field to existing `EmployeeAssessment` records in DynamoDB. All existing records will be set to `INVITED` status (the default value).

## Background

The `AssessmentStatus` field was added to track the lifecycle of employee assessments:
- **INVITED**: Default status when assessment is created
- **CONFIRMED**: User has confirmed participation
- **IN_PROGRESS**: User has started answering questions
- **COMPLETED**: User has finished the assessment

## Migration Details

### What it does:
1. Scans all EmployeeAssessment records in DynamoDB
2. Adds `assessmentStatus = "INVITED"` to records that don't have this field
3. Skips records that already have the field
4. Provides detailed logging of the migration process

### Running the Migration

#### Prerequisites:
1. Backup your DynamoDB data before running
2. Ensure AWS credentials are configured
3. Ensure the application has read/write access to the EmployeeAssessment table

#### Command:
```bash
cd AgileCheckupPerpetua
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentStatusMigration"
```

#### Dry Run (Recommended first):
```bash
# Note: Dry run functionality is planned but not yet implemented
# For now, review the code and run with confidence after backup
```

### Expected Output:
```
=== EmployeeAssessment AssessmentStatus Migration Tool ===
This will add AssessmentStatus field to all EmployeeAssessment records without this field.
All existing records will be set to INVITED status.
Make sure to backup your data before proceeding!
Starting in 5 seconds... Press Ctrl+C to cancel

Starting EmployeeAssessment AssessmentStatus migration...
Migrated employee assessment: assessment-id-1
Migrated employee assessment: assessment-id-2
...
AssessmentStatus migration completed!
Total employee assessments processed: 100
Employee assessments migrated: 95
Employee assessments skipped (already have status): 5
Employee assessments with errors: 0
```

## Code Changes Made

### 1. Entity Changes
- Added `assessmentStatus` field to `EmployeeAssessment.java`
- Set default value to `INVITED` for new instances
- Field is nullable to handle existing data gracefully

### 2. Service Layer Changes
- Updated `EmployeeAssessmentService.createEmployeeAssessment()` to set default status
- Added `updateAssessmentStatus()` method with validation
- Updated `incrementAnsweredQuestionCount()` to transition from INVITED to IN_PROGRESS
- Added null-safety checks for existing data

### 3. Validation
- Created `AssessmentStatusValidator` for status transition rules
- Validates allowed transitions:
  - INVITED → CONFIRMED or IN_PROGRESS
  - CONFIRMED → IN_PROGRESS
  - IN_PROGRESS → COMPLETED
  - COMPLETED → (no transitions allowed)

### 4. Tests
- Entity tests for default value assignment
- Service tests for status management and validation
- Validator tests for all transition scenarios
- Integration tests for migration behavior

## Rollback Plan

If issues occur during migration:

1. **Stop the migration** (Ctrl+C if still running)
2. **Restore from backup** using your DynamoDB backup strategy
3. **Review logs** to identify the issue
4. **Fix the code** and re-test in a development environment
5. **Re-run migration** after fixes are verified

## Post-Migration Verification

After running the migration:

1. **Check the logs** for any errors
2. **Verify record counts** match expectations
3. **Sample a few records** to confirm the field was added correctly:
   ```bash
   aws dynamodb get-item --table-name EmployeeAssessment --key '{"id":{"S":"your-test-id"}}'
   ```
4. **Test the application** to ensure normal functionality
5. **Run the test suite** to verify everything works:
   ```bash
   mvn test
   ```

## Troubleshooting

### Common Issues:

1. **Permission Errors**: Ensure AWS credentials have DynamoDB read/write access
2. **Network Issues**: Check AWS connectivity and retry
3. **Partial Migration**: Re-run the migration - it will skip already migrated records
4. **Performance**: Large tables may take time - monitor CloudWatch metrics

### Recovery Steps:

1. Check application logs for detailed error messages
2. Verify AWS credentials and permissions
3. Ensure table names match your environment
4. Consider running during low-traffic periods

## Future Considerations

- The migration is idempotent and can be safely re-run
- New assessments will automatically have the status field
- Consider adding status indexes for querying by status
- Monitor application performance after adding the new field