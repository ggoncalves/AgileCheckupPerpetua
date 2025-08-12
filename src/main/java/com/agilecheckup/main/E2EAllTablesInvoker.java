package com.agilecheckup.main;

import java.util.Set;

import com.agilecheckup.main.runner.CrudRunner;
import com.agilecheckup.main.runner.DashboardAnalyticsTableRunner;
import com.google.common.collect.ImmutableSet;

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
      //        new PerformanceCycleTableRunner(true),
//        new AssessmentMatrixTableRunner(true),
//        new EmployeeAssessmentTableRunner(true)
//        new QuestionTableRunner(true)
//        new AnswerTableRunner(true),
        new DashboardAnalyticsTableRunner(true)
    );
  }

}
