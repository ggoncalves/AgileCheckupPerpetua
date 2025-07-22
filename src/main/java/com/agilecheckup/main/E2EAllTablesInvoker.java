package com.agilecheckup.main;

import com.agilecheckup.main.runner.CrudRunner;
import com.agilecheckup.main.runner.DepartmentTableRunner;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class E2EAllTablesInvoker {

  private Set<CrudRunner> tableRunnerSet;

  E2EAllTablesInvoker() {
    initTableRunnerSet();
  }

  protected void invoke() {
    tableRunnerSet.forEach(CrudRunner::run);
  }

  void initTableRunnerSet() {
    tableRunnerSet = ImmutableSet.of(
//        new AnswerTableRunner(true),
//        new QuestionTableRunner(true),
//        new CompanyTableRunner(true),
        new DepartmentTableRunner(true)
//        new TeamTableRunner(true),
//        new PerformanceCycleTableRunner(true),
//        new AssessmentMatrixTableRunner(true),
//        new EmployeeAssessmentTableRunner(true)
    );
  }

}
