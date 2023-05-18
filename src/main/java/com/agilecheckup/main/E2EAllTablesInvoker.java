package com.agilecheckup.main;

import com.agilecheckup.main.runner.CommandTableRunner;
import com.agilecheckup.main.runner.CompanyTableRunner;
import com.agilecheckup.main.runner.QuestionTableRunner;
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
        createQuestionTableRunner(true),
        createCompanyTableRunner(true)
    );
  }

  CommandTableRunner createQuestionTableRunner(final boolean mustDelete) {
    return new QuestionTableRunner(mustDelete);
  }

  CommandTableRunner createCompanyTableRunner(final boolean mustDelete) {
    return new CompanyTableRunner(mustDelete);
  }
}
