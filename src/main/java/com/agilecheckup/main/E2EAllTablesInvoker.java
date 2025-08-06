package com.agilecheckup.main;

import com.agilecheckup.main.runner.AnswerV2TableRunner;
import com.agilecheckup.main.runner.AssessmentMatrixTableRunnerV2;
import com.agilecheckup.main.runner.CrudRunner;
import com.agilecheckup.main.runner.DashboardAnalyticsV2TableRunner;
import com.agilecheckup.main.runner.EmployeeAssessmentTableRunnerV2;
import com.agilecheckup.main.runner.QuestionTableRunnerV2;
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
//        new AssessmentMatrixTableRunnerV2(true),
//        new EmployeeAssessmentTableRunnerV2(true)
//        new QuestionTableRunnerV2(true)
//        new AnswerV2TableRunner(true),
        new DashboardAnalyticsV2TableRunner(true)
    );
  }

}
