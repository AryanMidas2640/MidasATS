//package com.midas.consulting.service;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.Accessors;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Accessors(chain = true)
//public class ServiceConfigurationResult {
//    private boolean success;
//    private String message;
//    private String tenantId;
//    private int servicesConfigured;
//    private long configurationTimeMs;
//
//    public ServiceConfigurationResult success() {
//        this.success = true;
//        return this;
//    }
//
//    public ServiceConfigurationResult failure(String message) {
//        this.success = false;
//        this.message = message;
//        return this;
//    }
//}