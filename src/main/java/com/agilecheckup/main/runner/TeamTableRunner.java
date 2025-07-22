package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.TeamService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class TeamTableRunner extends AbstractEntityCrudRunner<Team> {

  private TeamService teamService;
  private DepartmentService departmentService;
  private CompanyService companyService;
  private String testDepartmentId;
  private String testTenantId;

  public TeamTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
    setupTestData();
  }

  private void setupTestData() {
    ServiceComponent serviceComponent = DaggerServiceComponent.create();
    companyService = serviceComponent.buildCompanyService();
    departmentService = serviceComponent.buildDepartmentService();
    
    // Create test company
    Optional<Company> companyOpt = companyService.create(
        "12345678000100",  // documentNumber
        "Test Company for Teams",  // name
        "test@company.com",  // email
        "Test Company Description",  // description
        "team-test-tenant-id"  // tenantId
    );
    
    if (companyOpt.isPresent()) {
      Company company = companyOpt.get();
      testTenantId = company.getTenantId();
      
      // Create test department
      // Note: departmentService now returns DepartmentV2, but for compatibility
      // we'll handle the type change here
      var departmentOpt = departmentService.create(
          "Test Department for Teams",
          "Test Department Description",
          testTenantId,
          company.getId()
      );
      
      if (departmentOpt.isPresent()) {
        testDepartmentId = departmentOpt.get().getId();
        log.info("Created test department with ID: " + testDepartmentId);
      } else {
        log.error("Failed to create test department");
      }
    } else {
      log.error("Failed to create test company");
    }
  }

  @Override
  protected Collection<Supplier<Optional<Team>>> getCreateSupplier() {
    Collection<Supplier<Optional<Team>>> collection = new ArrayList<>();
    
    if (testDepartmentId != null && testTenantId != null) {
      collection.add(() -> getTeamService().create("Team Alpha", "First test team", testTenantId, testDepartmentId));
      collection.add(() -> getTeamService().create("Team Beta", "Second test team", testTenantId, testDepartmentId));
    } else {
      log.error("Test data not properly initialized, cannot create teams");
    }
    
    return collection;
  }

  @Override
  protected AbstractCrudService<Team, AbstractCrudRepository<Team>> getCrudService() {
    return getTeamService();
  }

  @Override
  protected void verifySavedEntity(Team savedEntity, Team fetchedEntity) {
    // Do nothing
  }

  private TeamService getTeamService() {
    if (teamService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      teamService = serviceComponent.buildTeamService();
    }
    return teamService;
  }
}
