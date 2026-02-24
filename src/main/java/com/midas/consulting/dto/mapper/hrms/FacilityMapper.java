package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateFacilityRequest;
import com.midas.consulting.model.hrms.Facility;
import com.midas.consulting.model.user.User;

public class FacilityMapper {

    public static Facility toFacility(CreateFacilityRequest request, User user) {
        Facility facility = new Facility();

        // Set ID if present (for updates)
        if (request.getId() != null && !request.getId().isEmpty()) {
            facility.setId(request.getId());
        }

        // Map all fields from request
        facility.setFacilityName(request.getFacilityName());
        facility.setFacilityAddress(request.getFacilityAddress());

        // Note: parentClient should be set by the service layer after validation

        return facility;
    }
}