package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.TeamRequest;
import com.midas.consulting.model.Team;
import com.midas.consulting.service.TeamService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teams")
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // Create or update a Team
    @PostMapping("/save")
    public ResponseEntity<Team> saveOrUpdateTeam(@RequestBody TeamRequest team) {
        Team savedTeam = teamService.saveOrUpdateTeam(team);
        return ResponseEntity.ok(savedTeam);
    }

    // Get all Teams
    @GetMapping("/all")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    // Get a Team by ID
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        Optional<Team> team = teamService.getTeamById(id);
        return team.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a Team by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTeamById(@PathVariable String id) {
        teamService.deleteTeamById(id);
        return ResponseEntity.ok().build();
    }
//    private final TeamService teamService;
//
//    @Autowired
//    public TeamController(TeamService teamService) {
//        this.teamService = teamService;
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @PostMapping
//    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
//        return ResponseEntity.ok(teamService.createTeam(team));
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @GetMapping
//    public ResponseEntity<List<Team>> getAllTeams() {
//        return ResponseEntity.ok(teamService.getAllTeams());
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @GetMapping("/division/{divisionId}")
//    public ResponseEntity<List<Team>> getTeamsByDivisionId(@PathVariable String divisionId) {
//        return ResponseEntity.ok(teamService.getTeamsByDivisionId(divisionId));
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @GetMapping("/{id}")
//    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
//        return teamService.getTeamById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @PutMapping("/{id}")
//    public ResponseEntity<Team> updateTeam(@PathVariable String id, @RequestBody Team team) {
//        return ResponseEntity.ok(teamService.updateTeam(id, team));
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTeam(@PathVariable String id) {
//        teamService.deleteTeam(id);
//        return ResponseEntity.noContent().build();
//    }
}
