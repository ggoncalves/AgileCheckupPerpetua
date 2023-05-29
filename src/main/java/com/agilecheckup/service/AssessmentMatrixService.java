package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

public class AssessmentMatrixService extends AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> {

  private final AssessmentMatrixRepository assessmentMatrixRepository;

  private final PerformanceCycleService performanceCycleService;

  @Inject
  public AssessmentMatrixService(AssessmentMatrixRepository assessmentMatrixRepository, PerformanceCycleService performanceCycleService) {
    this.assessmentMatrixRepository = assessmentMatrixRepository;
    this.performanceCycleService = performanceCycleService;
  }

  public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId,
                                           Set<Pillar> pillars) {
    return super.create(AssessmentMatrix.builder()
        .name(name)
        .description(description)
        .tenantId(tenantId)
        .performanceCycle(getPerformanceCycle(performanceCycleId))
        .pillars(pillars)
        .build());
  }

  private PerformanceCycle getPerformanceCycle(String performanceCycleId) {
    if (performanceCycleId == null) return null;
    return performanceCycleService.findById(performanceCycleId).orElse(null);
  }

  @Override
  AbstractCrudRepository<AssessmentMatrix> getRepository() {
    return assessmentMatrixRepository;
  }
}