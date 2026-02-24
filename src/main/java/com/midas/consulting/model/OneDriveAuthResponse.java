package com.midas.consulting.model;

public class OneDriveAuthResponse {

    // import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
FileUploadSuceesResponse root = om.readValue(myJsonString, FileUploadSuceesResponse.class); */

        public String token_type;
        public int expires_in;
        public int ext_expires_in;
        public String access_token;



}
