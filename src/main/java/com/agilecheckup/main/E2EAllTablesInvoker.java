package com.agilecheckup.main;

import com.agilecheckup.main.runner.AssessmentMatrixTableRunner;
import com.agilecheckup.main.runner.AssessmentMatrixTableRunnerV2;
import com.agilecheckup.main.runner.CompanyTableRunner;
import com.agilecheckup.main.runner.CrudRunner;
import com.agilecheckup.main.runner.PerformanceCycleTableRunner;
import com.agilecheckup.main.runner.TeamTableRunner;
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
//        new PerformanceCycleTableRunner(true),
        new AssessmentMatrixTableRunnerV2(true)
    );
  }

}
