# AssessmentStatus Migration - Quick Reference

## 🚀 Complete Migration Process (Copy & Paste Commands)

### Step 1: Create Backup
```bash
cd /Users/ggoncalves/dev/AgileCheckup/AgileCheckupPerpetua
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript"
```

### Step 2: Validate Backup
```bash
# Replace with actual backup filename from Step 1
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentBackupScript" -Dexec.args="--validate employeeassessment_backup_YYYYMMDD_HHMMSS.json"
```

### Step 3: Run Migration
```bash
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentStatusMigration"
```

### Step 4: Verify Migration Success
```bash
# Check a few records have assessmentStatus field
aws dynamodb scan --table-name EmployeeAssessment --limit 5 --filter-expression "attribute_exists(assessmentStatus)"

# Run tests to verify everything works
mvn test -Dtest=EmployeeAssessmentServiceTest
```

## 🚨 Emergency Rollback (If Migration Fails)

### Complete Rollback (Delete All + Restore)
```bash
# DANGER: This deletes all current data and restores from backup
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_YYYYMMDD_HHMMSS.json --delete-existing"
```

### Partial Rollback (Add Missing Records Only)
```bash
# Safer: Only adds missing records without deleting existing ones
mvn exec:java -Dexec.mainClass="com.agilecheckup.main.migration.EmployeeAssessmentRestoreScript" -Dexec.args="employeeassessment_backup_YYYYMMDD_HHMMSS.json"
```

## 📊 Quick Status Checks

### Check Record Count
```bash
aws dynamodb describe-table --table-name EmployeeAssessment --query 'Table.ItemCount'
```

### Check How Many Records Have AssessmentStatus
```bash
aws dynamodb scan --table-name EmployeeAssessment --select COUNT --filter-expression "attribute_exists(assessmentStatus)"
```

### Check How Many Records DON'T Have AssessmentStatus
```bash
aws dynamodb scan --table-name EmployeeAssessment --select COUNT --filter-expression "attribute_not_exists(assessmentStatus)"
```

### Sample Records With AssessmentStatus
```bash
aws dynamodb scan --table-name EmployeeAssessment --limit 3 --filter-expression "attribute_exists(assessmentStatus)" --projection-expression "id,assessmentStatus"
```

## 🎯 Success Criteria

✅ **Migration is successful when:**
- All records have `assessmentStatus` field
- All new records get `INVITED` status by default  
- Status transitions work correctly (INVITED → IN_PROGRESS on first answer)
- All existing tests pass
- Application functions normally

## ⚠️ Failure Scenarios & Solutions

| Problem | Solution |
|---------|----------|
| Migration stops with errors | Run complete rollback, fix code, retry |
| Some records missing assessmentStatus | Run migration again (idempotent) |
| Application errors after migration | Run complete rollback, investigate |
| Data corruption detected | Run complete rollback immediately |
| Performance issues | Monitor CloudWatch, consider batch size tuning |

## 🔧 Script Locations

```
src/main/java/com/agilecheckup/main/migration/
├── EmployeeAssessmentBackupScript.java      # Creates backup
├── EmployeeAssessmentRestoreScript.java     # Restores from backup  
├── EmployeeAssessmentStatusMigration.java   # Adds assessmentStatus field
├── README_BACKUP_RESTORE.md                 # Detailed backup/restore guide
├── README_ASSESSMENT_STATUS_MIGRATION.md    # Detailed migration guide
└── MIGRATION_QUICK_REFERENCE.md             # This file
```

## 📞 Emergency Protocol

1. **STOP** all migration operations immediately
2. **DOCUMENT** current state (record counts, error messages)
3. **ROLLBACK** using backup if data integrity is compromised
4. **CONTACT** team lead before attempting fixes
5. **TEST** all fixes in development environment first

## 🕐 Estimated Timing

| Records | Backup Time | Migration Time | Restore Time |
|---------|-------------|----------------|--------------|
| < 1,000 | 1-2 min | 1-2 min | 2-3 min |
| 1,000 - 10,000 | 2-5 min | 3-5 min | 5-10 min |
| 10,000 - 100,000 | 5-15 min | 10-15 min | 15-30 min |
| > 100,000 | 15+ min | 20+ min | 30+ min |

## 💡 Pro Tips

- **Always create backup first** - No exceptions!
- **Test in development** before production
- **Run during low-traffic periods**
- **Monitor CloudWatch metrics** during operations
- **Keep backup files for 30+ days**
- **Document any customizations** or issues encountered

---

**Remember: Better safe than sorry. When in doubt, backup and rollback!** 🛡️