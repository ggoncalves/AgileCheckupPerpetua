package com.agilecheckup.main;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PerpetuaMain {

  public static void main(String[] args) {
    log.info("Initializing PerpetuaMain Test");
    E2EAllTablesInvoker e2EAllTablesInvoker = new E2EAllTablesInvoker();
    e2EAllTablesInvoker.invoke();
    System.exit(1);
  }
}
