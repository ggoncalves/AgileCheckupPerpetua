package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.NonNull;

import javax.inject.Inject;
import java.util.Optional;

public class EmployeeAssessmentService extends AbstractCrudService<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> {

  private final AssessmentMatrixService assessmentMatrixService;

  private final TeamService teamService;

  private final EmployeeAssessmentRepository employeeAssessmentRepository;

  @Inject
  public EmployeeAssessmentService(EmployeeAssessmentRepository employeeAssessmentRepository, AssessmentMatrixService assessmentMatrixService, TeamService teamService) {
    this.employeeAssessmentRepository = employeeAssessmentRepository;
    this.assessmentMatrixService = assessmentMatrixService;
    this.teamService = teamService;
  }

  public Optional<EmployeeAssessment> create(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun) {
    return super.create(createEmployeeAssessment(assessmentMatrixId, teamId, name, email, documentNumber, documentType, gender, genderPronoun, null));
  }

  private EmployeeAssessment createEmployeeAssessment(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun, String personId) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    Optional<Team> team = teamService.findById(teamId);
    EmployeeAssessment employeeAssessment = EmployeeAssessment.builder()
        .assessmentMatrixId(assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix")).getId())
        .team(team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team")))
        .employee(createNaturalPerson(name, email, documentNumber, documentType, gender, genderPronoun, personId))
        .answeredQuestionCount(0)
        .build();
    return setFixedIdIfConfigured(employeeAssessment);
  }

  public static NaturalPerson createNaturalPerson(String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun, String personId) {
    return NaturalPerson.builder()
        .id(personId)
        .name(name)
        .email(email)
        .documentNumber(documentNumber)
        .personDocumentType(documentType)
        .gender(gender)
        .genderPronoun(genderPronoun)
        .build();
  }

  public void incrementAnsweredQuestionCount(String employeeAssessmentId) {
    EmployeeAssessment employeeAssessment = getRepository().findById(employeeAssessmentId);
    if (employeeAssessment != null) {
      employeeAssessment.setAnsweredQuestionCount(employeeAssessment.getAnsweredQuestionCount() + 1);
      getRepository().save(employeeAssessment);
    }
  }

  @Override
  AbstractCrudRepository<EmployeeAssessment> getRepository() {
    return employeeAssessmentRepository;
  }
}