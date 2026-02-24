package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateClientRequest;
import com.midas.consulting.model.hrms.Client;
import com.midas.consulting.model.user.User;

public class ClientsMapper {

    public static Client toClient(CreateClientRequest request, User user) {
        Client client = new Client();

        // Set ID if present (for updates)
        if (request.getId() != null && !request.getId().isEmpty()) {
            client.setId(request.getId());
        }

        // Map all fields from request
        client.setClientName(request.getClientName());
        client.setClientAddress(request.getClientAddress());
        client.setClientPOC(request.getClientPOC());
        client.setClientPOCPhone(request.getClientPOCPhone());
        client.setClientPOCEmail(request.getClientPOCEmail());
        client.setPaymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : "Net 30");
        client.setClientWebsite(request.getClientWebsite());

        return client;
    }
}