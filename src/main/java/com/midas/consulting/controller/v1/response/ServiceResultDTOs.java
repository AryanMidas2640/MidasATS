package com.midas.consulting.controller.v1.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Shared DTOs for service layer responses
 */
public class ServiceResultDTOs {

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class DatabaseSetupResult {
        private boolean success;
        private String message;
        private int collectionsCreated;
        private int indexesCreated;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ServiceConfigurationResult {
        private boolean success;
        private String message;
        private String tenantId;
        private int servicesConfigured;
    }


}