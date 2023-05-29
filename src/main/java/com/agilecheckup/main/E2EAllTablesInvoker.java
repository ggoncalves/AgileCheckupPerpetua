package com.agilecheckup.main;

import com.agilecheckup.main.runner.*;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class E2EAllTablesInvoker {

  private Set<CommandTableRunner> tableRunnerSet;

  E2EAllTablesInvoker() {
    initTableRunnerSet();
  }

  protected void invoke() {
    tableRunnerSet.forEach(CommandTableRunner::run);
  }

  void initTableRunnerSet() {
    tableRunnerSet = ImmutableSet.of(
        new QuestionTableRunner(true),
        new CompanyTableRunner(true),
        new DepartmentTableRunner(true),
        new TeamTableRunner(true),
        new PerformanceCycleTableRunner(true),
        new AssessmentMatrixTableRunner(true)
    );
  }

}
