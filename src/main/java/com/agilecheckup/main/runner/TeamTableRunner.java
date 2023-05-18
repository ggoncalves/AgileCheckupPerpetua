package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.TeamService;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class TeamTableRunner extends AbstractCommandRunner<Team> {

  private TeamService teamService;

  public TeamTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Supplier<Optional<Team>> getCreateSupplier() {
    // Must be aware that, the id must be valid.
    // TODO: This test should be changed to allow create a known departmentId at first.
    return () -> getTeamService().create("TeamName", "Team Description", "Another TenantId", "8db3abd1-d1a8-46e4-8d87-1dcce1825e51");
  }

  @Override
  protected AbstractCrudService<Team, AbstractCrudRepository<Team>> getCrudService() {
    return getTeamService();
  }

  @Override
  protected void verifySavedEntity(Team savedEntity, Team fetchedEntity) {
    // Do nothing
  }

  private TeamService getTeamService() {
    if (teamService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      teamService = serviceComponent.buildTeamService();
    }
    return teamService;
  }
}
