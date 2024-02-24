package com.agilecheckup.persistency.entity.score;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class CategoryScore implements Scorable {

  @DynamoDBAttribute
  private String categoryId;

  @DynamoDBAttribute
  private String categoryName;

  @DynamoDBAttribute
  private List<QuestionScore> questionScores;

  @DynamoDBAttribute
  private Integer score;

//  {
//    "id": "assessmentMatrix1",
//      "performanceCycleId": "cycle1",
//      "questionCount": 10,
//      "pillarMap": { ... },
//    "employeeResults": [
//    {
//      "employeeId": "employee1",
//        "totalScore": 80,
//        "scoresByPillar": {
//      "pillar1": {
//        "totalScore": 40,
//            "scoresByCategory": {
//          "category1": 20,
//              "category2": 20
//        }
//      },
//      "pillar2": {
//        "totalScore": 40,
//            "scoresByCategory": {
//          "category3": 20,
//              "category4": 20
//        }
//      }
//    }
//    },
//    {
//      "employeeId": "employee2",
//        "totalScore": 85,
//        "scoresByPillar": {
//      "pillar1": {
//        "totalScore": 45,
//            "scoresByCategory": {
//          "category1": 25,
//              "category2": 20
//        }
//      },
//      "pillar2": {
//        "totalScore": 40,
//            "scoresByCategory": {
//          "category3": 20,
//              "category4": 20
//        }
//      }
//    }
//    }
//  ]
//  }
}
