package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateClientRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.ClientsMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClientsService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public ClientsService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Client> getAllClients() {
        log.info("Fetching all clients");
        return mongoTemplate.getMongoTemplate().findAll(Client.class);
    }

    public Optional<Client> getClientById(String id) {
        log.info("Fetching client by id: {}", id);
        return Optional.ofNullable(mongoTemplate.getMongoTemplate().findById(id, Client.class));
    }

    public Client saveClient(CreateClientRequest createClientRequest, UserDto userDto) throws Exception {
        log.info("Saving new client: {}", createClientRequest.getClientName());

        // Check if client with same name already exists
        Query clientNameQuery = new Query(Criteria.where("clientName").is(createClientRequest.getClientName()));
        Client existingClient = mongoTemplate.getMongoTemplate().findOne(clientNameQuery, Client.class);

        if (existingClient != null) {
            log.error("Client with name '{}' already exists", createClientRequest.getClientName());
            throw new MidasCustomException.DuplicateEntityException(
                    "Client with name '" + createClientRequest.getClientName() + "' already exists"
            );
        }

        // Map the incoming request and save the client
        Client clientToBeSaved = ClientsMapper.toClient(createClientRequest, UserMapper.toUser(userDto));
        clientToBeSaved.setCreatedBy(userDto.getEmail());
        clientToBeSaved.setDateCreated(LocalDateTime.now());
        clientToBeSaved.setDateModified(LocalDateTime.now());

        Client savedClient = mongoTemplate.getMongoTemplate().save(clientToBeSaved);
        log.info("Client saved successfully with id: {}", savedClient.getId());
        return savedClient;
    }

    public Client updateClient(CreateClientRequest createClientRequest, UserDto userDto) throws Exception {
        log.info("Updating client with id: {}", createClientRequest.getId());

        if (createClientRequest.getId() == null || createClientRequest.getId().isEmpty()) {
            log.error("Client ID is required for update");
            throw new MidasCustomException.DuplicateEntityException("Client ID is required for update");
        }

        // Fetch existing client
        Client existingClient = mongoTemplate.getMongoTemplate().findById(createClientRequest.getId(), Client.class);

        if (existingClient == null) {
            log.error("Client not found with id: {}", createClientRequest.getId());
            throw new MidasCustomException.DuplicateEntityException(
                    "Client not found with id: " + createClientRequest.getId()
            );
        }

        // Check if client name is being changed to an existing name
        Query clientNameQuery = new Query(
                Criteria.where("clientName").is(createClientRequest.getClientName())
                        .and("_id").ne(createClientRequest.getId())
        );
        Client duplicateClient = mongoTemplate.getMongoTemplate().findOne(clientNameQuery, Client.class);

        if (duplicateClient != null) {
            log.error("Client with name '{}' already exists", createClientRequest.getClientName());
            throw new MidasCustomException.DuplicateEntityException(
                    "Client with name '" + createClientRequest.getClientName() + "' already exists"
            );
        }

        // Map the incoming request and update the client
        Client clientToUpdate = ClientsMapper.toClient(createClientRequest, UserMapper.toUser(userDto));
        clientToUpdate.setId(existingClient.getId());
        clientToUpdate.setCreatedBy(existingClient.getCreatedBy());
        clientToUpdate.setDateCreated(existingClient.getDateCreated());
        clientToUpdate.setDateModified(LocalDateTime.now());

        Client updatedClient = mongoTemplate.getMongoTemplate().save(clientToUpdate);
        log.info("Client updated successfully with id: {}", updatedClient.getId());
        return updatedClient;
    }

    public void deleteClient(String id) throws Exception {
        log.info("Deleting client with id: {}", id);

        Client client = mongoTemplate.getMongoTemplate().findById(id, Client.class);

        if (client == null) {
            log.error("Client not found with id: {}", id);
            throw new MidasCustomException.DuplicateEntityException(
                    "Client not found with id: " + id
            );
        }

        mongoTemplate.getMongoTemplate().remove(client);
        log.info("Client deleted successfully with id: {}", id);
    }
}