package com.agilecheckup.persistency.entity.base;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BaseEntityTest {

  private static final String BASE_UUID = "BASE_UUID";


  @Test
  void testBuildSubclassBaseEntity_DefaultUUID() {
    SubclassBaseEntity subclassBaseEntity = SubclassBaseEntity.builder().build();
    assertNotNull(subclassBaseEntity.getId());
    assertNotEquals(BASE_UUID, subclassBaseEntity.getId());
  }

  @Test
  void testBuildSubclassBaseEntity_BuildID() {
    SubclassBaseEntity subclassBaseEntity = SubclassBaseEntity.builder()
        .id(BASE_UUID)
        .build();
    assertNotNull(subclassBaseEntity.getId());
    assertEquals(BASE_UUID, subclassBaseEntity.getId());
  }
}