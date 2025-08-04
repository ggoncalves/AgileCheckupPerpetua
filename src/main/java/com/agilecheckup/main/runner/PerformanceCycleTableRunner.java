package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.PerformanceCycleServiceV2;
import com.agilecheckup.service.PerformanceCycleService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Log4j2
public class PerformanceCycleTableRunner implements CrudRunner {

    private PerformanceCycleServiceV2 performanceCycleServiceV2;
    private PerformanceCycleService performanceCycleService;
    private CompanyServiceV2 companyServiceV2;
    private CompanyV2 testCompany;
    private String testTenantId;
    private final boolean shouldCleanAfterComplete;

    public PerformanceCycleTableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    public void run() {
        log.info("\n=== PerformanceCycle V1-to-V2 Migration Demo ===");
        
        try {
            // 1. Setup test data (Company for FK relationship)
            setupTestData();
            
            if (testCompany == null) {
                log.error("Failed to create test company. Aborting demo.");
                return;
            }
            
            // 2. Demonstrate V1 CRUD operations
            log.info("\n1. Demonstrating V1 (Legacy) PerformanceCycle operations...");
            List<PerformanceCycle> v1Cycles = demonstrateV1Operations();
            
            // 3. Demonstrate V2 CRUD operations
            log.info("\n2. Demonstrating V2 PerformanceCycle operations...");
            List<PerformanceCycleV2> v2Cycles = demonstrateV2Operations();
            
            // 4. Show side-by-side comparison
            log.info("\n3. Side-by-side V1 vs V2 comparison:");
            demonstrateSideBySideComparison(v1Cycles.get(0), v2Cycles.get(0));
            
            // 5. Demonstrate migration-ready queries
            log.info("\n4. Demonstrating V2 query capabilities...");
            try {
                demonstrateV2QueryCapabilities();
            } catch (Exception e) {
                log.warn("V2 Query failed (likely due to unmigrated V1 records): {}", e.getMessage());
                log.warn("This is expected during migration phase - V1 records need to be migrated first.");
            }
            
            // 6. Show business rule consistency
            log.info("\n5. Demonstrating business rule consistency (isTimeSensitive calculation)...");
            demonstrateBusinessRuleConsistency();
            
            // 7. Cleanup
            if (shouldCleanAfterComplete) {
                log.info("\n6. Cleaning up test data...");
                cleanupTestData(v1Cycles, v2Cycles);
            }
            
        } catch (Exception e) {
            log.error("Error in PerformanceCycleTableRunner: {}", e.getMessage(), e);
        } finally {
            // Always cleanup test company
            cleanupTestCompany();
        }
        
        log.info("\n=== PerformanceCycle V1-to-V2 Migration Demo Complete ===");
    }
    
    private void setupTestData() {
        log.info("Setting up test data (Company)...");
        
        // Create test company
        Optional<CompanyV2> companyOpt = getCompanyService().create(
            "12345678000199",  // documentNumber
            "PerformanceCycle Test Company",  // name
            "pc-test@company.com",  // email
            "Company for PerformanceCycle V2 testing",  // description
            "pc-test-tenant-" + System.currentTimeMillis()  // unique tenantId
        );
        
        if (companyOpt.isPresent()) {
            testCompany = companyOpt.get();
            testTenantId = testCompany.getTenantId();
            log.info("Created test company: {} (Tenant: {})", testCompany.getName(), testTenantId);
        } else {
            log.error("Failed to create test company");
        }
    }
    
    private List<PerformanceCycle> demonstrateV1Operations() {
        List<PerformanceCycle> cycles = new ArrayList<>();
        
        // Create V1 PerformanceCycle
        log.info("Creating V1 PerformanceCycle with Legacy service...");
        Optional<PerformanceCycle> cycleOpt = getPerformanceCycleServiceLegacy().create(
            "Q1 2024 Performance Review",
            "First quarter performance evaluation cycle",
            testTenantId,
            testCompany.getId(),
            true,  // isActive
            true,  // isTimeSensitive (will be overridden by business rule)
            Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()),  // startDate
            Date.from(LocalDate.of(2024, 3, 31).atStartOfDay(ZoneId.systemDefault()).toInstant())  // endDate
        );
        
