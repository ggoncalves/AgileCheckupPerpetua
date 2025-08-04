package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.DepartmentServiceV2;
import com.agilecheckup.service.TeamServiceV2;
import com.agilecheckup.service.TeamService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class TeamTableRunner implements CrudRunner {

    private TeamServiceV2 teamServiceV2;
    private TeamService teamService;
    private CompanyServiceV2 companyServiceV2;
    private DepartmentServiceV2 departmentServiceV2;
    private final boolean shouldCleanAfterComplete;
    
    // Test data
    private String testTenantId;
    private String testDepartmentId;
    private CompanyV2 testCompany;
    private DepartmentV2 testDepartment;

    public TeamTableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("========================================");
        log.info("Starting TeamTableRunner - V1 to V2 Migration Test Suite");
        log.info("========================================");
        
        try {
            // 0. Setup test data (Company and Department)
            setupTestData();
            
            if (testDepartmentId == null) {
                log.error("Failed to setup test data. Exiting TeamTableRunner.");
                return;
            }
            
            // 1. Test V2 Creation (New teams)
            log.info("\n1. Testing V2 Team Creation...");
            List<TeamV2> v2Teams = testV2Creation();
            
            // 2. Test V2 Updates
            log.info("\n2. Testing V2 Team Updates...");
            testV2Updates(v2Teams);
            
            // 3. Fetch all existing teams (should include V1 teams if any)
            log.info("\n3. Fetching all existing teams (V1 + V2)...");
            List<TeamV2> allTeams = getTeamService().findAllByTenantId(testTenantId);
            log.info("Total teams found: {}", allTeams.size());
            allTeams.forEach(team -> log.info("  - {}: {} (Department: {})", 
                team.getId(), team.getName(), team.getDepartmentId()));
            
            // 4. Test finding teams by department
            log.info("\n4. Testing find teams by department...");
            List<TeamV2> departmentTeams = getTeamService().findByDepartmentId(testDepartmentId);
            log.info("Found {} teams in department {}", departmentTeams.size(), testDepartmentId);
            
            // 5. Test V1 creation for migration demonstration
            log.info("\n5. Creating V1 team for migration demonstration...");
            Optional<Team> v1Team = createV1TeamForMigration();
            
            // 6. Demonstrate V1 to V2 update
            if (v1Team.isPresent()) {
                log.info("\n6. Demonstrating V1 to V2 update...");
                demonstrateV1ToV2Update(v1Team.get());
            }
            
            // 7. Cleanup test data if needed
            if (shouldCleanAfterComplete) {
                log.info("\n7. Cleaning up test data...");
                cleanupTestData(v2Teams);
            }
            
        } catch (Exception e) {
            log.error("Error in TeamTableRunner: {}", e.getMessage(), e);
        } finally {
            // Always cleanup test company and department
            cleanupTestCompanyAndDepartment();
        }
        
        log.info("\n=== Team V1-to-V2 Migration Demo Complete ===");
    }
    
    private void setupTestData() {
        log.info("Setting up test data (Company and Department)...");
        
        // Create test company
        Optional<CompanyV2> companyOpt = getCompanyService().create(
            "98765432000198",  // documentNumber
            "Team Test Company",  // name
            "team-test@company.com",  // email
            "Company for Team V2 testing",  // description
            "team-test-tenant-" + System.currentTimeMillis()  // unique tenantId
        );
        
        if (companyOpt.isPresent()) {
            testCompany = companyOpt.get();
            testTenantId = testCompany.getTenantId();
            log.info("Created test company: {} (Tenant: {})", testCompany.getName(), testTenantId);
            
            // Create test department
            Optional<DepartmentV2> deptOpt = getDepartmentService().create(
                testTenantId,
                "Engineering Department",
                "Department for Team V2 testing",
                testCompany.getId()
            );
            
            if (deptOpt.isPresent()) {
                testDepartment = deptOpt.get();
                testDepartmentId = testDepartment.getId();
                log.info("Created test department: {} (ID: {})", testDepartment.getName(), testDepartmentId);
            } else {
                log.error("Failed to create test department");
            }
        } else {
            log.error("Failed to create test company");
        }
    }
    
    private List<TeamV2> testV2Creation() {
        List<TeamV2> createdTeams = new ArrayList<>();
        
        // Create teams with V2 service
        TeamV2[] teamsToCreate = {
            TeamV2.builder()
                .tenantId(testTenantId)
                .name("Backend Team")
                .description("Backend development team")
                .departmentId(testDepartmentId)
                .build(),
            TeamV2.builder()
                .tenantId(testTenantId)
                .name("Frontend Team")
                .description("Frontend development team")
                .departmentId(testDepartmentId)
                .build(),
            TeamV2.builder()
                .tenantId(testTenantId)
                .name("DevOps Team")
                .description("DevOps and infrastructure team")
                .departmentId(testDepartmentId)
                .build()
        };
        
        for (TeamV2 teamData : teamsToCreate) {
            try {
                Optional<TeamV2> created = getTeamService().create(
                    teamData.getTenantId(),
                    teamData.getName(),
                    teamData.getDescription(),
                    teamData.getDepartmentId()
                );
                
                if (created.isPresent()) {
                    createdTeams.add(created.get());
                    log.info("✓ Created V2 team: {} (ID: {})", created.get().getName(), created.get().getId());
                } else {
                    log.error("✗ Failed to create team: {}", teamData.getName());
                }
            } catch (Exception e) {
                log.error("✗ Error creating team {}: {}", teamData.getName(), e.getMessage());
            }
        }
        
        return createdTeams;
    }
    
    private void testV2Updates(List<TeamV2> teams) {
        if (teams.isEmpty()) {
            log.warn("No teams to update");
            return;
        }
        
        // Update first team
        TeamV2 firstTeam = teams.get(0);
        try {
            Optional<TeamV2> updated = getTeamService().update(
                firstTeam.getId(),
                firstTeam.getTenantId(),
                "Backend Team (Updated)",
                "Updated backend development team with new responsibilities",
                firstTeam.getDepartmentId()
            );
            
            if (updated.isPresent()) {
                log.info("✓ Updated team: {} -> {}", firstTeam.getName(), updated.get().getName());
            } else {
                log.error("✗ Failed to update team: {}", firstTeam.getName());
            }
        } catch (Exception e) {
            log.error("✗ Error updating team: {}", e.getMessage());
        }
    }
    
    private Optional<Team> createV1TeamForMigration() {
        try {
            // Using legacy service to create V1 team
            Optional<Team> v1Team = getTeamServiceLegacy().create(
                "Legacy Team",
                "Team created with V1 service",
                testTenantId,
                testDepartmentId
            );
            
            if (v1Team.isPresent()) {
                log.info("✓ Created V1 team: {} (ID: {})", v1Team.get().getName(), v1Team.get().getId());
            } else {
                log.error("✗ Failed to create V1 team");
            }
            
            return v1Team;
        } catch (Exception e) {
            log.error("✗ Error creating V1 team: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private void demonstrateV1ToV2Update(Team v1Team) {
        log.info("Attempting to update V1 team '{}' using V2 service...", v1Team.getName());
        
        try {
            // Update V1 team using V2 service
            Optional<TeamV2> updated = getTeamService().update(
                v1Team.getId(),
                v1Team.getTenantId(),
                "Legacy Team (Migrated to V2)",
                "This team was migrated from V1 to V2",
                v1Team.getDepartmentId()
            );
            
            if (updated.isPresent()) {
                log.info("✓ Successfully updated V1 team to V2!");
                log.info("  Updated team: {} (ID: {})", updated.get().getName(), updated.get().getId());
                log.info("  This demonstrates that V1 teams can be updated through V2 service");
            } else {
                log.warn("✗ Could not update V1 team through V2 service");
                log.warn("  This indicates that a migration might be needed");
            }
        } catch (Exception e) {
            log.error("✗ Error updating V1 team: {}", e.getMessage());
            log.info("Note: Existing V1 entities may need to be migrated to V2 format.");
        }
    }
    
    private void cleanupTestData(List<TeamV2> teams) {
        log.info("Cleaning up {} test teams...", teams.size());
        
        for (TeamV2 team : teams) {
            try {
                boolean deleted = getTeamService().deleteById(team.getId());
                if (deleted) {
                    log.info("✓ Deleted test team: {}", team.getName());
                } else {
                    log.warn("✗ Failed to delete test team: {}", team.getName());
                }
            } catch (Exception e) {
                log.error("✗ Error deleting test team {}: {}", team.getName(), e.getMessage());
            }
        }
    }
    
    private void cleanupTestCompanyAndDepartment() {
        log.info("Cleaning up test company and department...");
        
        // Delete test department
        if (testDepartment != null) {
            try {
                boolean deleted = getDepartmentService().deleteById(testDepartment.getId());
                if (deleted) {
                    log.info("✓ Deleted test department: {}", testDepartment.getName());
                } else {
                    log.warn("✗ Failed to delete test department: {}", testDepartment.getName());
                }
            } catch (Exception e) {
                log.error("✗ Error deleting test department: {}", e.getMessage());
            }
        }
        
        // Delete test company
        if (testCompany != null) {
            try {
                boolean deleted = getCompanyService().deleteById(testCompany.getId());
                if (deleted) {
                    log.info("✓ Deleted test company: {}", testCompany.getName());
                } else {
                    log.warn("✗ Failed to delete test company: {}", testCompany.getName());
                }
            } catch (Exception e) {
                log.error("✗ Error deleting test company: {}", e.getMessage());
            }
        }
    }

    private TeamServiceV2 getTeamService() {
        if (teamServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            teamServiceV2 = serviceComponent.buildTeamService();
        }
        return teamServiceV2;
    }
    
    private TeamService getTeamServiceLegacy() {
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
}