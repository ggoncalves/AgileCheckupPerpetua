package com.agilecheckup.persistency.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamTest {

    private Team team;

    @BeforeEach
    void setUp() {
        team = Team.builder()
                .id("team-123")
                .tenantId("tenant-456")
                .name("Engineering Team")
                .description("Core engineering team")
                .departmentId("dept-789")
                .build();
    }

    @Test
    void testBuilderCreatesTeamWithAllFields() {
        assertThat(team.getId()).isEqualTo("team-123");
        assertThat(team.getTenantId()).isEqualTo("tenant-456");
        assertThat(team.getName()).isEqualTo("Engineering Team");
        assertThat(team.getDescription()).isEqualTo("Core engineering team");
        assertThat(team.getDepartmentId()).isEqualTo("dept-789");
    }

    @Test
    void testEqualsAndHashCode() {
        Team sameTeam = Team.builder()
                .id("team-123")
                .tenantId("tenant-456")
                .name("Engineering Team")
                .description("Core engineering team")
                .departmentId("dept-789")
                .build();

        Team differentTeam = Team.builder()
                .id("team-999")
                .tenantId("tenant-456")
                .name("Marketing Team")
                .description("Marketing team")
                .departmentId("dept-999")
                .build();

        assertThat(team).isEqualTo(sameTeam);
        assertThat(team.hashCode()).isEqualTo(sameTeam.hashCode());
        assertThat(team).isNotEqualTo(differentTeam);
    }

    @Test
    void testToString() {
        String teamString = team.toString();
        assertThat(teamString).contains("Team");
        assertThat(teamString).contains("departmentId=dept-789");
        assertThat(teamString).contains("name=Engineering Team");
    }

    @Test
    void testGettersAndSetters() {
        team.setDepartmentId("new-dept-123");
        assertThat(team.getDepartmentId()).isEqualTo("new-dept-123");

        team.setName("New Team Name");
        assertThat(team.getName()).isEqualTo("New Team Name");

        team.setDescription("New Description");
        assertThat(team.getDescription()).isEqualTo("New Description");
    }

    @Test
    void testNoArgsConstructor() {
        Team emptyTeam = new Team();
        assertThat(emptyTeam).isNotNull();
        assertThat(emptyTeam.getId()).isNotNull(); // ID is auto-generated
        assertThat(emptyTeam.getDepartmentId()).isNull();
    }

    @Test
    void testBuilderWithAllFields() {
        Team fullTeam = Team.builder()
                .tenantId("tenant-123")
                .name("Team Name")
                .description("Team Description")
                .departmentId("dept-123")
                .build();
        assertThat(fullTeam.getTenantId()).isEqualTo("tenant-123");
        assertThat(fullTeam.getName()).isEqualTo("Team Name");
        assertThat(fullTeam.getDescription()).isEqualTo("Team Description");
        assertThat(fullTeam.getDepartmentId()).isEqualTo("dept-123");
    }
}