        if (cycleOpt.isPresent()) {
            PerformanceCycle cycle = cycleOpt.get();
            cycles.add(cycle);
            log.info("✓ V1 PerformanceCycle created: {} (ID: {})", cycle.getName(), cycle.getId());
            log.info("  - isTimeSensitive: {} (calculated by business rule)", cycle.getIsTimeSensitive());
            
            // Update V1 PerformanceCycle
            log.info("Updating V1 PerformanceCycle...");
            Optional<PerformanceCycle> updatedOpt = getPerformanceCycleServiceLegacy().update(
                cycle.getId(),
                "Q1 2024 - Updated",
                "Updated first quarter evaluation",
                testTenantId,
                testCompany.getId(),
                false,  // isActive (changed to false)
                false,  // isTimeSensitive
                Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null  // endDate removed
            );
            
            if (updatedOpt.isPresent()) {
                log.info("✓ V1 PerformanceCycle updated: isActive={}, isTimeSensitive={}", 
                    updatedOpt.get().getIsActive(), updatedOpt.get().getIsTimeSensitive());
            }
        }
        
        return cycles;
    }
    
    private List<PerformanceCycleV2> demonstrateV2Operations() {
        List<PerformanceCycleV2> cycles = new ArrayList<>();
        
        // Create V2 PerformanceCycle
        log.info("Creating V2 PerformanceCycle with V2 service...");
        Optional<PerformanceCycleV2> cycleOpt = getPerformanceCycleService().create(
            testTenantId,  // tenantId first (V2 signature)
            "Q2 2024 Performance Review",
            "Second quarter performance evaluation cycle",
            testCompany.getId(),
            true,  // isActive
            true,  // isTimeSensitive (will be calculated by business rule)
            LocalDate.of(2024, 4, 1),  // startDate (LocalDate)
            LocalDate.of(2024, 6, 30)  // endDate (LocalDate)
        );
        
        if (cycleOpt.isPresent()) {
            PerformanceCycleV2 cycle = cycleOpt.get();
            cycles.add(cycle);
            log.info("✓ V2 PerformanceCycle created: {} (ID: {})", cycle.getName(), cycle.getId());
            log.info("  - isTimeSensitive: {} (calculated by business rule)", cycle.getIsTimeSensitive());
            
            // Update V2 PerformanceCycle
            log.info("Updating V2 PerformanceCycle...");
            Optional<PerformanceCycleV2> updatedOpt = getPerformanceCycleService().update(
                cycle.getId(),
                testTenantId,  // V2 signature with tenantId first
                "Q2 2024 - Enhanced",
                "Enhanced second quarter evaluation with extended timeline",
                testCompany.getId(),
                true,  // isActive
                false,  // isTimeSensitive (will be recalculated)
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 9, 30)  // extended endDate
            );
            
            if (updatedOpt.isPresent()) {
                log.info("✓ V2 PerformanceCycle updated: isActive={}, isTimeSensitive={}", 
                    updatedOpt.get().getIsActive(), updatedOpt.get().getIsTimeSensitive());
            }
        }
        
        return cycles;
    }
    
    private void demonstrateSideBySideComparison(PerformanceCycle v1Cycle, PerformanceCycleV2 v2Cycle) {
        log.info("V1 PerformanceCycle:");
        log.info("  - ID: {}", v1Cycle.getId());
        log.info("  - Name: {}", v1Cycle.getName());
        log.info("  - Date Type: {} (Legacy Date)", v1Cycle.getStartDate().getClass().getSimpleName());
        log.info("  - isTimeSensitive: {}", v1Cycle.getIsTimeSensitive());
        
        log.info("V2 PerformanceCycle:");
        log.info("  - ID: {}", v2Cycle.getId());
        log.info("  - Name: {}", v2Cycle.getName());
        log.info("  - Date Type: {} (Modern LocalDate)", v2Cycle.getStartDate().getClass().getSimpleName());
        log.info("  - isTimeSensitive: {}", v2Cycle.getIsTimeSensitive());
        log.info("  - Enhanced Client: Uses AWS SDK V2 Enhanced Client");
    }
    
    private void demonstrateV2QueryCapabilities() {
        // Test tenant-based queries
        List<PerformanceCycleV2> tenantCycles = getPerformanceCycleService().findAllByTenantId(testTenantId);
        log.info("✓ Found {} cycles for tenant: {}", tenantCycles.size(), testTenantId);
        
        // Test company-based queries
        List<PerformanceCycleV2> companyCycles = getPerformanceCycleService().findByCompanyId(testCompany.getId());
        log.info("✓ Found {} cycles for company: {}", companyCycles.size(), testCompany.getId());
        
        // Test active cycles query
        List<PerformanceCycleV2> activeCycles = getPerformanceCycleService().findActiveByTenantId(testTenantId);
        log.info("✓ Found {} active cycles for tenant", activeCycles.size());
    }
    
    private void demonstrateBusinessRuleConsistency() {
        // Test business rule: isTimeSensitive = (endDate != null)
        log.info("Testing business rule: isTimeSensitive = (endDate != null)");
        
        // Cycle with endDate - should be time sensitive
        Optional<PerformanceCycleV2> timeSensitiveOpt = getPerformanceCycleService().create(
            testTenantId,
            "Time Sensitive Cycle",
            "Has end date, should be time sensitive",
            testCompany.getId(),
            true,
            false,  // User passes false, but business rule will make it true
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 3, 31)  // Has end date
        );
        
        if (timeSensitiveOpt.isPresent()) {
            log.info("✓ Cycle with endDate: isTimeSensitive = {} (business rule applied)", 
                timeSensitiveOpt.get().getIsTimeSensitive());
        }
        
        // Cycle without endDate - should not be time sensitive
        Optional<PerformanceCycleV2> notTimeSensitiveOpt = getPerformanceCycleService().create(
            testTenantId,
            "Ongoing Cycle",
            "No end date, should not be time sensitive",
            testCompany.getId(),
            true,
            true,  // User passes true, but business rule will make it false
            LocalDate.of(2024, 1, 1),
            null  // No end date
        );
        
        if (notTimeSensitiveOpt.isPresent()) {
            log.info("✓ Cycle without endDate: isTimeSensitive = {} (business rule applied)", 
                notTimeSensitiveOpt.get().getIsTimeSensitive());
        }
    }
    
    private void cleanupTestData(List<PerformanceCycle> v1Cycles, List<PerformanceCycleV2> v2Cycles) {
        // Clean V1 cycles
        for (PerformanceCycle cycle : v1Cycles) {
            try {
                getPerformanceCycleServiceLegacy().delete(cycle);
                log.info("✓ Cleaned up V1 cycle: {}", cycle.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup V1 cycle {}: {}", cycle.getId(), e.getMessage());
            }
        }
        
        // Clean V2 cycles
        for (PerformanceCycleV2 cycle : v2Cycles) {
            try {
                getPerformanceCycleService().deleteById(cycle.getId());
                log.info("✓ Cleaned up V2 cycle: {}", cycle.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup V2 cycle {}: {}", cycle.getId(), e.getMessage());
            }
        }
        
        // Clean any remaining test cycles from the tenant
        try {
            List<PerformanceCycle> remainingV1Cycles = getPerformanceCycleServiceLegacy().findAllByTenantId(testTenantId);
            for (PerformanceCycle cycle : remainingV1Cycles) {
                getPerformanceCycleServiceLegacy().delete(cycle);
                log.info("✓ Cleaned up remaining V1 cycle: {}", cycle.getId());
            }
            
            List<PerformanceCycleV2> remainingV2Cycles = getPerformanceCycleService().findAllByTenantId(testTenantId);
            for (PerformanceCycleV2 cycle : remainingV2Cycles) {
                getPerformanceCycleService().deleteById(cycle.getId());
                log.info("✓ Cleaned up remaining V2 cycle: {}", cycle.getId());
            }
        } catch (Exception e) {
            log.warn("Error cleaning remaining cycles: {}", e.getMessage());
        }
    }
    
    private void cleanupTestCompany() {
        if (testCompany != null) {
            try {
                getCompanyService().deleteById(testCompany.getId());
                log.info("✓ Cleaned up test company: {}", testCompany.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup test company {}: {}", testCompany.getId(), e.getMessage());
            }
        }
    }
    
    // Service getters with lazy initialization
    private PerformanceCycleServiceV2 getPerformanceCycleService() {
        if (performanceCycleServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            performanceCycleServiceV2 = serviceComponent.buildPerformanceCycleService();
        }
        return performanceCycleServiceV2;
    }
    
    private PerformanceCycleService getPerformanceCycleServiceLegacy() {
        if (performanceCycleService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            performanceCycleService = serviceComponent.buildPerformanceCycleServiceLegacy();
        }
        return performanceCycleService;
    }
    
    private CompanyServiceV2 getCompanyService() {
        if (companyServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            companyServiceV2 = serviceComponent.buildCompanyService();
        }
        return companyServiceV2;
    }
}