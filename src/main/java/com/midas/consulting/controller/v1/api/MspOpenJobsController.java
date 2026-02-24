package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.MSPEmailServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/mspopenjobs")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class MspOpenJobsController {

  private   MSPEmailServices emailServices;

    @Autowired
    public MspOpenJobsController(MSPEmailServices emailServices, AuthenticationManager authenticationManager) {
        this.emailServices = emailServices;
    }

    @GetMapping("/getLatestOpenJobs/{subject}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getLatestOpenJobs(@PathVariable String subject) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(emailServices.getFoldersMSPOpenJobs(subject));
            return response;
        } catch (IOException e) {
           response.setStatus(Response.Status.EXCEPTION);
           response.setErrors(e);

        }
        return response;
    }

    private static void writeAttachment(String path, JSONObject attach) throws IOException {
        String nomeFile = attach.getString("name");
        byte[] byteArr = Base64.getMimeDecoder().decode(attach.getString("contentBytes").getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(path + nomeFile), byteArr);
    }
//    {
//        Response response = new Response();
//        Calendar c = Calendar.getInstance();
//        LocalDate date = new org.joda.time.LocalDate();
//
//        String searchMailString = String.format("*** MSP Open Jobs - %s %dth %d ***", date.monthOfYear().getAsString(), date.getDayOfMonth(), date.getYear());
//        Date date = new Date();
//        String pattern = "yyyy-MM-dd"; // Adjust the pattern according to your date format
//        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
//
//        try {
//            date = dateFormat.parse(expiryDate);
//            System.out.println("Parsed Date: " + date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        CreateEmployeeDocumentRequest createEmployeeDocumentRequest = new CreateEmployeeDocumentRequest()
//                .setEmployeeId(empId)
//                .setExpiryDate(date)
//                .setDocType(docType);
////        okhttp3.Response fileUploadResponse = new OneDriveAuthService().upload(file);
//        okhttp3.Response fileUploadResponse = oneDriveServices.upload(file);
//        try {
//            createEmployeeDocumentRequest.setUploadDocStructure(fileUploadResponse.body().string());
//            response.setStatus(Response.Status.OK).setPayload(employeeDocumentService.saveDocument(createEmployeeDocumentRequest, userService.findUserByEmail(principal.getName())));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return response;
//    }
}
