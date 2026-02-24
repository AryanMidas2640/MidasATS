package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.TeamRequest;
import com.midas.consulting.model.CompanyDivision;
import com.midas.consulting.model.Team;
import com.midas.consulting.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamService {

    @Autowired
    private MongoTemplateProvider mongoTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyDivisionService companyDivisionService;

    // Create or update a Team
    public Team saveOrUpdateTeam(TeamRequest teamRequest) {
        // Initialize a Set to store User objects
        Set<User> userList = new HashSet<>();

        // Fetch Users based on userIdList from the TeamRequest
        teamRequest.getUserIdList().forEach(userId -> {
            User user = userService.findUserObjectById(userId);
            if (user != null) {
                userList.add(user);
            }
        });

        // Fetch CompanyDivision based on divisionId from the TeamRequest
        Optional<CompanyDivision> companyDivision = companyDivisionService.getDivisionById(teamRequest.getDivisionId());

        if (!companyDivision.isPresent()) {
            throw new IllegalArgumentException("Invalid division ID: " + teamRequest.getDivisionId());
        }

        // Create a new Team entity and set properties
        Team team = new Team()
                .setId(teamRequest.getId()) // Use setId if updating an existing Team
                .setName(teamRequest.getName())
                .setDescription(teamRequest.getDescription())
                .setDivisionId(companyDivision.get()) // Set the fetched CompanyDivision
                .setActive(teamRequest.getActive())
                .setDateCreated(new Date())
                .setDateModified(new Date())
                .setUserList(userList); // Set the user list

        // If there's an ID, try to update the existing team
        if (team.getId() != null) {
            Query query = new Query(Criteria.where("id").is(team.getId()));
            Team existingTeam = mongoTemplate.getMongoTemplate().findOne(query, Team.class);

            if (existingTeam != null) {
                // Update the existing team with the new details
                team.setDateCreated(existingTeam.getDateCreated());  // Retain the original creation date
                team.setDateModified(new Date());
                return mongoTemplate.getMongoTemplate().save(team); // Save updated team
            }
        }

        // If no team exists with the given ID, create a new one
        return mongoTemplate.getMongoTemplate().save(team);
    }

    // Get all Teams
    public List<Team> getAllTeams() {
        return mongoTemplate.getMongoTemplate().findAll(Team.class);
    }

    // Get Team by ID
    public Optional<Team> getTeamById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Team team = mongoTemplate.getMongoTemplate().findOne(query, Team.class);
        return Optional.ofNullable(team);
    }

    // Delete Team by ID
    public void deleteTeamById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.getMongoTemplate().remove(query, Team.class);
    }
}
