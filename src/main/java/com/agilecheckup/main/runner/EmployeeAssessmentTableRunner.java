package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.EmployeeAssessmentService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class EmployeeAssessmentTableRunner extends AbstractEntityCrudRunner<EmployeeAssessment> {

  private EmployeeAssessmentService employeeAssessmentService;

  public EmployeeAssessmentTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Collection<Supplier<Optional<EmployeeAssessment>>> getCreateSupplier() {
    Collection<Supplier<Optional<EmployeeAssessment>>> collection = new ArrayList<>();
    collection.add(() -> getEmployeeAssessmentService().create(
        "cce53ec6-3253-4f8f-b842-c83d3631efbe",
        "9c4505d9-2241-45c0-9eda-ddb85d8c5608",
        "Josefa Santos de Souza",
        "josefa.santos@gmail.com",
        "97766392873",
        PersonDocumentType.CPF,
        Gender.MALE,
        GenderPronoun.HE)
    );
    return collection;
  }

  @Override
  protected AbstractCrudService<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> getCrudService() {
    return getEmployeeAssessmentService();
  }

  @Override
  protected void verifySavedEntity(EmployeeAssessment savedEntity, EmployeeAssessment fetchedEntity) {

  }

  private EmployeeAssessmentService getEmployeeAssessmentService() {
    if (employeeAssessmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentService = serviceComponent.buildEmployeeAssessmentService();
    }
    return employeeAssessmentService;
  }
}
