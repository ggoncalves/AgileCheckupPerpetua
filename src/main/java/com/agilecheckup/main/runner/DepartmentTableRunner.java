package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.DepartmentService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class DepartmentTableRunner extends AbstractEntityCrudRunner<Department> {

  private DepartmentService departmentService;

  public DepartmentTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Collection<Supplier<Optional<Department>>> getCreateSupplier() {
    Collection<Supplier<Optional<Department>>> collection = new ArrayList<>();
    collection.add(() -> getDepartmentService().create("DepartmentName", "Department Description", "Another TenantId", "19bcdfcb-9162-4a0b-a5c3-c034702d0961"));
    return collection;
  }

  @Override
  protected AbstractCrudService<Department, AbstractCrudRepository<Department>> getCrudService() {
    return getDepartmentService();
  }

  @Override
  protected void verifySavedEntity(Department savedEntity, Department fetchedEntity) {
    // Do nothing
  }

  private DepartmentService getDepartmentService() {
    if (departmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      departmentService = serviceComponent.buildDepartmentService();
    }
    return departmentService;
  }
}
