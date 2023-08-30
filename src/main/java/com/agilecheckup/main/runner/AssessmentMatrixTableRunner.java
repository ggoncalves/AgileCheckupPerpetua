package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Log4j2
public class AssessmentMatrixTableRunner extends AbstractEntityCrudRunner<AssessmentMatrix> {

  private AssessmentMatrixService assessmentMatrixService;

  public AssessmentMatrixTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Collection<Supplier<Optional<AssessmentMatrix>>> getCreateSupplier() {
    Category c1 = Category.builder()
        .name("Categoria de Nome 1")
        .description("Categoria de Descrição 1")
        .build();

    Category c2 = Category.builder()
        .name("Categoria de Nome 2")
        .description("Categoria de Descrição 2")
        .build();

    Set<Category> categories1 = ImmutableSet.of(c1, c2);

    Pillar p1 = Pillar.builder()
        .name("Pillar name 1")
        .description("Pillar description 1 ")
        .categories(categories1)
        .build();

    Category c3 = Category.builder()
        .name("Categoria de Nome 3")
        .description("Categoria de Descrição 3")
        .build();

    Category c4 = Category.builder()
        .name("Categoria de Nome 4")
        .description("Categoria de Descrição 4")
        .build();

    Set<Category> categories2 = ImmutableSet.of(c3, c4);

    Pillar p2 = Pillar.builder()
        .name("Pillar name 2")
        .description("Pillar description 2 ")
        .categories(categories2)
        .build();

    Set<Pillar> pillars = ImmutableSet.of(p1, p2);

    Collection<Supplier<Optional<AssessmentMatrix>>> collection = new ArrayList<>();
    collection.add(() -> getAssessmentMatrixService().create(
        "AssessmentMatrixName",
        "AssessmentMatrix Description",
        "Another TenantId",
        "321c5be6-9534-4b7c-9919-2f4418900935",
        pillars
    ));
    return collection;
  }

  @Override
  protected AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> getCrudService() {
    return getAssessmentMatrixService();
  }

  @Override
  protected void verifySavedEntity(AssessmentMatrix savedEntity, AssessmentMatrix fetchedEntity) {
    // Do nothing
  }

  private AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }
}
