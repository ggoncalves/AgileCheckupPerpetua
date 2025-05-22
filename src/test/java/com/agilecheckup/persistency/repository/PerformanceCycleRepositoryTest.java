package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.PerformanceCycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedPerformanceCycle;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleRepositoryTest extends AbstractRepositoryTest<PerformanceCycle> {

  @InjectMocks
  @Spy
  private PerformanceCycleRepository performanceCycleRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return performanceCycleRepository;
  }

  @Override
  PerformanceCycle createMockedT() {
    return createMockedPerformanceCycle(GENERIC_ID_1234, GENERIC_ID_1234);
  }

  @Override
  Class getMockedClass() {
    return PerformanceCycle.class;
  }
}