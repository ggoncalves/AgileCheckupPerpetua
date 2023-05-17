package com.agilecheckup.main;

import com.agilecheckup.main.runner.CommandTableRunner;
import com.agilecheckup.service.QuestionService;
import lombok.extern.log4j.Log4j2;

import java.util.Set;

@Log4j2
public class PerpetuaMain {

  private static QuestionService questionService;

  private Set<CommandTableRunner> tableRunnerSet;

  public static void main(String[] args) {
    log.info("Initializing PerpetuaMain Test");
    E2EAllTablesInvoker e2EAllTablesInvoker = new E2EAllTablesInvoker();
    e2EAllTablesInvoker.invoke();
    System.exit(1);
  }
}
