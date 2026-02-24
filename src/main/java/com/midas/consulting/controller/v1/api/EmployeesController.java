package com.midas.consulting.controller.v1.api;

import com.microsoft.graph.tasks.IProgressCallback;
import com.midas.consulting.controller.v1.request.hrms.CreateEmployeeDocumentRequest;
import com.midas.consulting.controller.v1.request.hrms.CreateEmployeeRequest;
import com.midas.consulting.controller.v1.request.hrms.CreateSubmissionDocumentRequest;
//import com.midas.consulting.dto.response.Response;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.hrms.EmployeeDocs;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.AmazonClient;
import com.midas.consulting.service.hrms.EmployeeDocumentService;
import com.midas.consulting.service.hrms.EmployeeService;
import com.midas.consulting.service.hrms.onedrive.OneDriveAuthService;
import com.midas.consulting.service.hrms.onedrive.OneDriveServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/employee")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class EmployeesController {

    //    @Autowired
    private AuthenticationManager authenticationManager;


    //    @Autowired
    private EmployeeDocumentService employeeDocumentService;

    //    @Autowired
    private UserService userService;

    private EmployeeService employeeService;


    private  OneDriveServices oneDriveServices;
    final IProgressCallback callback = new IProgressCallback() {
        @Override
        public void progress(final long current, final long max) {
            System.out.println("Status is " + current + " max of " + max
            );
        }
    };
    private AmazonClient amazonClient;


    @Autowired
    public EmployeesController(OneDriveAuthService oneDriveAuthService, AuthenticationManager authenticationManager, EmployeeDocumentService employeeDocumentService, UserService userService, EmployeeService employeeService, AmazonClient amazonClient) {
        this.authenticationManager = authenticationManager;
        this.employeeDocumentService = employeeDocumentService;
        this.userService = userService;
        this.employeeService = employeeService;
        this.amazonClient = amazonClient;
        this.oneDriveServices = oneDriveAuthService;
    }



    @PostMapping("/uploadFile")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response uploadFile(@RequestParam("docType") String docType,
                               @RequestParam("docDesc") String docDesc,
                               @RequestParam("docName") String docName,
                               @RequestParam("empId") String empId,
                               @RequestParam("expiryDate") String expiryDate,
                               Principal principal,
                               @RequestPart(value = "file") MultipartFile file) throws IOException {
        Response response = new Response();
        Date date = new Date();
        String pattern = "yyyy-MM-dd"; // Adjust the pattern according to your date format
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        if (expiryDate.equals("") || expiryDate == null) {

            date = null;//dateFormat.parse(expiryDate);
            System.out.println("Parsed Date: " + date);

        } else {
            try {
//             expiryDate = "2124-12-12";
                date = dateFormat.parse(expiryDate);
                System.out.println("Parsed Date: " + date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        CreateEmployeeDocumentRequest createEmployeeDocumentRequest = new CreateEmployeeDocumentRequest()
                .setEmployeeId(empId)
                .setExpiryDate(date)
                .setDocType(docType)
                .setDocName(docName)
                .setDocDesc(docDesc);
//        okhttp3.Response fileUploadResponse = new OneDriveAuthService().upload(file);
        okhttp3.Response fileUploadResponse = oneDriveServices.upload(file);
        try {
            createEmployeeDocumentRequest.setUploadDocStructure(fileUploadResponse.body().string());
            response.setStatus(Response.Status.OK).setPayload(employeeDocumentService.saveDocument(createEmployeeDocumentRequest, userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }



    @PostMapping("/submissionFile")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response submissionFile(@RequestParam("docType") String docType,
                               @RequestParam("docDesc") String docDesc,
                               @RequestParam("docName") String docName,
                               @RequestParam("email") String email,
                                   @RequestParam("status") String status,
                                   @RequestParam("comment") String comment,
                                   @RequestParam("completeDate") String completeDate,
                                   @RequestParam("meetRequirements") String meetRequirements,
                                   @RequestParam("passFailResponse") String passFailResponse,
                                   @RequestParam("certificateNumber") String certificateNumber,
                                   @RequestParam("srcId") String srcId,
                                   @RequestParam("attachmentType") String attachmentType,
                                   @RequestParam("achieveDate") String achieveDate,
                                   @RequestParam("expiryDate") String expiryDate,
                               @RequestParam("vmsName") String vmsName,
                               Principal principal,
                               @RequestPart(value = "file") MultipartFile file) throws IOException {
        Response response = new Response();
        Date date = new Date();
        String pattern = "yyyy-MM-dd"; // Adjust the pattern according to your date format
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        CreateSubmissionDocumentRequest createEmployeeDocumentRequest = new CreateSubmissionDocumentRequest()
                .setAttachmentType(attachmentType)
                .setDocType(docType)
                .setEmail(email)
                .setAchieveDate(achieveDate)
                .setExpiryDate(expiryDate)
                .setDocName(docName)
                .setComment(comment)
                .setStatus(status)
                .setCompleteDate(completeDate)
                .setMeetRequirements(meetRequirements)
                .setPassFailResponse(passFailResponse)
                .setCertificateNumber(certificateNumber)
                .setSourceId(srcId)
                .setDocDesc(docDesc);
//        okhttp3.Response fileUploadResponse = new OneDriveAuthService().upload(file);
        okhttp3.Response fileUploadResponse = oneDriveServices.upload(file);
        try {
            createEmployeeDocumentRequest.setUploadDocStructure(fileUploadResponse.body().string());
            response.setStatus(Response.Status.OK).setPayload(employeeDocumentService.saveSubmissionDocument(createEmployeeDocumentRequest, userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }


    @DeleteMapping("/deleteFile")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public String deleteFile(@RequestPart(value = "url") String fileUrl) {
        return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
    }

    @PostMapping("/addDocument")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response addDocument(@RequestBody CreateEmployeeDocumentRequest createEmployeeDocumentRequest, Principal principal) throws Exception {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(employeeDocumentService.saveDocument(createEmployeeDocumentRequest, userService.findUserByEmail(principal.getName())));
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }

        return response;
    }

    @PostMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateDocument(@RequestBody CreateEmployeeDocumentRequest createEmployeeDocumentRequest, Principal principal) throws Exception {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(employeeDocumentService.updateDocument(createEmployeeDocumentRequest, userService.findUserByEmail(principal.getName())));
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }
        return response;
    }


    @GetMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllEmployees(Principal principal) {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(employeeService.getAllEmployees());
        }
        catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }

        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getEmployeeById(@PathVariable String id) {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(employeeService.getEmployeeById(id).orElse(null));
        }
        catch (Exception ee){
            response.setStatus(Response.Status.OK);
            response.setErrors(ee.getMessage());
            return  response;
        }

        return response;
    }

    @GetMapping("/getSubmissionDocBySrcId/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getSubmissionDocBySrcId(@PathVariable String id) {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(employeeService.getEmployeeById(id).orElse(null));
        }
        catch (Exception ee){
            response.setStatus(Response.Status.OK);
            response.setErrors(ee.getMessage());
            return  response;
        }

        return response;
    }

    @GetMapping("/getDocsByEmployeeId/{employeeId}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getDocsByEmployeeId(@PathVariable String employeeId) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            List<EmployeeDocs> employeeDocs = employeeService.getDocsByEmployeeId(employeeId);
            response.setPayload(employeeDocs);

        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }


    @GetMapping("/getAccessToken")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAccessToken() {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);

            response.setPayload(oneDriveServices.getAccessToken());

        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }
//    All Docs By EmployeeId




    @PostMapping("/saveEmployee")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response saveEmployee(@RequestBody CreateEmployeeRequest createOrganisationRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(employeeService.saveEmployee(createOrganisationRequest, userService.findUserByEmail(principal.getName())));
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }

        return response;
    }

    @PatchMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateEmployee(@RequestBody CreateEmployeeRequest createEmployeeRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(employeeService.updateEmployee(createEmployeeRequest, userService.findUserByEmail(principal.getName())));

        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteEmployee(@PathVariable String id) {
        Response response = new Response();
        try {
            employeeService.deleteEmployee(id);
            response.setStatus(Response.Status.OK).setPayload("Deleted Successfully");

        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }

    @DeleteMapping("/deleteEmployeeDocument/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteEmployeeDocument(@PathVariable String id) {
        Response response = new Response();
        try {
            employeeDocumentService.deleteDocument(id);
            response.setStatus(Response.Status.OK).setPayload("Deleted Successfully");
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }
}
