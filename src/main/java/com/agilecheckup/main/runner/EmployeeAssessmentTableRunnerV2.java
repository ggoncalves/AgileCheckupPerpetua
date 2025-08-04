package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.DepartmentServiceV2;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleServiceV2;
import com.agilecheckup.service.TeamService;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Log4j2
public class EmployeeAssessmentTableRunnerV2 implements CrudRunner {

    private EmployeeAssessmentServiceV2 employeeAssessmentServiceV2;
    private EmployeeAssessmentService employeeAssessmentServiceLegacy;
    private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;
    private TeamService teamService;
    private CompanyServiceV2 companyServiceV2;
    private DepartmentServiceV2 departmentServiceV2;
    private PerformanceCycleServiceV2 performanceCycleServiceV2;
    private CompanyV2 testCompany;
    private DepartmentV2 testDepartment;
    private Team testTeam;
    private PerformanceCycleV2 testPerformanceCycle;
    private AssessmentMatrixV2 testAssessmentMatrix;
    private String testTenantId;
    private final boolean shouldCleanAfterComplete;

    public EmployeeAssessmentTableRunnerV2(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    public void run() {
        log.info("\n=== EmployeeAssessment V2 Migration Demo ===");
        
        try {
            // 1. Setup test data (Company, Department, Team, PerformanceCycle, AssessmentMatrix for FK relationships)
            setupTestData();
            
            if (testCompany == null || testDepartment == null || testTeam == null || testPerformanceCycle == null || testAssessmentMatrix == null) {
                log.error("Failed to create test dependencies. Aborting demo.");
                return;
            }
            
            // 2. Demonstrate V2 CRUD operations
            log.info("\n1. Demonstrating V2 EmployeeAssessment operations...");
            List<EmployeeAssessmentV2> v2Assessments = demonstrateV2Operations();
            
            // 3. Demonstrate GSI query capabilities
            log.info("\n2. Demonstrating V2 GSI query capabilities...");
            demonstrateV2QueryCapabilities();
            
            // 4. Demonstrate business logic (validation, status transitions, scoring)
            log.info("\n3. Demonstrating business logic...");
            demonstrateBusinessLogic(v2Assessments);
            
            // 5. Demonstrate employee validation workflow
            log.info("\n4. Demonstrating employee validation workflow...");
            demonstrateEmployeeValidation();
            
            // 6. Show side-by-side comparison with V1 if available
            log.info("\n5. Side-by-side V1 vs V2 comparison:");
            demonstrateSideBySideComparison();
            
            // 7. Cleanup
            if (shouldCleanAfterComplete) {
                log.info("\n6. Cleaning up test data...");
                cleanupTestData(v2Assessments);
            }
            
        } catch (Exception e) {
            log.error("Error in EmployeeAssessmentTableRunnerV2: {}", e.getMessage(), e);
        } finally {
            // Always cleanup test dependencies
            cleanupTestDependencies();
        }
        
        log.info("\n=== EmployeeAssessment V2 Migration Demo Complete ===");
    }
    
    private void setupTestData() {
        log.info("Setting up test dependencies (Company, Department, Team, PerformanceCycle, AssessmentMatrix)...");
        
        // Create test company
        Optional<CompanyV2> companyOpt = getCompanyService().create(
            "12345678000199",  // documentNumber
            "EmployeeAssessment Test Company",  // name
            "ea-test@company.com",  // email
            "Company for EmployeeAssessment V2 testing",  // description
            "ea-test-tenant-" + System.currentTimeMillis()  // unique tenantId
        );
        
        if (companyOpt.isPresent()) {
            testCompany = companyOpt.get();
            testTenantId = testCompany.getTenantId();
            log.info("Created test company: {} (Tenant: {})", testCompany.getName(), testTenantId);
            
            // Create test department
            Optional<DepartmentV2> departmentOpt = getDepartmentService().create(
                "Test Department",
                "Department for EmployeeAssessment testing",
                testTenantId,
                testCompany.getId()
            );
            
            if (departmentOpt.isPresent()) {
                testDepartment = departmentOpt.get();
                log.info("Created test department: {} (ID: {})", testDepartment.getName(), testDepartment.getId());
                
                // Create test team
                Optional<Team> teamOpt = getTeamService().create(
                    "Test Team",
                    "Team for EmployeeAssessment testing",
                    testTenantId,
                    testDepartment.getId()  // Use department ID, not company ID
                );
                
                if (teamOpt.isPresent()) {
                    testTeam = teamOpt.get();
                    log.info("Created test team: {} (ID: {})", testTeam.getName(), testTeam.getId());
                    
                    // Create test performance cycle
                    Optional<PerformanceCycleV2> performanceCycleOpt = getPerformanceCycleService().create(
                        testTenantId,
                        "Test Performance Cycle",
                        "Performance cycle for EmployeeAssessment testing",
                        testCompany.getId(),
                        true,  // isActive
                        false,  // isTimeSensitive
                        LocalDate.now(),  // startDate
                        null  // endDate (null means not time sensitive)
                    );
                    
                    if (performanceCycleOpt.isPresent()) {
                        testPerformanceCycle = performanceCycleOpt.get();
                        log.info("Created test performance cycle: {} (ID: {})", 
                            testPerformanceCycle.getName(), testPerformanceCycle.getId());
                        
                        // Create test assessment matrix
                        Optional<AssessmentMatrixV2> matrixOpt = getAssessmentMatrixServiceV2().create(
                            "Test Assessment Matrix",
                            "Assessment matrix for EmployeeAssessment testing",
                            testTenantId,
                            testPerformanceCycle.getId(),
                            new HashMap<>()
                        );
                        
                        if (matrixOpt.isPresent()) {
                            testAssessmentMatrix = matrixOpt.get();
                            log.info("Created test assessment matrix: {} (ID: {})", 
                                testAssessmentMatrix.getName(), testAssessmentMatrix.getId());
                        } else {
                            log.error("Failed to create test assessment matrix");
                        }
                    } else {
                        log.error("Failed to create test performance cycle");
                    }
                } else {
                    log.error("Failed to create test team");
                }
            } else {
                log.error("Failed to create test department");
            }
        } else {
            log.error("Failed to create test company");
        }
    }
    
    private List<EmployeeAssessmentV2> demonstrateV2Operations() {
        List<EmployeeAssessmentV2> assessments = new ArrayList<>();
        
        // Create V2 EmployeeAssessments
        log.info("Creating V2 EmployeeAssessments...");
        
        // Employee 1
        Optional<EmployeeAssessmentV2> assessment1Opt = getEmployeeAssessmentServiceV2().create(
            testAssessmentMatrix.getId(),
            testTeam.getId(),
            "John Doe",
            "john.doe@example.com",
            "123456789",
            PersonDocumentType.CPF,
            Gender.MALE,
            GenderPronoun.HE
        );
        
        if (assessment1Opt.isPresent()) {
            EmployeeAssessmentV2 assessment1 = assessment1Opt.get();
            assessments.add(assessment1);
            log.info("✓ V2 EmployeeAssessment created: {} (ID: {})", 
                assessment1.getEmployee().getName(), assessment1.getId());
            log.info("  - Status: {}", assessment1.getAssessmentStatus());
            log.info("  - Email normalized: {}", assessment1.getEmployeeEmailNormalized());
            log.info("  - Answered questions: {}", assessment1.getAnsweredQuestionCount());
        }
        
        // Employee 2
        Optional<EmployeeAssessmentV2> assessment2Opt = getEmployeeAssessmentServiceV2().create(
            testAssessmentMatrix.getId(),
            testTeam.getId(),
            "Jane Smith",
            "JANE.SMITH@EXAMPLE.COM",  // Test email normalization
            "987654321",
            PersonDocumentType.CPF,
            Gender.FEMALE,
            GenderPronoun.SHE
        );
        
        if (assessment2Opt.isPresent()) {
            EmployeeAssessmentV2 assessment2 = assessment2Opt.get();
            assessments.add(assessment2);
            log.info("✓ V2 EmployeeAssessment created: {} (ID: {})", 
                assessment2.getEmployee().getName(), assessment2.getId());
            log.info("  - Email normalized: {} (from uppercase)", assessment2.getEmployeeEmailNormalized());
        }
        
        // Test uniqueness constraint
        log.info("Testing uniqueness constraint (should fail)...");
        try {
            getEmployeeAssessmentServiceV2().create(
                testAssessmentMatrix.getId(),
                testTeam.getId(),
                "John Duplicate",
                "john.doe@example.com",  // Same email as first employee
                "111111111",
                PersonDocumentType.CPF,
                Gender.MALE,
                GenderPronoun.HE
            );
            log.error("❌ Uniqueness constraint failed - duplicate was allowed!");
        } catch (Exception e) {
            log.info("✓ Uniqueness constraint working: {}", e.getMessage());
        }
        
        // Update assessment
        if (!assessments.isEmpty()) {
            EmployeeAssessmentV2 toUpdate = assessments.get(0);
            log.info("Updating V2 EmployeeAssessment...");
            Optional<EmployeeAssessmentV2> updatedOpt = getEmployeeAssessmentServiceV2().update(
                toUpdate.getId(),
                testAssessmentMatrix.getId(),
                testTeam.getId(),
                "John Updated Doe",
                "john.updated@example.com",
                "123456789",
                PersonDocumentType.CPF,
                Gender.MALE,
                GenderPronoun.HE
            );
            
            if (updatedOpt.isPresent()) {
                log.info("✓ V2 EmployeeAssessment updated: {} -> {}", 
                    toUpdate.getEmployee().getName(), updatedOpt.get().getEmployee().getName());
            }
        }
        
        return assessments;
    }
    
    private void demonstrateV2QueryCapabilities() {
        // Test GSI queries
        log.info("Testing V2 GSI query capabilities...");
        
        // Query by tenant
        List<EmployeeAssessmentV2> tenantAssessments = getEmployeeAssessmentServiceV2().findAllByTenantId(testTenantId);
        log.info("✓ Found {} assessments for tenant: {}", tenantAssessments.size(), testTenantId);
        
        // Query by assessment matrix
        List<EmployeeAssessmentV2> matrixAssessments = getEmployeeAssessmentServiceV2()
            .findByAssessmentMatrix(testAssessmentMatrix.getId(), testTenantId);
        log.info("✓ Found {} assessments for matrix: {}", matrixAssessments.size(), testAssessmentMatrix.getId());
        
        // Test existence query
        boolean exists = getEmployeeAssessmentServiceV2().getRepository()
            .existsByAssessmentMatrixAndEmployeeEmail(testAssessmentMatrix.getId(), "john.updated@example.com");
        log.info("✓ Employee exists check: {}", exists);
        
        // Test case-insensitive existence
        boolean existsCase = getEmployeeAssessmentServiceV2().getRepository()
            .existsByAssessmentMatrixAndEmployeeEmail(testAssessmentMatrix.getId(), "JOHN.UPDATED@EXAMPLE.COM");
        log.info("✓ Case-insensitive exists check: {}", existsCase);
    }
    
    private void demonstrateBusinessLogic(List<EmployeeAssessmentV2> assessments) {
        if (assessments.isEmpty()) {
            log.warn("No assessments available for business logic demo");
            return;
        }
        
        EmployeeAssessmentV2 assessment = assessments.get(0);
        String assessmentId = assessment.getId();
        
        // Test status transitions
        log.info("Testing assessment status transitions...");
        
        // Initial status should be INVITED
        log.info("Initial status: {}", assessment.getAssessmentStatus());
        
        // Transition to CONFIRMED
        Optional<EmployeeAssessmentV2> confirmedOpt = getEmployeeAssessmentServiceV2()
            .updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);
        if (confirmedOpt.isPresent()) {
            log.info("✓ Status transitioned to: {}", confirmedOpt.get().getAssessmentStatus());
        }
        
        // Test question count increment
        log.info("Testing question count increment...");
        log.info("Questions answered before: {}", assessment.getAnsweredQuestionCount());
        
        getEmployeeAssessmentServiceV2().incrementAnsweredQuestionCount(assessmentId);
        
        // Re-fetch to see changes
        Optional<EmployeeAssessmentV2> refetchedOpt = getEmployeeAssessmentServiceV2().findById(assessmentId);
        if (refetchedOpt.isPresent()) {
            EmployeeAssessmentV2 refetched = refetchedOpt.get();
            log.info("✓ Questions answered after increment: {}", refetched.getAnsweredQuestionCount());
            log.info("✓ Status after first question: {}", refetched.getAssessmentStatus());
        }
        
        // Test lastActivityDate update
        log.info("Testing lastActivityDate update...");
        getEmployeeAssessmentServiceV2().updateLastActivityDate(assessmentId);
        log.info("✓ LastActivityDate updated");
    }
    
