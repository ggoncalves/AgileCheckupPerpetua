package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.service.DepartmentServiceV2;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class DepartmentTableRunner implements CrudRunner {

  private DepartmentServiceV2 departmentServiceV2;
  private final boolean shouldCleanAfterComplete;

  public DepartmentTableRunner(boolean shouldCleanAfterComplete) {
    this.shouldCleanAfterComplete = shouldCleanAfterComplete;
  }

  @Override
  public void run() {
    log.info("Starting DepartmentTableRunner with V2 implementation");
    
    // Create departments
    List<DepartmentV2> createdDepartments = createDepartments();
    
    // Fetch and verify departments
    fetchAndVerifyDepartments(createdDepartments);
    
    // List all departments
    listAllDepartments();
    
    // Clean up if requested
    if (shouldCleanAfterComplete) {
      cleanupDepartments(createdDepartments);
    }
    
    log.info("DepartmentTableRunner completed successfully");
  }

  private List<DepartmentV2> createDepartments() {
    log.info("Creating test departments...");
    List<DepartmentV2> departments = new ArrayList<>();
    
    Optional<DepartmentV2> dept1 = getDepartmentService().create(
        "Engineering", 
        "Engineering Department", 
        "test-tenant-123", 
        "ca93d066-62af-4a49-aa46-0dd7f33ba9bc"
    );
    
    Optional<DepartmentV2> dept2 = getDepartmentService().create(
        "Marketing", 
        "Marketing Department", 
        "test-tenant-123", 
        "ca93d066-62af-4a49-aa46-0dd7f33ba9bc"
    );
    
    if (dept1.isPresent()) {
      departments.add(dept1.get());
      log.info("Created department: {}", dept1.get().getName());
    }
    
    if (dept2.isPresent()) {
      departments.add(dept2.get());
      log.info("Created department: {}", dept2.get().getName());
    }
    
    return departments;
  }

  private void fetchAndVerifyDepartments(List<DepartmentV2> departments) {
    log.info("Fetching and verifying departments...");
    for (DepartmentV2 dept : departments) {
      Optional<DepartmentV2> fetched = getDepartmentService().findById(dept.getId());
      if (fetched.isPresent()) {
        log.info("Verified department: {} - {}", fetched.get().getId(), fetched.get().getName());
      } else {
        log.warn("Could not fetch department with ID: {}", dept.getId());
      }
    }
  }

  private void listAllDepartments() {
    log.info("Listing all departments by tenant...");
    List<DepartmentV2> allDepartments = getDepartmentService().findAllByTenantId("test-tenant-123");
    log.info("Found {} departments", allDepartments.size());
    allDepartments.forEach(dept -> log.info("Department: {} - {}", dept.getId(), dept.getName()));
  }

  private void cleanupDepartments(List<DepartmentV2> departments) {
    log.info("Cleaning up test departments...");
    for (DepartmentV2 dept : departments) {
      boolean deleted = getDepartmentService().deleteById(dept.getId());
      if (deleted) {
        log.info("Deleted department: {}", dept.getName());
      } else {
        log.warn("Failed to delete department: {}", dept.getName());
      }
    }
  }

  private DepartmentServiceV2 getDepartmentService() {
    if (departmentServiceV2 == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      departmentServiceV2 = serviceComponent.buildDepartmentService();
    }
    return departmentServiceV2;
  }
}