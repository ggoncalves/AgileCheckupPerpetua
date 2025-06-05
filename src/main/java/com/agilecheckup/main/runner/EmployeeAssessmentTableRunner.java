package com.agilecheckup.main.runner;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

@Log4j2
public class EmployeeAssessmentTableRunner extends AbstractEntityCrudRunner<EmployeeAssessment> {

  public static final String TENANT_ID = "9c4505d9-2241-45c0-9eda-ddb85d8c5608";

  private final List<Optional<Question>> questions = new ArrayList<>();

  private final List<Optional<Answer>> answers = new ArrayList<>();

  private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();

  private final List<Optional<AssessmentMatrix>> assessmentMatrices = new ArrayList<>();
  private Map<String, Pillar> pillarMap;

  public EmployeeAssessmentTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<EmployeeAssessment>>> getCreateSupplier() {
    Optional<AssessmentMatrix> assessmentMatrix = createAssessmentMatrix();
    assessmentMatrices.add(assessmentMatrix);
    Collection<Supplier<Optional<EmployeeAssessment>>> collection = new ArrayList<>();

      assessmentMatrix.ifPresent(am -> collection.add(() -> tableRunnerHelper.getEmployeeAssessmentService().create(
          am.getId(),
          am.getTenantId(),
          "Josefa Santos de Souza",
          "josefa.santos@gmail.com",
          "97766392873",
          PersonDocumentType.CPF,
          Gender.MALE,
          GenderPronoun.HE)
      ));

    return collection;
  }

  @Override
  protected AbstractCrudService<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> getCrudService() {
    return tableRunnerHelper.getEmployeeAssessmentService();
  }

  private Optional<AssessmentMatrix> createAssessmentMatrix() {
    return tableRunnerHelper.getAssessmentMatrixService().create(
        "AssessmentMatrixName",
        "AssessmentMatrix Description",
        TENANT_ID,
        "321c5be6-9534-4b7c-9919-2f4418900935",
        getPillarMap()
    );
  }

  @Override
  protected void postCreate(Collection<EmployeeAssessment> entities) {
    entities.forEach(entity -> {
      createDependencies(entity);
      tableRunnerHelper.getEmployeeAssessmentService().updateEmployeeAssessmentScore(entity.getId(), TENANT_ID);
    });
  }

  private void createDependencies(EmployeeAssessment entity) {

    getPillarMap().forEach((pillarId, pillar) -> pillar.getCategoryMap().forEach((categoryId, category) -> {
      Optional<Question> question = createQuestion(entity, pillar, category);
      question.ifPresent(q -> createAnswer(entity, q));
    }));
  }

  private Map<String, Pillar> getPillarMap() {
    if (pillarMap == null) {
      pillarMap = tableRunnerHelper.createPillarsWithCategoriesMap();
    }
    return pillarMap;
  }

  private Optional<Question> createQuestion(EmployeeAssessment entity, Pillar pillar, Category category) {
    Optional<Question> question = tableRunnerHelper.getQuestionService().create(
        "Pergunta oficial",
        QuestionType.STAR_THREE,
        TENANT_ID,
        15d,
        entity.getAssessmentMatrixId(),
        pillar.getId(),
        category.getId(),
        "Extra description"
    );
    questions.add(question);
    return question;
  }

  private void createAnswer(EmployeeAssessment entity, Question question) {
    Optional<Answer> answer = tableRunnerHelper.getAnswerService().create(entity.getId(), question.getId(),
        LocalDateTime.now(), "2"
        , EmployeeAssessmentTableRunner.TENANT_ID, "");
    answers.add(answer);
  }

  @Override
  protected void verifySavedEntity(EmployeeAssessment savedEntity, EmployeeAssessment fetchedEntity) {

  }

  @Override
  protected void deleteDependencies() {
    super.deleteDependencies();
    deleteQuestions();
    deleteAnswers();
    deleteAssessmentMatrices();
  }

  private void deleteAnswers() {
    answers.forEach(answer -> answer.ifPresent(tableRunnerHelper.getAnswerService()::delete));
  }

  private void deleteQuestions() {
    questions.forEach(question -> question.ifPresent(tableRunnerHelper.getQuestionService()::delete));
  }

  private void deleteAssessmentMatrices() {
    assessmentMatrices.forEach(matrix -> matrix.ifPresent(tableRunnerHelper.getAssessmentMatrixService()::delete));
  }
}