    private void demonstrateEmployeeValidation() {
        log.info("Testing employee validation workflow...");
        
        // Test successful validation
        EmployeeValidationRequest request = new EmployeeValidationRequest();
        request.setAssessmentMatrixId(testAssessmentMatrix.getId());
        request.setTenantId(testTenantId);
        request.setEmail("john.updated@example.com");
        
        EmployeeValidationResponse response = getEmployeeAssessmentServiceV2().validateEmployee(request);
        log.info("✓ Validation result: Success={}, Message={}", "SUCCESS".equals(response.getStatus()), response.getMessage());
        log.info("  - Employee: {}, Status: {}", response.getName(), response.getAssessmentStatus());
        
        // Test validation with non-existent employee
        EmployeeValidationRequest notFoundRequest = new EmployeeValidationRequest();
        notFoundRequest.setAssessmentMatrixId(testAssessmentMatrix.getId());
        notFoundRequest.setTenantId(testTenantId);
        notFoundRequest.setEmail("nonexistent@example.com");
        
        EmployeeValidationResponse notFoundResponse = getEmployeeAssessmentServiceV2().validateEmployee(notFoundRequest);
        log.info("✓ Not found result: Success={}, Message={}", 
            "SUCCESS".equals(notFoundResponse.getStatus()), notFoundResponse.getMessage());
    }
    
