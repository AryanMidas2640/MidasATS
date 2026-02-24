    package com.midas.consulting.controller.v1.api;

    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import com.fasterxml.jackson.annotation.JsonInclude;
    import com.fasterxml.jackson.databind.DeserializationFeature;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.google.gson.Gson;
    import com.google.i18n.phonenumbers.NumberParseException;
    import com.google.i18n.phonenumbers.PhoneNumberUtil;
    import com.google.i18n.phonenumbers.Phonenumber;
    import com.midas.consulting.controller.v1.request.candidate.CandidateSearchRequest;
    import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequestMidas;
    import com.midas.consulting.controller.v1.response.Response;
    import com.midas.consulting.controller.v1.response.microsoft.fileuploads.FileHandle;
    import com.midas.consulting.controller.v1.response.parsing.midas.ResumeParsingRespone;
    import com.midas.consulting.controller.v1.response.parsing.midasv2.ResumeParsingResponeV2;
    import com.midas.consulting.dto.mapper.UserMapper;
    import com.midas.consulting.dto.mapper.candidate.CandidateMapper;
    import com.midas.consulting.dto.model.user.UserDto;
    import com.midas.consulting.dto.parser.ResumeData;
    import com.midas.consulting.exception.MidasCustomException;
    import com.midas.consulting.model.candidate.*;
    import com.midas.consulting.model.user.User;
    import com.midas.consulting.service.ActivityService;
    import com.midas.consulting.service.TenantContext;
    import com.midas.consulting.service.UserService;
    import com.midas.consulting.service.candidate.IMidasCandidateService;
    import com.midas.consulting.service.candidate.LocationService;
    import com.midas.consulting.service.candidate.MidasCandidateServiceImpl;
    import com.midas.consulting.service.hrms.ClientsService;
    import com.midas.consulting.service.hrms.onedrive.OneDriveAuthService;
    import com.midas.consulting.service.parsing.MidasResumeParsingService;
    import com.midas.consulting.util.PhoneNumberFormatter;
    import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
    import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
    import io.swagger.annotations.Api;
    import io.swagger.annotations.ApiOperation;
    import io.swagger.annotations.Authorization;
    import lombok.Data;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import lombok.experimental.Accessors;
    import org.apache.poi.xwpf.usermodel.XWPFDocument;
    import org.modelmapper.ModelMapper;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.util.StringUtils;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import javax.servlet.http.HttpServletRequest;
    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.nio.file.StandardCopyOption;
    import java.security.Principal;
    import java.util.*;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    import java.util.stream.Collectors;

    @RestController
    @CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
    @RequestMapping("/api/v1/candidateMidas")

    @Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
    public class CandidateControllerMidas {
        private static final Logger logger = LoggerFactory.getLogger(CandidateControllerMidas.class);

        private LocationService locationService;
        //    @Autowired
        private AuthenticationManager authenticationManager;

        @Value("${app.file-processing.temp-directory:${java.io.tmpdir}/ats-temp}")
        private String tempDirectory;

        //    @Autowired
        private UserService userService;

    //    private ICandidateService iCandidateService;

        private IMidasCandidateService iCandidateService;

        private ActivityService activityService;
        private final ObjectMapper objectMapper;

        private ClientsService clientsService;

        private MidasResumeParsingService midasResumeParsingService;
        private OneDriveAuthService oneDriveServices;




        @Autowired
        public CandidateControllerMidas(LocationService locationService, ActivityService activityService, MidasResumeParsingService midasResumeParsingService, OneDriveAuthService oneDriveServices, IMidasCandidateService iCandidateService, AuthenticationManager authenticationManager, UserService userService, ClientsService clientsService) {
            this.oneDriveServices = oneDriveServices;
            this.iCandidateService = iCandidateService;
            this.activityService =activityService;
            this.clientsService = clientsService;
            this.midasResumeParsingService =midasResumeParsingService;
            this.objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.userService = userService;
    //        this.mongoTemplateFactory=mongoTemplateFactory;
            this.authenticationManager = authenticationManager;
        }

        @PostMapping("/createCandidate")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Response createCandidate(Principal principal, @org.springframework.web.bind.annotation.RequestBody CreateCandidateRequestMidas
                createCandidateRequest) throws IOException, MidasCustomException.ParsingException {
            return Response.ok().setPayload(iCandidateService.addCandidate(createCandidateRequest));
        }

        @GetMapping("/allUsersCandidates")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Response allUsersCandidates(Principal principal, @RequestParam(defaultValue = "0") int page, // Default page 0
                                           @RequestParam(defaultValue = "10") int size)  {
            Pageable pageable = PageRequest.of(page, size);

            return Response.ok().setPayload(iCandidateService.getAllCandidates(pageable));
        }

        @PatchMapping("/reparse-missing-phone")
        @ApiOperation(value = "Update candidates with missing phone numbers through re-parsing",
                authorizations = {@Authorization(value = "apiKey")})
        public Response reparseAndUpdateCandidatesWithMissingPhone(
                Principal principal,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "50") int size,
                @RequestParam(defaultValue = "false") boolean dryRun) {

            String correlationId = UUID.randomUUID().toString();
            logger.info("Starting reparse for candidates with missing phone - correlationId: {}, page: {}, size: {}, dryRun: {}",
                    correlationId, page, size, dryRun);

            Response response = new Response();
            List<CandidateUpdateResult> updateResults = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            try {
                // Find candidates with null or blank phone numbers
                Pageable pageable = PageRequest.of(page, size);
                Page<CandidateMidas> candidatesWithMissingPhone = iCandidateService.getCandidatesWithMissingPhone(pageable);

                logger.info("Found {} candidates with missing phone numbers - correlationId: {}",
                        candidatesWithMissingPhone.getTotalElements(), correlationId);

                if (candidatesWithMissingPhone.isEmpty()) {
                    response.setStatus(Response.Status.OK)
                            .setPayload("No candidates found with missing phone numbers");
                    return response;
                }

                int successCount = 0;
                int errorCount = 0;

                for (CandidateMidas candidate : candidatesWithMissingPhone.getContent()) {
                    String candidateCorrelationId = correlationId + "_" + candidate.getId();

                    try {
                        CandidateUpdateResult result = processPhoneUpdateForCandidate(candidate, candidateCorrelationId, dryRun);
                        updateResults.add(result);

                        if (result.isSuccess()) {
                            successCount++;
                        } else {
                            errorCount++;
                        }

                    } catch (Exception e) {
                        logger.error("Error processing candidate {} - correlationId: {}",
                                candidate.getId(), candidateCorrelationId, e);

                        CandidateUpdateResult errorResult = new CandidateUpdateResult()
                                .setCandidateId(candidate.getId())
                                .setCandidateName(candidate.getName())
                                .setCandidateEmail(candidate.getEmail())
                                .setSuccess(false)
                                .setErrorMessage("Processing failed: " + e.getMessage());

                        updateResults.add(errorResult);
                        errors.add("Candidate " + candidate.getId() + ": " + e.getMessage());
                        errorCount++;
                    }
                }

                // Prepare response
                Map<String, Object> result = new HashMap<>();
                result.put("correlationId", correlationId);
                result.put("totalCandidatesFound", candidatesWithMissingPhone.getTotalElements());
                result.put("candidatesProcessed", candidatesWithMissingPhone.getContent().size());
                result.put("successfulUpdates", successCount);
                result.put("failedUpdates", errorCount);
                result.put("dryRun", dryRun);
                result.put("updateResults", updateResults);
                result.put("hasNextPage", candidatesWithMissingPhone.hasNext());
                result.put("currentPage", page);
                result.put("totalPages", candidatesWithMissingPhone.getTotalPages());

                if (!errors.isEmpty()) {
                    result.put("errors", errors);
                }

                response.setStatus(Response.Status.OK)
                        .setPayload(result);

                logger.info("Reparse completed - correlationId: {}, success: {}, errors: {}",
                        correlationId, successCount, errorCount);

                return response;

            } catch (Exception e) {
                logger.error("Error in reparse operation - correlationId: {}", correlationId, e);
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR)
                        .setErrors("Reparse operation failed: " + e.getMessage());
                return response;
            }
        }



        private CandidateUpdateResult processPhoneUpdateForCandidate(CandidateMidas candidate, String correlationId, boolean dryRun) {
            CandidateUpdateResult result = new CandidateUpdateResult()
                    .setCandidateId(candidate.getId())
                    .setCandidateName(candidate.getName())
                    .setCandidateEmail(candidate.getEmail())
                    .setOriginalPhone(candidate.getPhone());

            try {
                // Check if candidate has a file handle for re-parsing


                logger.debug("Re-parsing candidate {} - correlationId: {}", candidate.getId(), correlationId);

                // Download file from OneDrive and create temp file
//                Path tempFilePath = createTempFileFromOneDrive(candidate.getFileHandle(), correlationId);

                try {
                    // Re-parse the resume
//                    ResumeParsingResponeV2 parseResult = parseResume(tempFilePath, correlationId);
                    ResumeParsingResponeV2 parseResultFullText = parseResumeFullText(candidate.getFullText());


                    if (parseResultFullText == null) {
                        result.setSuccess(false)
                                .setErrorMessage("Failed to re-parse resume");
                        return result;
                    }

                    // Extract phone number from new parsing result
                    String newPhone = extractPhoneFromParseResult(parseResultFullText);

                    if (candidate.getState().isEmpty()){
                        candidate.setState(parseResultFullText.getParsed().getAddress().getState());
                    }
                    if (candidate.getCity().isEmpty()){
                        candidate.setCity(parseResultFullText.getParsed().getAddress().getCity());

                    }
                    if (candidate.getZip().isEmpty()){
                        candidate.setZip(parseResultFullText.getParsed().getAddress().getZip());
                    }

                    if (candidate.getName().isEmpty()){
                        String[] nameParts= parseResultFullText.getParsed().getName().split(" ");
                        switch (nameParts.length){
                            case 0:{

                            }
                            case 1:{
                                candidate.setName(Arrays.stream(nameParts).collect(Collectors.joining()));
                            }
                            case 2:{
                                candidate.setName(nameParts[0]);
                                candidate.setLastName(nameParts[1]);
                            }
                            case 3:{
                                candidate.setName(Arrays.stream(nameParts).collect(Collectors.joining()));
                            }
                            default:{
                                candidate.setName(Arrays.stream(nameParts).collect(Collectors.joining()));
                            }
                        }

                    }
                    if (parseResultFullText.getParsed().getSkills().size()>0){
                        candidate.setSkills(parseResultFullText.getParsed().getSkills());
                    }

                    if (parseResultFullText.getParsed().getExperience().size()>0){
                        candidate.setExperience(Collections.singletonList(parseResultFullText.getParsed().getExperience()));
                    }
                    if (parseResultFullText.getParsed().getTotalExperience()>0){
                        candidate.setTotalExp(String.valueOf(parseResultFullText.getParsed().getTotalExperience()));
                    }

                    if (StringUtils.isEmpty(newPhone)) {
                        result.setSuccess(false)
                                .setErrorMessage("No phone number found in re-parsed resume");
                        return result;
                    }

                    // Format the phone number
                    String formattedPhone = PhoneNumberFormatter.formatPhoneNumber(newPhone);

                    result.setNewPhone(formattedPhone)
                            .setParsingSource("Re-parsed from original file");

                    // Update candidate if not dry run
                    if (!dryRun) {
                        // Check for duplicate phone numbers
                        Optional<CandidateMidas> existingCandidate = iCandidateService.getCandidateByPhone(formattedPhone);
                        if (existingCandidate.isPresent() && !existingCandidate.get().getId().equals(candidate.getId())) {
                            result.setSuccess(false)
                                    .setErrorMessage("Phone number already exists for another candidate: " + existingCandidate.get().getId());
                            return result;
                        }

                        // Update the candidate
                        candidate.setPhone(formattedPhone);
                        CandidateMidas updatedCandidate = iCandidateService.updateCandidatePhone(candidate);

                        // Log the activity
                        logPhoneUpdateActivity(candidate, formattedPhone, correlationId);

                        result.setSuccess(true)
                                .setMessage("Phone number successfully updated");

                        logger.debug("Successfully updated phone for candidate {} - correlationId: {}",
                                candidate.getId(), correlationId);
                    } else {
                        result.setSuccess(true)
                                .setMessage("Phone number would be updated (dry run mode)");
                    }

                } finally {
                    // Always cleanup temp file
//                    cleanupTempFile(tempFilePath, correlationId);
                }

            } catch (Exception e) {
                logger.error("Error processing phone update for candidate {} - correlationId: {}",
                        candidate.getId(), correlationId, e);
                result.setSuccess(false)
                        .setErrorMessage("Processing error: " + e.getMessage());
            }

            return result;
        }

        private Path createTempFileFromOneDrive(FileHandle fileHandle, String correlationId) throws IOException {
            try {
                // Download file content from OneDrive
                byte[] fileContent = oneDriveServices.downloadFile(fileHandle.getId());

                // Create temp file
                String uniqueFileName = System.currentTimeMillis() + "_" + correlationId + "_" +
                        sanitizeFileName(fileHandle.getName());

                Path tempDir = Paths.get(tempDirectory);
                Files.createDirectories(tempDir);

                Path tempFilePath = tempDir.resolve(uniqueFileName);
                Files.write(tempFilePath, fileContent);

                logger.debug("Created temp file for re-parsing - correlationId: {}, path: {}",
                        correlationId, tempFilePath);

                return tempFilePath;

            } catch (Exception e) {
                logger.error("Failed to create temp file from OneDrive - correlationId: {}", correlationId, e);
                throw new IOException("Failed to download file from OneDrive: " + e.getMessage(), e);
            }
        }



        private String extractPhoneFromParseResult(ResumeParsingResponeV2 parseResult) {
            try {
                if (parseResult.getParsed() != null && parseResult.getParsed().getPhone() != null) {
                    return parseResult.getParsed().getPhone().trim();
                }
            } catch (Exception e) {
                logger.debug("Error extracting phone from parse result: {}", e.getMessage());
            }
            return null;
        }

        private void logPhoneUpdateActivity(CandidateMidas candidate, String newPhone, String correlationId) {
            try {
                Activity activity = new Activity();
                activity.setActivityNote(String.format(
                        "<h3 style='color: #1e90ff;'>System Generated Phone Update</h3>" +
                                "<p><strong>Correlation ID:</strong> %s</p>" +
                                "<p><strong>Previous Phone:</strong> %s</p>" +
                                "<p><strong>New Phone:</strong> %s</p>" +
                                "<p><strong>Update Source:</strong> Re-parsed from original resume file</p>",
                        correlationId,
                        candidate.getPhone() != null ? candidate.getPhone() : "null/blank",
                        newPhone
                ));
                activity.setActivityType(ActivityType.SYSTEM_GEN_CANDIDATE_MODIFIED);
                activity.setDateCreated(new Date());
                activity.setCandidateID(candidate.getId());
                // Set user as system if no principal available
                activity.setUserID(null); // or find system user

                activityService.createOrUpdateActivity(activity);

            } catch (Exception e) {
                logger.warn("Failed to log phone update activity for candidate {} - correlationId: {}",
                        candidate.getId(), correlationId, e);
            }
        }

        // Helper class for update results
        @Data
        @Accessors(chain = true)
        public static class CandidateUpdateResult {
            private String candidateId;
            private String candidateName;
            private String candidateEmail;
            private String originalPhone;
            private String newPhone;
            private String parsingSource;
            private boolean success;
            private String message;
            private String errorMessage;
        }

        @GetMapping("/allUsersCandidatesByUser")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Response allUsersCandidatesByUser(Principal principal, @RequestParam String userId,  @RequestParam(defaultValue = "0") int page, // Default page 0
                                           @RequestParam(defaultValue = "10") int size)  {
            Pageable pageable = PageRequest.of(page, size);

            return Response.ok().setPayload(iCandidateService.getAllCandidates(pageable));
        }


        public static ResumeData extractJsonAndConvertToPojo(String input) throws IOException {
            // Regular expression to find the JSON part
            String jsonPattern = input.replace("Companies worked at", "companies-worked-at");
            System.out.println(jsonPattern);
            // Convert JSON string to POJO
            ObjectMapper objectMapper = new ObjectMapper();
            ResumeData resumeDataArray = objectMapper.readValue(jsonPattern, ResumeData.class);
            // Assuming there's only one JSON object in the array
            return resumeDataArray;

        }

        public static String extractAndReplaceExperience(String input) throws IOException {
            // Regular expression to find the experience array
            String experiencePattern = "'experience': \\[(.*?)\\]";
            Pattern pattern = Pattern.compile(experiencePattern, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String experienceArray = matcher.group(1);
                // Remove single quotes and newlines, and trim spaces
                experienceArray = experienceArray.replace("'", "").replace("\n", "").trim();
                // Replace multiple spaces with a single space
                experienceArray = experienceArray.replaceAll("\\s+", " ");
                // Create the new experience string in JSON format
                String newExperience = "'experience': [\"" + experienceArray + "\"]";
                // Replace the old experience array with the new one
                return input.substring(0, matcher.start()) + newExperience + input.substring(matcher.end());
            } else {
                throw new IOException("Experience array not found in the input string.");
            }
        }


