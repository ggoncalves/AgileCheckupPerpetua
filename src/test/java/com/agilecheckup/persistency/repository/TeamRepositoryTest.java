package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Team;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedTeam;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryTest extends AbstractRepositoryTest<Team> {

  @InjectMocks
  @Spy
  private TeamRepository teamRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return teamRepository;
  }

  @Override
  Team createMockedT() {
    return createMockedTeam();
  }
}