    private void demonstrateSideBySideComparison() {
        try {
            // Create a V1 assessment for comparison
            Optional<com.agilecheckup.persistency.entity.EmployeeAssessment> v1Opt = 
                getEmployeeAssessmentServiceLegacy().create(
                    testAssessmentMatrix.getId(),
                    testTeam.getId(),
                    "V1 Test Employee",
                    "v1.employee@example.com",
                    "111111111",
                    PersonDocumentType.CPF,
                    Gender.MALE,
                    GenderPronoun.HE
                );
            
            // Get a V2 assessment
            List<EmployeeAssessmentV2> v2Assessments = getEmployeeAssessmentServiceV2().findAllByTenantId(testTenantId);
            
            if (v1Opt.isPresent() && !v2Assessments.isEmpty()) {
                com.agilecheckup.persistency.entity.EmployeeAssessment v1Assessment = v1Opt.get();
                EmployeeAssessmentV2 v2Assessment = v2Assessments.get(0);
                
                log.info("V1 EmployeeAssessment (Legacy):");
                log.info("  - ID: {}", v1Assessment.getId());
                log.info("  - Employee: {}", v1Assessment.getEmployee().getName());
                log.info("  - Email normalized: {}", v1Assessment.getEmployeeEmailNormalized());
                log.info("  - SDK Version: AWS SDK V1 (DynamoDBMapper)");
                log.info("  - Query Method: Table scan with filtering");
                
                log.info("V2 EmployeeAssessment (Enhanced):");
                log.info("  - ID: {}", v2Assessment.getId());
                log.info("  - Employee: {}", v2Assessment.getEmployee().getName());
                log.info("  - Email normalized: {}", v2Assessment.getEmployeeEmailNormalized());
                log.info("  - SDK Version: AWS SDK V2 Enhanced Client");
                log.info("  - Query Method: Efficient GSI queries");
                log.info("  - Attribute Converters: Custom JSON converters for complex types");
                
                // Cleanup V1 assessment
                getEmployeeAssessmentServiceLegacy().deleteById(v1Assessment.getId());
            }
        } catch (Exception e) {
            log.warn("Could not demonstrate V1 comparison: {}", e.getMessage());
            log.info("V2 EmployeeAssessment Features:");
            log.info("  - AWS SDK V2 Enhanced Client with type-safe operations");
            log.info("  - Efficient GSI queries for existence checks and filtering");
            log.info("  - Custom attribute converters for complex types");
            log.info("  - Preserved business logic and method signatures");
            log.info("  - Enhanced error handling and logging");
        }
    }
    
