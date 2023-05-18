package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PersonDocumentType;
import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;

public class TestObjectFactory {

  public static final String QUESTION_ID_1234 = "1234";
  public static final String GENERIC_ID_1234 = "1234";

  public static Question createMockedQuestion() {
    return Question.builder()
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question createMockedQuestion(String id) {
    return Question.builder()
        .id(id)
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  public static Question copyQuestionAndAddId(Question question, String id) {
    return Question.builder()
        .id(id)
        .question(question.getQuestion())
        .rateType(question.getRateType())
        .tenantId(question.getTenantId())
        .points(question.getPoints())
        .build();
  }

  public static Company createMockedCompany() {
    return Company.builder()
        .name("Company Name")
        .description("Company description")
        .tenantId("Random Tenant Id")
        .email("company@email.com")
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber("0001")
        .build();
  }

  public static Company createMockedCompany(String id) {
    return copyCompanyAndAddId(createMockedCompany(), id);
  }

  public static Company copyCompanyAndAddId(Company company, String id) {
    return Company.builder()
        .id(id)
        .name(company.getName())
        .description(company.getDescription())
        .tenantId(company.getTenantId())
        .email(company.getEmail())
        .personDocumentType(company.getPersonDocumentType())
        .documentNumber(company.getDocumentNumber())
        .industry(company.getIndustry())
        .size(company.getSize())
        .address(company.getAddress())
        .build();


  }
}