//        @PostMapping(value = "/uploadResumeFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//        public Response uploadResumeFiles(Principal principal, @RequestPart(value = "files") List<MultipartFile> files) throws IOException {
//            Response response = new Response();
//            ResumeParsingResponeV2 root = new ResumeParsingResponeV2();
//            List<CandidateMidas> candidateResponses = new ArrayList<>();
//            List<String> errors = new ArrayList<>();
//
//            for (MultipartFile file : files) {
//                CreateCandidateRequestMidas createCandidateRequest = new CreateCandidateRequestMidas();
//                List<FileHandle> fileHandles = new ArrayList<>();
//                if (!file.isEmpty() && file.getSize() > 0) {
//                    okhttp3.Response response1 = oneDriveServices.uploadCandidateResume(file);
//                    FileHandle fileInfo = objectMapper.readValue(response1.body().string(), FileHandle.class);
//                    fileHandles.add(fileInfo);
//                    createCandidateRequest.setFileHandle(response1 != null ? fileInfo : null);
//                    // Save the file to the tempFiles directory
//                    String tempDir = System.getProperty("user.dir") + "/tempFiles";
//                    String originalFilename = file.getOriginalFilename();
//                    Path filePath = Paths.get(tempDir, originalFilename);
//                    ResumeData resumeData = new ResumeData();
//                    CandidateMidas candidateResponse = new CandidateMidas();
//                    try {
//                        Files.createDirectories(filePath.getParent());
//                        Files.write(filePath, file.getBytes());
//                        String mimeType = "application/pdf";
//                        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
//                        if ("doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
//                            mimeType = "application/msword";
//                        }
//                        Boolean doICallParse = iCandidateService.getCandidateByEmailFromDoc(filePath);
//                        if (doICallParse) {
//                            //                        throw new MidasCustomException.DuplicateEntityException("The candidate you are trying to create already exist");
//                            errors.add("Candidate with email " + iCandidateService.getCandidateEmailFromDoc(filePath) + " existed");
//                        }
////                         root = midasResumeParsingService.parseResume(filePath.toFile().getPath());
//                        String completeResponse = midasResumeParsingService.parseResumeString(filePath.toFile().getPath());
//                        JsonNode responseData = objectMapper.readTree(completeResponse);
//
//                        if (!responseData.has("message") || !"File processed successfully".equalsIgnoreCase(responseData.get("message").asText())) {
//                            response.setStatus(Response.Status.BAD_REQUEST).setErrors(completeResponse);
//                            Files.delete(filePath);
//                            return response;
//                        }
//                        root = objectMapper.treeToValue(responseData, ResumeParsingResponeV2.class);
//                        CandidateMapper.mapToCandidate(responseData);
//                        if (!root.getMessage().equalsIgnoreCase("File processed successfully")) {
//                            response.setStatus(Response.Status.BAD_REQUEST).setErrors("File is empty or invalid");
//                        }
//                        try {
//                            CandidateMidas candidateMidas = CandidateMapper.mapToCandidate(responseData);
//                            if (candidateMidas != null) {
//                                candidateResponse = iCandidateService.addCandidate(candidateMidas);
//                            }
//                            candidateResponse = iCandidateService.addCandidate(createCandidateRequest);
////                           candidateResponse= iCandidateService.addCandidate(candidateMidas);
//                        } catch (Exception ee) {
//                            errors.add(ee.getMessage());
//                        }
//                        Files.delete(filePath);
//                        //                    response.setStatus(Response.Status.OK).setPayload(candidateResponse);
//                        candidateResponses.add(candidateResponse);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        errors.add(e.getMessage());
//                        Files.delete(filePath);
//                    } catch (MidasCustomException.ParsingException e) {
//
//                        errors.add(e.getMessage());
//                        Files.delete(filePath);
//                    }
//                } else {
//                    //                response.setStatus(Response.Status.BAD_REQUEST).setErrors("File is empty or invalid").setPayload(errors);
//                    errors.add("File is empty or invalid");
//
//                }
//            }
//            response.setStatus(Response.Status.OK).setErrors(String.join(", ", errors)).setPayload(candidateResponses);
//            return response;
//        }
//

        @PostMapping(value = "/uploadResumeFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Response uploadResumeFiles(Principal principal, @RequestPart(value = "files") List<MultipartFile> files) {

            String correlationId = UUID.randomUUID().toString();
            logger.info("Starting resume upload - correlationId: {}, fileCount: {}", correlationId, files.size());

            Response response = new Response();
            List<CandidateMidas> candidateResponses = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Process each file
            for (MultipartFile file : files) {
                String fileCorrelationId = correlationId + "_" + UUID.randomUUID().toString().substring(0, 8);
                ProcessingResult result = processResumeFile(file, fileCorrelationId);

                if (result.isSuccess()) {
                    candidateResponses.add(result.getCandidate());
                } else {
                    errors.addAll(result.getErrors());
                }
            }

            // Set response based on results
            if (!candidateResponses.isEmpty() || errors.isEmpty()) {
                response.setStatus(Response.Status.OK)
                        .setErrors(errors.isEmpty() ? null : String.join(", ", errors))
                        .setPayload(candidateResponses);
            } else {
                response.setStatus(Response.Status.BAD_REQUEST)
                        .setErrors(String.join(", ", errors))
                        .setPayload(candidateResponses);
            }

            logger.info("Resume upload completed - correlationId: {}, successful: {}, errors: {}    ",
                    correlationId, candidateResponses.size(), errors.size());

            return response;
        }

        private ProcessingResult processResumeFile(MultipartFile file, String fileCorrelationId) {
            ProcessingResult result = new ProcessingResult();
            Path tempFilePath = null;
            FileHandle oneDriveFileHandle = null;

            try {
                // Validate file
                if (file.isEmpty() || file.getSize() == 0) {
                    result.addError("File is empty or invalid: " + file.getOriginalFilename());
                    return result;
                }

                logger.info("Processing file - correlationId: {}, fileName: {}, size: {}",
                        fileCorrelationId, file.getOriginalFilename(), file.getSize());

                // Step 1: Upload to OneDrive first (external dependency)
                oneDriveFileHandle = uploadToOneDrive(file, fileCorrelationId);

                // Step 2: Create temp file with proper resource management
                tempFilePath = createTempFile(file, fileCorrelationId);

                // Step 3: Check for duplicate candidate
                if (isCandidateDuplicate(tempFilePath, result)) {
                    return result; // Returns with error already set
                }

                // Step 4: Parse resume
                ResumeParsingResponeV2 parseResult = parseResume(tempFilePath, fileCorrelationId);
                if (parseResult == null) {
                    result.addError("Failed to parse resume: " + file.getOriginalFilename());
                    return result;
                }

                // Step 5: Create and save candidate
                CandidateMidas candidate = createCandidate(parseResult, oneDriveFileHandle, fileCorrelationId);
                if (candidate != null) {
                    result.setCandidate(candidate);
                    result.setSuccess(true);
                    logger.info("Successfully processed file - correlationId: {}, candidateId: {}",
                            fileCorrelationId, candidate.getId());
                } else {
                    result.addError("Failed to create candidate from parsed data");
                }

            } catch (Exception e) {
                logger.error("Error processing file - correlationId: {}, fileName: {}",
                        fileCorrelationId, file.getOriginalFilename(), e);
                result.addError("Processing failed: " + e.getMessage());
            } finally {
                // Critical: Always cleanup temp file
                cleanupTempFile(tempFilePath, fileCorrelationId);
            }

            return result;
        }

        private FileHandle uploadToOneDrive(MultipartFile file, String correlationId) throws IOException {
            try {
                logger.debug("Uploading to OneDrive - correlationId: {}", correlationId);
                okhttp3.Response response = oneDriveServices.uploadCandidateResume(file);

                if (!response.isSuccessful()) {
                    throw new IOException("OneDrive upload failed with status: " + response.code());
                }

                String responseBody = response.body().string();
                FileHandle fileHandle = objectMapper.readValue(responseBody, FileHandle.class);

                logger.debug("OneDrive upload successful - correlationId: {}, fileId: {}",
                        correlationId, fileHandle.getId());

                return fileHandle;

            } catch (Exception e) {
                logger.error("OneDrive upload failed - correlationId: {}", correlationId, e);
                throw new IOException("Failed to upload to OneDrive: " + e.getMessage(), e);
            }
        }

        private Path createTempFile(MultipartFile file, String correlationId) throws IOException {
            try {
                // Create unique filename to prevent conflicts
                String uniqueFileName = System.currentTimeMillis() + "_" +
                        correlationId + "_" +
                        sanitizeFileName(file.getOriginalFilename());

                Path tempDir = Paths.get(tempDirectory);
                Files.createDirectories(tempDir);

                Path tempFilePath = tempDir.resolve(uniqueFileName);

                // Use efficient NIO copy with proper resource management
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                }

                logger.debug("Temp file created - correlationId: {}, path: {}", correlationId, tempFilePath);
                return tempFilePath;

            } catch (IOException e) {
                logger.error("Failed to create temp file - correlationId: {}", correlationId, e);
                throw new IOException("Failed to create temporary file: " + e.getMessage(), e);
            }
        }

        private String sanitizeFileName(String fileName) {
            if (fileName == null) return "unknown_file";

            // Remove or replace problematic characters
            return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                    .replaceAll("_{2,}", "_"); // Replace multiple underscores with single
        }

        private boolean isCandidateDuplicate(Path filePath, ProcessingResult result) {
            try {
                Boolean isDuplicate = iCandidateService.getCandidateByEmailFromDoc(filePath);
                if (Boolean.TRUE.equals(isDuplicate)) {
                    String email = iCandidateService.getCandidateEmailFromDoc(filePath);
                    String errorMsg = "Candidate with email " + email + " already exists";
                    result.addError(errorMsg);
                    logger.warn("Duplicate candidate detected: {}", email);
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.warn("Error checking for duplicate candidate", e);
                // Don't fail the process for duplicate check errors
                return false;
            }
        }

        private ResumeParsingResponeV2 parseResume(Path filePath, String correlationId) {
            try {
                logger.debug("Parsing resume - correlationId: {}", correlationId);

                String completeResponse = midasResumeParsingService.parseResumeStr(filePath.toString());
                JsonNode responseData = objectMapper.readTree(completeResponse);

                // Validate parsing response
                if (!responseData.has("message") ||
                        !"File processed successfully".equalsIgnoreCase(responseData.get("message").asText())) {
                    logger.error("Resume parsing failed - correlationId: {}, response: {}", correlationId, completeResponse);
                    return null;
                }

                ResumeParsingResponeV2 parseResult = objectMapper.treeToValue(responseData, ResumeParsingResponeV2.class);
                logger.debug("Resume parsing successful - correlationId: {}", correlationId);

                return parseResult;

            } catch (Exception e) {
                logger.error("Error parsing resume - correlationId: {}", correlationId, e);
                return null;
            }
        }

        private ResumeParsingResponeV2 parseResumeFullText(String fullText) {
            try {
                logger.debug("Parsing resume - correlationId: {}");

                String completeResponse = midasResumeParsingService.parseResumeString(fullText);
                JsonNode responseData = objectMapper.readTree(completeResponse);

                // Validate parsing response
                if (!responseData.has("message") &&
                        !"File processed successfully".equalsIgnoreCase(responseData.get("message").asText())) {
                    logger.error("Resume parsing failed - response: {}", completeResponse);
                    return null;
                }

                ResumeParsingResponeV2 parseResult = objectMapper.treeToValue(responseData, ResumeParsingResponeV2.class);
                logger.debug("Resume parsing successful - correlationId:" );

                return parseResult;

            } catch (Exception e) {
                logger.error("Error parsing resume - correlationId: {}", e.getMessage());
                return null;
            }
        }

        private CandidateMidas createCandidate(ResumeParsingResponeV2 parseResult, FileHandle fileHandle, String correlationId) {
            try {
                logger.debug("Creating candidate - correlationId: {}", correlationId);

                // Map parsed data to candidate
                JsonNode responseData = objectMapper.valueToTree(parseResult);
                CandidateMidas candidateMidas = CandidateMapper.mapToCandidate(responseData);

                if (candidateMidas == null) {
                    logger.error("Failed to map parsed data to candidate - correlationId: {}", correlationId);
                    return null;
                }

                // Create request with file handle
                CreateCandidateRequestMidas createRequest = new CreateCandidateRequestMidas();
                createRequest.setFileHandle(fileHandle);
candidateMidas.setFileHandle(fileHandle);
                // Save candidate
                CandidateMidas savedCandidate = iCandidateService.addCandidate(candidateMidas);

                logger.debug("Candidate created successfully - correlationId: {}, candidateId: {}",
                        correlationId, savedCandidate.getId());

                return savedCandidate;

            } catch (Exception e) {
                logger.error("Error creating candidate - correlationId: {}", correlationId, e);
                return null;
            }
        }

        private void cleanupTempFile(Path tempFilePath, String correlationId) {
            if (tempFilePath != null && Files.exists(tempFilePath)) {
                try {
                    boolean deleted = deleteFileWithRetry(tempFilePath, 3, 100);
                    if (deleted) {
                        logger.debug("Temp file cleaned up successfully - correlationId: {}", correlationId);
                    } else {
                        logger.warn("Failed to delete temp file after retries - correlationId: {}, file: {}",
                                correlationId, tempFilePath);
                        // Last resort - mark for deletion on JVM exit
                        tempFilePath.toFile().deleteOnExit();
                    }
                } catch (Exception e) {
                    logger.error("Error during temp file cleanup - correlationId: {}, file: {}",
                            correlationId, tempFilePath, e);
                    tempFilePath.toFile().deleteOnExit();
                }
            }
        }

        private boolean deleteFileWithRetry(Path file, int maxRetries, long initialDelayMs) {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    Files.delete(file);
                    return true;
                } catch (IOException e) {
                    logger.debug("Delete attempt {} failed for file {}: {}", attempt + 1, file, e.getMessage());

                    if (attempt < maxRetries - 1) {
                        try {
                            Thread.sleep(initialDelayMs * (1L << attempt)); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        // Helper class for processing results
        private static class ProcessingResult {
            private boolean success = false;
            private CandidateMidas candidate;
            private List<String> errors = new ArrayList<>();

            public boolean isSuccess() { return success; }
            public void setSuccess(boolean success) { this.success = success; }

            public CandidateMidas getCandidate() { return candidate; }
            public void setCandidate(CandidateMidas candidate) { this.candidate = candidate; }

            public List<String> getErrors() { return errors; }
            public void addError(String error) { this.errors.add(error); }
        }
    private CreateCandidateRequestMidas settleName(CreateCandidateRequestMidas createCandidateRequest) {

        String fullName = createCandidateRequest.getName();
        String[] nameArr = fullName.split(" ");

        String firstName = "";
        String middleName = "";
        String lastName = "";

// Handle different name cases
        if (nameArr.length == 1) {
            // If only one name is provided, assume it's the first name
            firstName = nameArr[0];
        } else if (nameArr.length == 2) {
            // If two names are provided, assume first name and last name
            firstName = nameArr[0];
            lastName = nameArr[1];
        } else if (nameArr.length >= 3) {
            // If three or more names are provided, treat it as first, middle, and last name
            firstName = nameArr[0];
            middleName = nameArr[1];
            lastName = nameArr[2];

            // If more than three names exist, combine the extra ones into the last name
            if (nameArr.length > 3) {
                for (int i = 3; i < nameArr.length; i++) {
                    lastName += " " + nameArr[i];
                }
            }
        }

// Set the values in the request
        createCandidateRequest.setName(firstName);
        createCandidateRequest.setLastName(middleName+" "+lastName);

        return  createCandidateRequest;
    }


        @GetMapping("/searchFreeTextN")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchCandidatesN(Principal principal, @RequestParam String searchText) {
                return iCandidateService.searchCandidatesByNormalizedPhone(searchText);
        }

        @GetMapping("/searchLatest")
        public ResponseEntity<MidasCandidateServiceImpl.SearchResult<CandidateMidas>> searchCandidates(
                @RequestParam String q,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "50") int size) {

            MidasCandidateServiceImpl.SearchResult<CandidateMidas> result = iCandidateService.searchCandidatesWithCount(q, page, size);
            return ResponseEntity.ok(result);
        }
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        @GetMapping("/distinct-skills")
        public Set<String> getDistinctSkills() {
            return iCandidateService.getDistinctSkills();
        }

        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        @GetMapping("/autocomplete-skills")
        public Set<String> getSkillSuggestions(@RequestParam String query) {
            return iCandidateService.getSkillSuggestions(query);
        }

    ModelMapper modelMapper = new ModelMapper();
        @PostMapping("/uploadResumeFile")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Response uploadResumeFile(Principal principal, @RequestPart(value = "file") MultipartFile file, Boolean overrideExisting) throws IOException {
            Response response = new Response();
            ResumeParsingRespone root = new ResumeParsingRespone();
    //        CreateCandidateRequest createCandidateRequest = new CreateCandidateRequest();
                List<FileHandle> fileHandles = new ArrayList<>();

            CreateCandidateRequestMidas createCandidateRequest = new CreateCandidateRequestMidas();
            if (!file.isEmpty() && file.getSize() > 0) {
                okhttp3.Response response1 = oneDriveServices.uploadCandidateResume(file);
                FileHandle fileInfo = objectMapper.readValue(response1.body().string(), FileHandle.class);
                fileHandles.add(fileInfo);
                createCandidateRequest.setFileHandle(response1 != null ? fileInfo : null);
                // Save the file to the tempFiles directory
                String tempDir = System.getProperty("user.dir") + "/tempFiles";
                String originalFilename = file.getOriginalFilename();
                Path filePath = Paths.get(tempDir, originalFilename);
                ResumeData resumeData = new ResumeData();
                CandidateMidas candidateResponse = new CandidateMidas();
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, file.getBytes());
                    String mimeType = "application/pdf";
                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
                    if ("doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
                        mimeType = "application/msword";
                    }
                    Boolean doICallParse = iCandidateService.getCandidateByEmailFromDoc(filePath);
                    if (doICallParse) {
    //                    throw new MidasCustomException.DuplicateEntityException("The candidate you are trying to create already exist");
                        Optional<CandidateMidas> candidateMidas= iCandidateService.getCandidateByEmail(iCandidateService.getCandidateEmailFromDoc(filePath));
                        if (candidateMidas.isPresent())
                                return     response.setStatus(Response.Status.DUPLICATE_ENTITY).setPayload(candidateMidas.get());
                        else
                            return     response.setStatus(Response.Status.DUPLICATE_ENTITY).setPayload("Candidate was not found in database");
                    }
                    root = midasResumeParsingService.parseResume(filePath.toFile().getPath());

                    if (!root.getMessage().equalsIgnoreCase("File processed successfully")){
                        response.setStatus(Response.Status.BAD_REQUEST).setErrors("File is empty or invalid");
                    }
                    else{
                        createCandidateRequest=   modelMapper.map(root.getParsed(), CreateCandidateRequestMidas.class );
                        createCandidateRequest.setCompaniesWorkedAt(root.getParsed().getCompaniesworkedat());
                        fileHandles.add(fileInfo);
                        createCandidateRequest.setFileHandle(response1 != null ? fileInfo : null);
                    }
                    candidateResponse = iCandidateService.addCandidate(createCandidateRequest);
                    response.setStatus(Response.Status.OK).setPayload(candidateResponse);

                    Files.delete(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    response.setStatus(Response.Status.INTERNAL_SERVER_ERROR).setErrors("Could not save file: " + e.getMessage());
                    Files.delete(filePath);
                    return response;
                } catch (MidasCustomException.ParsingException e) {
                    response.setStatus(Response.Status.NOT_FOUND).setErrors("Could not save file: " + e.getMessage());
                    Files.delete(filePath);
                    return response;
                }
            } else {
                response.setStatus(Response.Status.BAD_REQUEST).setErrors("File is empty or invalid");
            }

            return response;
        }


        @GetMapping("/searchFreeText")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchCandidates(Principal principal, @RequestParam String searchText) {
            return iCandidateService.searchAcrossAllFieldsN(searchText);
        }
        @GetMapping("/searchFreeTextExact")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchFreeTextExact(Principal principal, @RequestParam String searchText) {
            return iCandidateService.searchAcrossAllFieldsSafelyExact(searchText);
        }
        @GetMapping("/searchCandidateResumes")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchResumes(
                @RequestParam(required = false, defaultValue = "") List<String> plusKeywords,
                @RequestParam(required = false, defaultValue = "") List<String> minusKeywords,
                @RequestParam(required = false, defaultValue = "") List<String> orPlusKeywords) {

            return iCandidateService.searchCandidateResumes(plusKeywords, minusKeywords, orPlusKeywords);
        }

        @GetMapping("/searchSkills")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchCandidates(
                @RequestParam (required = false) List<String> andCriteria,
                @RequestParam (required = false) List<String> orCriteria,
                @RequestParam (required = false) List<String> notCriteria) {


            if (andCriteria == null) {
                andCriteria = new ArrayList<>();
            }
            if (orCriteria == null) {
                orCriteria = new ArrayList<>();
            }
            if (notCriteria == null) {
                notCriteria = new ArrayList<>();
            }



    //        return iCandidateService.findCandidates(andCriteria1, orCriteria1, notCriteria1);
            return iCandidateService.findCandidatesBySkills(andCriteria, orCriteria, notCriteria);

        }

        @GetMapping("/searchSkillsPaged")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Page<CandidateMidas> searchCandidatesPage(
                @RequestParam(required = false) List<String> andCriteria,
                @RequestParam(required = false) List<String> orCriteria,
                @RequestParam(required = false) List<String> notCriteria,
                @RequestParam(defaultValue = "0") int page, // Default page 0
                @RequestParam(defaultValue = "10") int size) { // Default size 10

            if (andCriteria == null) {
                andCriteria = new ArrayList<>();
            }
            if (orCriteria == null) {
                orCriteria = new ArrayList<>();
            }
            if (notCriteria == null) {
                notCriteria = new ArrayList<>();
            }

    //        Pageable pageable = PageRequest.of(page, size);
            Pageable pageable = PageRequest.of(page, size);

            // Fetch the page of candidates
            Page<CandidateMidas> candidatesPage = iCandidateService.findCandidatesBySkills(andCriteria, orCriteria, notCriteria, pageable);

            // Iterate over the candidates and apply phone number formatting
            candidatesPage.forEach(candidate -> {
                if (candidate.getPhone() != null) {
                    String formattedPhone = PhoneNumberFormatter.formatPhoneNumber(candidate.getPhone());
                    candidate.setPhone(formattedPhone); // Ensure there is a setter for the phone field
                }
            });

            return candidatesPage;
        }

        private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        public String formatPhoneNumber( String phoneNumber) {
            try {
                // Parse the phone number (assuming US as default region, you can change this)
                Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, "US");

                // Format the phone number to a common format (e.g., "XXX-XXX-XXXX")
                String formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

                return formattedNumber;
            } catch (NumberParseException e) {
                return "Invalid phone number format: " + phoneNumber;
            }
        }
        @GetMapping("/searchCandidateResumesPaged")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Page<CandidateMidas> searchResumes(
                @RequestParam(required = false, defaultValue = "") List<String> plusKeywords,
                @RequestParam(required = false, defaultValue = "") List<String> minusKeywords,
                @RequestParam(required = false, defaultValue = "") List<String> orPlusKeywords,
                @RequestParam(defaultValue = "0") int page, // Default page is 0
                @RequestParam(defaultValue = "10") int size) { // Default size is 10

            Pageable pageable = PageRequest.of(page, size);

            // Fetch the page of candidates
            Page<CandidateMidas> candidatesPage = iCandidateService.searchCandidateResumesPaged(plusKeywords, minusKeywords, orPlusKeywords, pageable);

            // Iterate over the candidates and apply phone number formatting
            candidatesPage.forEach(candidate -> {
                if (candidate.getPhone() != null) {
                    String formattedPhone = PhoneNumberFormatter.formatPhoneNumber(candidate.getPhone());
                    candidate.setPhone(formattedPhone); // Ensure there is a setter for the phone field
                }
            });

            return candidatesPage;

        }

    @PostMapping("/uploadConversionFile")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Object> uploadAndConvertToPDF(Principal principal,@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please upload a file", HttpStatus.BAD_REQUEST);
        }

        String pdfFilePath = "converted.pdf"; // You can set a dynamic file path here
        try {
            // Save the uploaded file temporarily
            File tempFile = File.createTempFile("upload-", ".docx");
            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(file.getBytes());
            }

            // Convert the uploaded .docx file to PDF
            ConvertToPDF(tempFile.getAbsolutePath(), pdfFilePath);

            // Read the PDF file into byte array
            byte[] pdfBytes = readFileToByteArray(pdfFilePath);

            // Set response headers for downloading PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "converted.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error converting the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
        public static byte[] readFileToByteArray(String filePath) {
            try (FileInputStream fis = new FileInputStream(new File(filePath));
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void ConvertToPDF(String docPath, String pdfPath) {
            try {
                InputStream doc = new FileInputStream(new File(docPath));
                XWPFDocument document = new XWPFDocument(doc);
                PdfOptions options = PdfOptions.create();
                OutputStream out = new FileOutputStream(new File(pdfPath));
                PdfConverter.getInstance().convert(document, out, options);
                document.close();
                out.close();
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        @GetMapping("/searchOnAll")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchCandidates(

                @RequestParam(required = false) String email,
                @RequestParam(required = false) String phone
        ) {

            String emailRegex = (email != null) ? ".*" + email + ".*" : ".*";
            String phoneRegex = (phone != null) ? ".*" + phone + ".*" : ".*";


            return iCandidateService.searchCandidates(emailRegex, phoneRegex);
        }

        @GetMapping("/candidateById/{id}")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        private Response candidateById(@PathVariable String id, Principal principal) {

            Response response = new Response();
            response.setStatus(Response.Status.OK).setPayload(iCandidateService.getCandidateById(id));
            return response;
        }

        @PatchMapping("/updateCandidateById")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        private Response updateCandidateById(Principal principal, @RequestBody CreateCandidateRequestMidas createCandidateRequest) throws Exception {
            Response response
                    = new Response();
            System.err.println("DDDDDDD:"+new Gson().toJson(createCandidateRequest));
            UserDto userDto= userService.findUserByEmail(principal.getName());
            Optional<CandidateMidas> candidateMidas=  iCandidateService.getCandidateById(createCandidateRequest.getId());if (candidateMidas.isPresent()){

                CandidateMidas candidateToBeUpdated= modelMapper.map(createCandidateRequest,CandidateMidas.class)
                        ;
                updateFields(candidateMidas.get(),candidateToBeUpdated, UserMapper.toUser( userDto),candidateMidas.get());
                response.setStatus(Response.Status.OK).setPayload(iCandidateService.updateCandidate(createCandidateRequest));
            return  response;
            }
              return response;
        }
    private boolean areEqual(Object oldValue, Object newValue) {
        return (oldValue == null ? "" : oldValue.toString().trim()).equalsIgnoreCase(
                (newValue == null ? "" : newValue.toString().trim()));
    }
        private String objectToString(Object obj) {
            return (obj == null) ? "null" : obj.toString();
        }

        private void checkAndRecordChange(String fieldName, Object oldValue, Object newValue, Map<String, List<String>> changes) {
        if (!areEqual(oldValue, newValue)) {
            changes.put(fieldName, Arrays.asList(objectToString(oldValue), objectToString(newValue)));
        }
    }
    public String updateFields(CandidateMidas existing, CandidateMidas incoming, User user, CandidateMidas candidateMidas) throws IllegalAccessException, IOException {
        Map<String, List<String>> changes = new HashMap<>();

        // Helper method to compare and record changes for basic fields
        checkAndRecordChange("Email", existing.getEmail(), incoming.getEmail(), changes);
        checkAndRecordChange("Phone", existing.getPhone(), incoming.getPhone(), changes);
        checkAndRecordChange("ID", existing.getId(), incoming.getId(), changes);
        checkAndRecordChange("Current CTC", existing.getCurrentCTC(), incoming.getCurrentCTC(), changes);
        checkAndRecordChange("Name", existing.getName(), incoming.getName(), changes);
        checkAndRecordChange("Date of Birth", existing.getDateOfBirth(), incoming.getDateOfBirth(), changes);

        checkAndRecordChange("Total Experience", existing.getTotalExp(), incoming.getTotalExp(), changes);
        checkAndRecordChange("Regions", existing.getRegions(), incoming.getRegions(), changes);
        checkAndRecordChange("Municipality", existing.getMunicipality(), incoming.getMunicipality(), changes);
        checkAndRecordChange("Profession", existing.getProfession(), incoming.getProfession(), changes);
        checkAndRecordChange("PrimarySpeciality", existing.getPrimarySpeciality(), incoming.getPrimarySpeciality(), changes);
        checkAndRecordChange("DesiredShifts", existing.getDesiredShifts(), incoming.getDesiredShifts(), changes);
        checkAndRecordChange("OtherPhone", existing.getOtherPhone(), incoming.getOtherPhone(), changes);
        checkAndRecordChange("WorkAuthorization", existing.getWorkAuthorization(), incoming.getWorkAuthorization(), changes);
        checkAndRecordChange("Gender", existing.getGender(), incoming.getGender(), changes);
        checkAndRecordChange("Full Text", existing.getFullText(), incoming.getFullText(), changes);

        // Prepare activity logger for changes
        StringBuilder changedValue = new StringBuilder();
        if (!changes.isEmpty()) {
            Activity activityRequest = new Activity();
            changedValue.append("<h3 style='color: #1e90ff;'>System Generated Changes:</h3><table style='width: 100%; border-collapse: collapse;'>");
            changedValue.append("<thead><tr style='background-color: #1e90ff; color: white;'><th>Field</th><th>Old Value</th><th>New Value</th></tr></thead>");
            changedValue.append("<tbody>");

            for (Map.Entry<String, List<String>> entry : changes.entrySet()) {
                if (entry.getKey().equals("Education")) {
                    changedValue.append(formatEducationChange(entry.getValue().get(0), entry.getValue().get(1)));
                } else {
                    changedValue.append(formatChange(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1)));
                }
            }

            changedValue.append("</tbody></table>");

            activityRequest.setActivityNote(changedValue.toString());
            activityRequest.setActivityType(ActivityType.SYSTEM_GEN_CANDIDATE_MODIFIED);
            activityRequest.setDateCreated(new Date());
            activityRequest.setCandidateID(candidateMidas.getId());
            activityRequest.setUserID(user);

            activityService.createOrUpdateActivity(activityRequest);
        }

        return changedValue.toString();
    }

        // New helper method to handle education details
        private String formatEducationChange(Object oldValue, Object newValue) {
            StringBuilder html = new StringBuilder();

            // Assuming oldValue and newValue are lists of education objects
            List<Education> oldEducation = parseEducationList(oldValue);
            List<Education> newEducation = parseEducationList(newValue);

            html.append("<tr><td colspan='3'><b>Education Details</b></td></tr>");
            for (int i = 0; i < newEducation.size(); i++) {
                Education newEdu = newEducation.get(i);
                Education oldEdu = (i < oldEducation.size()) ? oldEducation.get(i) : null;

                html.append("<tr><td>Degree</td>")
                        .append("<td>").append(oldEdu != null ? oldEdu.getDegree() : "N/A").append("</td>")
                        .append("<td>").append(newEdu.getDegree()).append("</td></tr>");



                html.append("<tr><td>Major</td>")
                        .append("<td>").append(oldEdu != null ? oldEdu.getInstitution() : "N/A").append("</td>")
                        .append("<td>").append(newEdu.getInstitution()).append("</td></tr>");

                html.append("<tr><td>Location</td>")
                        .append("<td>").append(oldEdu != null ? oldEdu.getLocation() : "N/A").append("</td>")
                        .append("<td>").append(newEdu.getLocation()).append("</td></tr>");

                // Add more fields if needed...
            }

            return html.toString();
        }

        // Helper method to safely convert list to string
        private String listToString(List<String> list) {
            return (list != null && !list.isEmpty()) ? String.join(", ", list) : "N/A";
        }

        // Parse object to list of education
        private List<Education> parseEducationList(Object obj) {
            return (obj != null && obj instanceof List) ? (List<Education>) obj : new ArrayList<>();
        }

    // Other existing methods...

        private boolean isDifferent(String oldValue, String newValue) {
            return oldValue == null || newValue == null || !oldValue.trim().equals(newValue.trim());
        }

        private boolean contentEquals(List<Object> list1, List<Object> list2) {
            if (list1 == null || list2 == null || list1.size() != list2.size()) {
                return false; // Different sizes or one of the lists is null
            }
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).equals(list2.get(i))) {
                    return false; // Found a difference
                }
            }
            return true; // All elements are equal
        }

        @GetMapping("/searchCandidateResumesPagedCityState1")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public Page<CandidateMidas> searchCandidateResumesPagedCityState1(
                @RequestParam(required = false, defaultValue = "") String booleanExp,
                @RequestParam(required = false, defaultValue = "") String city,
                @RequestParam(required = false, defaultValue = "") String zip,
                @RequestParam(required = false, defaultValue = "") String state,
                @RequestParam(required = false, defaultValue = "") Integer radiusMiles,
                @RequestParam(defaultValue = "0") int page, // Default page is 0
                @RequestParam(defaultValue = "10") int size) { // Default size is 10

            logger.info("Initial Search String: " + booleanExp);

            Pageable pageable = PageRequest.of(page, size);

            List<Location> locationList = new ArrayList<>();
            // Add city and radius-based filtering
            if (!StringUtils.isEmpty(city) && radiusMiles != null && radiusMiles > 0) {
                locationList = locationService.findLocationsByZipOrStateAndRadius(city, radiusMiles);
                booleanExp = getBooleanCreated(booleanExp, locationList);
            }

            // Add zip-based filtering
            if (!StringUtils.isEmpty(zip)) {
                if (!StringUtils.isEmpty(booleanExp)) {
                    booleanExp += " AND ";
                }
                booleanExp += zip;
            }

            // Add city filtering if no radius is provided
            if (!StringUtils.isEmpty(city) && (radiusMiles == null || radiusMiles <= 0)) {
                if (!StringUtils.isEmpty(booleanExp)) {
                    booleanExp += " AND ";
                }
                booleanExp += city;
            }

            // Add state filtering
            if (!StringUtils.isEmpty(state)) {
                if (!StringUtils.isEmpty(booleanExp)) {
                    booleanExp += " AND ";
                }
                booleanExp += " "+state+" ";
            }

            logger.info("Final Search String: " + booleanExp);

            // Fetch the page of candidates
            Page<CandidateMidas> candidatesPage = iCandidateService.searchCandidateResumesCityStatePaged(booleanExp, pageable);

            // Format phone numbers for the candidates
            candidatesPage.forEach(candidate -> {
                if (candidate.getPhone() != null) {
                    String formattedPhone = PhoneNumberFormatter.formatPhoneNumber(candidate.getPhone());
                    candidate.setPhone(formattedPhone);
                }
            });

            return candidatesPage;
        }
        private String getBooleanCreated(String booleanExp, List<Location> locationList) {
            StringBuilder booleanExpBuilder = new StringBuilder(booleanExp);
            booleanExpBuilder.append(" AND ");
    //        locationList.forEach(location -> booleanExpBuilder.append(" AND ").append(location.getCity()));
            for (int i = 0; i < locationList.size(); i++) {
                Location location = locationList.get(i);
                if (i > 0) {
                    booleanExpBuilder.append(" OR ");
                }
                booleanExpBuilder.append(location.getCity());
            }
            return booleanExpBuilder.toString();
        }

        // Helper method to format the changes (old value -> new value) in tabular row format
        private String formatChange(String fieldName, Object oldValue, Object newValue) {
            return "<tr style='border: 1px solid #dddddd;'><td style='padding: 8px;'>" + fieldName + "</td>"
                    + "<td style='padding: 8px;'>" + (oldValue == null ? "null" : oldValue) + "</td>"
                    + "<td style='padding: 8px;'>" + (newValue == null ? "null" : newValue) + "</td></tr>";
        }

        @GetMapping("/allCandidates")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        private Response allCandidates() {
            Response response
                    = new Response();

            List<CandidateMidas> candidateMidas =iCandidateService.getAllCandidates();
            response.setStatus(Response.Status.OK).setPayload(candidateMidas);
            return response;
        }

        @GetMapping("/allCandidatesPaginate")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public ResponsePaginate allCandidatesPaginate(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {

            ResponsePaginate response = new ResponsePaginate();

            // Create a Pageable object using the page number and size
            Pageable pageable = PageRequest.of(page, size);

            // Modify the service call to handle pagination
            Page<CandidateMidas> paginatedCandidates = iCandidateService.getAllCandidates(pageable);

            response.setStatus(Response.Status.OK)
                    .setPayload(paginatedCandidates.getContent()) // Get the paginated list of candidates
                    .setTotalElements(paginatedCandidates.getTotalElements()) // Optional: to return total count
                    .setTotalPages(paginatedCandidates.getTotalPages()); // Optional: to return total pages

            return response;
        }






    @Data
    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
        public class ResponsePaginate<T> {
            private Response.Status status;
            private  T payload;
            private long totalElements;
            private int totalPages;
            }

        @PostMapping("/search")
        @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
        public List<CandidateMidas> searchCandidates(@RequestBody CandidateSearchRequest searchCriteria) {
            return iCandidateService.searchCandidates(

                    searchCriteria.getEmail(),
                    searchCriteria.getPhone()

            );
        }



        // Add this pre-handle method to set tenant context from the header
        @ModelAttribute
        public void setTenantContext(HttpServletRequest request) {
            String tenantId = request.getHeader("X-Tenant");
            logger.info("Setting tenant context in controller: {}", tenantId);
            TenantContext.setCurrentTenant(tenantId);
        }

        // Add this after-completion method to clear tenant context
        @ResponseStatus
        public void clearTenantContext() {
            logger.info("Clearing tenant context in controller");
            TenantContext.clear();
        }
    }