    private void cleanupTestData(List<EmployeeAssessmentV2> assessments) {
        // Clean V2 assessments
        for (EmployeeAssessmentV2 assessment : assessments) {
            try {
                getEmployeeAssessmentServiceV2().deleteById(assessment.getId());
                log.info("✓ Cleaned up V2 assessment: {}", assessment.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup V2 assessment {}: {}", assessment.getId(), e.getMessage());
            }
        }
        
        // Clean any remaining test assessments from the tenant
        try {
            List<EmployeeAssessmentV2> remainingAssessments = getEmployeeAssessmentServiceV2().findAllByTenantId(testTenantId);
            for (EmployeeAssessmentV2 assessment : remainingAssessments) {
                getEmployeeAssessmentServiceV2().deleteById(assessment.getId());
                log.info("✓ Cleaned up remaining V2 assessment: {}", assessment.getId());
            }
        } catch (Exception e) {
            log.warn("Error cleaning remaining assessments: {}", e.getMessage());
        }
    }
    
    private void cleanupTestDependencies() {
        // Cleanup in reverse order of creation
        if (testAssessmentMatrix != null) {
            try {
                getAssessmentMatrixServiceV2().deleteById(testAssessmentMatrix.getId());
                log.info("✓ Cleaned up test assessment matrix: {}", testAssessmentMatrix.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup test assessment matrix {}: {}", testAssessmentMatrix.getId(), e.getMessage());
            }
        }
        
        if (testPerformanceCycle != null) {
            try {
                getPerformanceCycleService().deleteById(testPerformanceCycle.getId());
                log.info("✓ Cleaned up test performance cycle: {}", testPerformanceCycle.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup test performance cycle {}: {}", testPerformanceCycle.getId(), e.getMessage());
            }
        }
        
        if (testTeam != null) {
            try {
                getTeamService().delete(testTeam);
                log.info("✓ Cleaned up test team: {}", testTeam.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup test team {}: {}", testTeam.getId(), e.getMessage());
            }
        }
        
        if (testDepartment != null) {
            try {
                getDepartmentService().deleteById(testDepartment.getId());
                log.info("✓ Cleaned up test department: {}", testDepartment.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup test department {}: {}", testDepartment.getId(), e.getMessage());
            }
        }
        
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
    private EmployeeAssessmentServiceV2 getEmployeeAssessmentServiceV2() {
        if (employeeAssessmentServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            employeeAssessmentServiceV2 = serviceComponent.buildEmployeeAssessmentServiceV2();
        }
        return employeeAssessmentServiceV2;
    }
    
    private EmployeeAssessmentService getEmployeeAssessmentServiceLegacy() {
        if (employeeAssessmentServiceLegacy == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            employeeAssessmentServiceLegacy = serviceComponent.buildEmployeeAssessmentService();
        }        
        return employeeAssessmentServiceLegacy;
    }
    
    private AssessmentMatrixServiceV2 getAssessmentMatrixServiceV2() {
        if (assessmentMatrixServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            assessmentMatrixServiceV2 = serviceComponent.buildAssessmentMatrixServiceV2();
        }
        return assessmentMatrixServiceV2;
    }
    
    private TeamService getTeamService() {
        if (teamService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            teamService = serviceComponent.buildTeamServiceLegacy();
        }
        return teamService;
    }
    
    private CompanyServiceV2 getCompanyService() {
        if (companyServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            companyServiceV2 = serviceComponent.buildCompanyService();
        }
        return companyServiceV2;
    }
    
    private DepartmentServiceV2 getDepartmentService() {
        if (departmentServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            departmentServiceV2 = serviceComponent.buildDepartmentService();
        }
        return departmentServiceV2;
    }
    
    private PerformanceCycleServiceV2 getPerformanceCycleService() {
        if (performanceCycleServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            performanceCycleServiceV2 = serviceComponent.buildPerformanceCycleService();
        }
        return performanceCycleServiceV2;
    }
}