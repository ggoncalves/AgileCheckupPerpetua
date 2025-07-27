package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class AssessmentMatrixTableRunner extends AbstractEntityCrudRunner<AssessmentMatrix> {

  private AssessmentMatrixService assessmentMatrixService;
  private List<Question> questions;

  private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();

  public AssessmentMatrixTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<AssessmentMatrix>>> getCreateSupplier() {
    Map<String, PillarV2> pillarMap = tableRunnerHelper.createPillarsWithCategoriesMapV2();

    Collection<Supplier<Optional<AssessmentMatrix>>> collection = new ArrayList<>();
    collection.add(() -> getAssessmentMatrixService().create(
        "AssessmentMatrixName",
        "AssessmentMatrix Description",
        "Another TenantId",
        "321c5be6-9534-4b7c-9919-2f4418900935",
        pillarMap
    ));
    return collection;
  }

  @Override
  protected AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> getCrudService() {
    return getAssessmentMatrixService();
  }

  @Override
  protected void verifySavedEntity(AssessmentMatrix savedEntity, AssessmentMatrix fetchedEntity) {
    // Do nothing
  }

  private AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }

  private List<Question> getQuestions() {
    if (questions == null) {
      questions = new ArrayList<>();
    }
    return questions;
  }

  @Override
  protected void postCreate(Collection<AssessmentMatrix> entities) {
    entities.forEach(entity -> {
      createDependencies(entity);
      getAssessmentMatrixService().updateCurrentPotentialScore(entity.getId(), entity.getTenantId());
    });
  }

  private void createDependencies(AssessmentMatrix entity) {
    PillarV2 pillar = entity.getPillarMap().values().iterator().next();
    CategoryV2 category = pillar.getCategoryMap().values().iterator().next();
    tableRunnerHelper.getQuestionService().create(
        "Pergunta oficial",
        QuestionType.STAR_THREE,
        entity.getTenantId(),
        15d,
        entity.getId(),
        pillar.getId(),
        category.getId(),
        "Extra description"
    ).ifPresent(question -> getQuestions().add(question));
  }

  @Override
  protected void deleteDependencies() {
    super.deleteDependencies();
    getQuestions().forEach(question -> tableRunnerHelper.getQuestionService().delete(question));
  }

  /**
   * Enhanced run method that includes V1-to-V2 migration and update operations
   */
  @Override
  public void run() {
    log.info("=== AssessmentMatrix CRUD Operations with V1-to-V2 Migration Demo ===");
    
    // Step 1: Create new AssessmentMatrix entities with V2 Pillar/Category
    log.info("1. Creating new AssessmentMatrix entities with V2 Pillar/Category structure...");
    Collection<AssessmentMatrix> newEntities = create();
    postCreate(newEntities);
    fetch(newEntities);
    
    // Step 2: Fetch existing V1 entities and migrate them to V2
    log.info("2. Fetching existing V1 AssessmentMatrix entities for migration...");
    Collection<AssessmentMatrix> existingEntities = fetchExistingEntitiesForMigration();
    
    // Step 3: Migrate V1 entities to V2 structure
    log.info("3. Migrating V1 entities to V2 Pillar/Category structure...");
    Collection<AssessmentMatrix> migratedEntities = migrateV1ToV2(existingEntities);
    
    // Step 4: Update entities with new V2 Pillar/Category structures
    log.info("4. Updating entities with enhanced V2 Pillar/Category structures...");
    Collection<AssessmentMatrix> updatedEntities = updateWithNewV2Structure(newEntities);
    
    // Step 5: List all entities to verify operations
    log.info("5. Listing all entities after operations...");
    try {
      invokeListAll();
    } catch (Exception e) {
      log.warn("Unable to list entities due to mixed data formats (V1 Map vs V2 JSON). This is expected during migration.", e);
      log.info("Note: Existing V1 entities with Map format need to be migrated to V2 JSON format.");
    }
    
    // Step 6: Cleanup if requested (entities created in this run)
    // Note: In a real migration, you wouldn't delete the migrated entities
    log.info("6. Cleaning up newly created entities (keeping migrated ones)...");
    internalDelete(newEntities);
    
    log.info("=== AssessmentMatrix V1-to-V2 Migration Demo Complete ===");
  }

  /**
   * Fetch existing AssessmentMatrix entities from database for migration demonstration
   */
  private Collection<AssessmentMatrix> fetchExistingEntitiesForMigration() {
    try {
      PaginatedScanList<AssessmentMatrix> allEntities = getAssessmentMatrixService().findAll();
      
      // Filter entities that might have V1 Pillar structure (for demonstration)
      List<AssessmentMatrix> existingEntities = allEntities.stream()
          .filter(entity -> entity.getPillarMap() != null && !entity.getPillarMap().isEmpty())
          .limit(3) // Limit for demonstration purposes
          .collect(Collectors.toList());
      
      log.info("Found {} existing AssessmentMatrix entities for migration demo", existingEntities.size());
      existingEntities.forEach(entity -> 
        log.info("Existing entity: {} with {} pillars", entity.getName(), entity.getPillarMap().size())
      );
      
      return existingEntities;
    } catch (Exception e) {
      log.warn("Could not fetch existing entities for migration demo: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Migrate V1 AssessmentMatrix entities to V2 Pillar/Category structure
   */
  private Collection<AssessmentMatrix> migrateV1ToV2(Collection<AssessmentMatrix> v1Entities) {
    List<AssessmentMatrix> migratedEntities = new ArrayList<>();
    
    for (AssessmentMatrix entity : v1Entities) {
      try {
        // Create enhanced V2 pillar structure
        Map<String, PillarV2> enhancedV2PillarMap = createEnhancedV2PillarsFromExisting(entity.getPillarMap());
        
        // Update the entity with V2 structure
        Optional<AssessmentMatrix> updatedEntity = getAssessmentMatrixService().update(
            entity.getId(),
            entity.getName() + " (Migrated to V2)",
            entity.getDescription() + " - Enhanced with V2 Pillar/Category structure",
            entity.getTenantId(),
            entity.getPerformanceCycleId(),
            enhancedV2PillarMap
        );
        
        if (updatedEntity.isPresent()) {
          migratedEntities.add(updatedEntity.get());
          log.info("Successfully migrated entity: {} to V2 structure", entity.getName());
          
          // Update potential score for migrated entity
          getAssessmentMatrixService().updateCurrentPotentialScore(entity.getId(), entity.getTenantId());
        } else {
          log.warn("Failed to migrate entity: {}", entity.getName());
        }
      } catch (Exception e) {
        log.error("Error migrating entity {}: {}", entity.getName(), e.getMessage());
      }
    }
    
    log.info("Successfully migrated {} entities from V1 to V2 structure", migratedEntities.size());
    return migratedEntities;
  }

  /**
   * Create enhanced V2 Pillar/Category structure from existing pillar map
   */
  private Map<String, PillarV2> createEnhancedV2PillarsFromExisting(Map<String, PillarV2> existingPillarMap) {
    Map<String, PillarV2> enhancedPillarMap = new HashMap<>();
    
    // If existing map is empty or null, create default V2 structure
    if (existingPillarMap == null || existingPillarMap.isEmpty()) {
      return tableRunnerHelper.createPillarsWithCategoriesMapV2();
    }
    
    // Enhance existing V2 pillars with additional categories
    for (Map.Entry<String, PillarV2> pillarEntry : existingPillarMap.entrySet()) {
      PillarV2 existingPillar = pillarEntry.getValue();
      
      // Create enhanced category map with existing + new categories
      Map<String, CategoryV2> enhancedCategoryMap = new HashMap<>(existingPillar.getCategoryMap());
      
      // Add additional V2 categories for demonstration
      CategoryV2 newCategory = CategoryV2.builder()
          .name("Enhanced Category - " + System.currentTimeMillis())
          .description("Category added during V2 migration")
          .build();
      enhancedCategoryMap.put(newCategory.getId(), newCategory);
      
      // Create enhanced pillar
      PillarV2 enhancedPillar = PillarV2.builder()
          .id(existingPillar.getId())
          .name(existingPillar.getName() + " (Enhanced V2)")
          .description(existingPillar.getDescription() + " - Enhanced during migration")
          .categoryMap(enhancedCategoryMap)
          .createdDate(existingPillar.getCreatedDate())
          .lastUpdatedDate(existingPillar.getLastUpdatedDate())
          .build();
      
      enhancedPillarMap.put(pillarEntry.getKey(), enhancedPillar);
    }
    
    // Add one completely new V2 pillar
    Map<String, CategoryV2> newPillarCategories = new HashMap<>();
    CategoryV2 migrationCategory = CategoryV2.builder()
        .name("Migration Demo Category")
        .description("Category created during V1-to-V2 migration")
        .build();
    newPillarCategories.put(migrationCategory.getId(), migrationCategory);
    
    PillarV2 migrationPillar = PillarV2.builder()
        .name("Migration Demo Pillar")
        .description("Pillar created during V1-to-V2 migration demonstration")
        .categoryMap(newPillarCategories)
        .build();
    
    enhancedPillarMap.put(migrationPillar.getId(), migrationPillar);
    
    log.info("Created enhanced V2 pillar structure with {} pillars", enhancedPillarMap.size());
    return enhancedPillarMap;
  }

  /**
   * Update existing entities with new V2 Pillar/Category structures
   */
  private Collection<AssessmentMatrix> updateWithNewV2Structure(Collection<AssessmentMatrix> entities) {
    List<AssessmentMatrix> updatedEntities = new ArrayList<>();
    
    for (AssessmentMatrix entity : entities) {
      try {
        // Create a completely new V2 pillar structure for update
        Map<String, PillarV2> newV2PillarMap = createUpdatedV2PillarStructure();
        
        // Update the entity with new V2 structure
        Optional<AssessmentMatrix> updatedEntity = getAssessmentMatrixService().update(
            entity.getId(),
            entity.getName() + " (Updated with V2)",
            entity.getDescription() + " - Updated with new V2 Pillar/Category structure",
            entity.getTenantId(),
            entity.getPerformanceCycleId(),
            newV2PillarMap
        );
        
        if (updatedEntity.isPresent()) {
          updatedEntities.add(updatedEntity.get());
          log.info("Successfully updated entity: {} with new V2 structure", entity.getName());
          
          // Update potential score for updated entity
          getAssessmentMatrixService().updateCurrentPotentialScore(entity.getId(), entity.getTenantId());
        } else {
          log.warn("Failed to update entity: {}", entity.getName());
        }
      } catch (Exception e) {
        log.error("Error updating entity {}: {}", entity.getName(), e.getMessage());
      }
    }
    
    log.info("Successfully updated {} entities with new V2 structures", updatedEntities.size());
    return updatedEntities;
  }

  /**
   * Create a new V2 Pillar/Category structure for update operations
   */
  private Map<String, PillarV2> createUpdatedV2PillarStructure() {
    Map<String, PillarV2> updatedPillarMap = new HashMap<>();
    
    // Create V2 Pillar 1 with multiple categories
    Map<String, CategoryV2> pillar1Categories = new HashMap<>();
    
    CategoryV2 category1 = CategoryV2.builder()
        .name("Updated Category 1")
        .description("First category in updated V2 structure")
        .build();
    pillar1Categories.put(category1.getId(), category1);
    
    CategoryV2 category2 = CategoryV2.builder()
        .name("Updated Category 2")
        .description("Second category in updated V2 structure")
        .build();
    pillar1Categories.put(category2.getId(), category2);
    
    CategoryV2 category3 = CategoryV2.builder()
        .name("Updated Category 3")
        .description("Third category in updated V2 structure")
        .build();
    pillar1Categories.put(category3.getId(), category3);
    
    PillarV2 pillar1 = PillarV2.builder()
        .name("Updated V2 Pillar 1")
        .description("First pillar in updated V2 structure")
        .categoryMap(pillar1Categories)
        .build();
    updatedPillarMap.put(pillar1.getId(), pillar1);
    
    // Create V2 Pillar 2 with different categories
    Map<String, CategoryV2> pillar2Categories = new HashMap<>();
    
    CategoryV2 category4 = CategoryV2.builder()
        .name("Advanced Category A")
        .description("Advanced category A in updated V2 structure")
        .build();
    pillar2Categories.put(category4.getId(), category4);
    
    CategoryV2 category5 = CategoryV2.builder()
        .name("Advanced Category B")
        .description("Advanced category B in updated V2 structure")
        .build();
    pillar2Categories.put(category5.getId(), category5);
    
    PillarV2 pillar2 = PillarV2.builder()
        .name("Updated V2 Pillar 2")
        .description("Second pillar in updated V2 structure")
        .categoryMap(pillar2Categories)
        .build();
    updatedPillarMap.put(pillar2.getId(), pillar2);
    
    log.info("Created updated V2 pillar structure with {} pillars and {} total categories", 
        updatedPillarMap.size(), 
        updatedPillarMap.values().stream().mapToInt(p -> p.getCategoryMap().size()).sum());
    
    return updatedPillarMap;
  }

  private void internalDelete(Collection<AssessmentMatrix> entities) {
    delete(entities);
    deleteDependencies();
  }

}
