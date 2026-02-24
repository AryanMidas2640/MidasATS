
package com.midas.consulting.controller.v1.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.midas.consulting.controller.v1.response.candidate.CandidateChangeLogResponse;
import com.midas.consulting.model.Note;
import com.midas.consulting.service.ActivityService;
import com.midas.consulting.service.NoteService;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.candidate.IMidasCandidateService;
import com.midas.consulting.service.hrms.ClientsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class ReportsController {

    private final Logger log = LoggerFactory.getLogger(ReportsController.class);
    private final IMidasCandidateService iMidasCandidateService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final ClientsService clientsService;
    private final ActivityService activityService;
    private final NoteService noteService;
    private final ModelMapper modelMapper;

    @Autowired
    public ReportsController(IMidasCandidateService iMidasCandidateService,
                             NoteService noteService,
                             ActivityService activityService,
                             AuthenticationManager authenticationManager,
                             UserService userService,
                             ClientsService clientsService) {
        this.iMidasCandidateService = iMidasCandidateService;
        this.authenticationManager = authenticationManager;
        this.noteService = noteService;
        this.userService = userService;
        this.clientsService = clientsService;
        this.activityService = activityService;
        this.modelMapper = new ModelMapper();
    }

    @PostMapping("/fetchNotesBasedOnCriteria")
    @ApiOperation(value = "Fetch notes based on criteria with timeout handling",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Note>> fetchNotesBasedOnCriteria(
            @RequestBody Map<String, List<String>> options) {
        try {
            log.info("Fetching notes with criteria: {}", options);
            List<Note> notes = noteService.fetchNotesBasedOnCriteria(options);
            log.info("Successfully fetched {} notes", notes.size());
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error fetching notes based on criteria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/fetchbetweendates")
    @ApiOperation(value = "Fetch candidates with change logs between dates with timeout handling",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<CandidateChangeLogResponse>> getCandidatesWithChangeLogsDates(
            @RequestBody CandidateChangeLogRequest request) {
        try {
            log.info("Fetching candidates between dates: {} to {} for users: {}",
                    request.getFromDate(), request.getToDate(), request.getUserId());

            // Validate request
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                log.warn("No user IDs provided in request");
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            Date fromDate = request.getFromDate() != null ?
                    Date.from(request.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
            Date toDate = request.getToDate() != null ?
                    Date.from(request.getToDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()) : null;

            List<CandidateChangeLogResponse> response =
                    iMidasCandidateService.getCandidatesAndChangeLogs(request.getUserId(), fromDate, toDate);

            log.info("Successfully fetched {} candidate change logs", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching candidates with change logs between dates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/fetch")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<CandidateChangeLogResponse>> getCandidatesWithChangeLogs(
            @RequestBody Map<String, List<String>> options) {

        long startTime = System.currentTimeMillis();
        try {
            log.info("Starting getCandidatesWithChangeLogs with {} user IDs",
                    options.get("userId") != null ? options.get("userId").size() : 0);

            List<String> userIds = options.get("userId");

            // Add validation
            if (userIds == null || userIds.isEmpty()) {
                log.warn("No user IDs provided");
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            // Limit the number of users to prevent timeout (CRITICAL FIX)
            if (userIds.size() > 25) {
                log.warn("Too many user IDs provided: {}. Limiting to first 25 to prevent timeout.", userIds.size());
                userIds = userIds.subList(0, 25);
            }

            log.info("Processing {} user IDs", userIds.size());
            List<CandidateChangeLogResponse> response = iMidasCandidateService.getCandidatesAndChangeLogs(userIds);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully completed getCandidatesWithChangeLogs in {}ms for {} users, returned {} responses",
                    duration, userIds.size(), response.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error in getCandidatesWithChangeLogs after {}ms: {}", duration, e.getMessage(), e);

            // Return empty list instead of error to prevent client-side issues
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/fetchResumeUploadsBasedOnCriteria")
    @ApiOperation(value = "Fetch resume uploads based on criteria with pagination",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Map<String, Object>> fetchResumeUploadsBasedOnCriteria(
            @RequestBody Map<String, List<String>> options,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            log.info("Fetching resume uploads with criteria: {}, page: {}, size: {}", options, page, size);

            // Validate pagination parameters
            page = Math.max(page, 1);
            size = Math.max(Math.min(size, 100), 1); // Limit size to 100

            // TODO: Implement the actual logic here
            Map<String, Object> result = new HashMap<>();
            result.put("candidates", Collections.emptyList());
            result.put("totalElements", 0);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("message", "Feature not yet implemented");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching resume uploads: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Internal server error"));
        }
    }

    @PostMapping("/fetchNotesBasedOnUserAndCriteria")
    @ApiOperation(value = "Fetch notes based on user and criteria with timeout handling",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Note>> fetchNotesBasedOnUserAndCriteria(
            @RequestBody Map<String, Object> options) {
        try {
            log.info("Fetching notes based on user and criteria: {}", options);
            List<Note> notes = noteService.fetchNotesBasedOnUserAndCriteria(options);
            log.info("Successfully fetched {} notes", notes.size());
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error fetching notes based on user and criteria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/fetchNotesBasedOnCriteriaV2")
    @ApiOperation(value = "Fetch notes based on criteria V2 with enhanced validation",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Note>> fetchNotesBasedOnCriteriaV2(
            @RequestBody
            @Schema(
                    description = "Filter options (only 'userEmail' and 'dateCreated' supported)",
                    example = "{\"userEmail\": [\"user1@example.com\"], \"dateCreated\": [\"2025-01-01\", \"2025-02-01\"]}"
            )
            Map<String, List<String>> options) {

        if (options == null || options.getOrDefault("userEmail", Collections.emptyList()).isEmpty()) {
            log.warn("Invalid request - userEmail is required");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            log.info("Fetching notes V2 with criteria: {}", options);
            List<Note> notes = noteService.fetchNotesBasedOnCriteria(options);
            log.info("Successfully fetched {} notes", notes.size());
            return ResponseEntity.ok(notes);

        } catch (IllegalArgumentException iae) {
            log.warn("Invalid filter criteria: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error fetching notes V2: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @Data
    @Getter
    @Setter
    public static class CandidateChangeLogRequest {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
        private LocalDate fromDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
        private LocalDate toDate;

        @NotNull
        @NotEmpty
        List<String> userId;
    }
}

//package com.midas.consulting.controller.v1.api;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.midas.consulting.controller.v1.response.candidate.CandidateChangeLogResponse;
//import com.midas.consulting.model.Note;
//import com.midas.consulting.model.candidate.CandidateMidas;
//import com.midas.consulting.service.ActivityService;
//import com.midas.consulting.service.NoteService;
//import com.midas.consulting.service.UserService;
//import com.midas.consulting.service.candidate.IMidasCandidateService;
//import com.midas.consulting.service.hrms.ClientsService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.Authorization;
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//import org.modelmapper.ModelMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/reports")
//
//@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
//@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
//public class ReportsController {
//
//    private  final Logger log= LoggerFactory.getLogger(ReportsController.class);
//    private final IMidasCandidateService iMidasCandidateService;
//    private final AuthenticationManager authenticationManager;
//    private final UserService userService;
//    private final ClientsService clientsService;
//    private ActivityService activityService;
//    private NoteService noteService;
//    private ModelMapper modelMapper;
//
//    @Autowired
//    public ReportsController(IMidasCandidateService iMidasCandidateService, NoteService noteService, ActivityService activityService, AuthenticationManager authenticationManager, UserService userService, ClientsService clientsService) {
//        this.iMidasCandidateService = iMidasCandidateService;
//        this.authenticationManager = authenticationManager;
//        this.noteService = noteService;
//        this.userService = userService;
//        this.clientsService = clientsService;
//        this.activityService = activityService;
//        this.modelMapper = new ModelMapper();
//    }
//
//    @PostMapping("/fetchNotesBasedOnCriteria")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public ResponseEntity<List<Note>> fetchNotesBasedOnCriteria(
//            @RequestBody Map<String, List<String>> options) {
//        try {
//            List<Note> notes = noteService.fetchNotesBasedOnCriteria(options);
//            return ResponseEntity.ok(notes);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
//        }
//    }
//
//    @PostMapping("/fetchbetweendates")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public ResponseEntity<List<CandidateChangeLogResponse>> getCandidatesWithChangeLogsDates(
//            @RequestBody CandidateChangeLogRequest request) {
//        try {
//            Date fromDate = request.getFromDate() != null ?
//                    Date.from(request.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
//            Date toDate = request.getToDate() != null ?
//                    Date.from(request.getToDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()) : null;
//
//            List<CandidateChangeLogResponse> response =
//                    iMidasCandidateService.getCandidatesAndChangeLogs(request.getUserId(), fromDate, toDate);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Collections.emptyList());
//        }
//    }
//
//    @Data
//    @Getter
//    @Setter
//    public static class CandidateChangeLogRequest {
//
//        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
//        private LocalDate fromDate;
//
//        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
//        private LocalDate toDate;
//
//        List<String> userId;
//
//    }
//
//    @PostMapping("/fetch")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public ResponseEntity<List<CandidateChangeLogResponse>> getCandidatesWithChangeLogs(@RequestBody Map<String, List<String>> options) {
//        return ResponseEntity.ok(iMidasCandidateService.getCandidatesAndChangeLogs(options.get("userId")));
//
//    }
//    @PostMapping("/fetchResumeUploadsBasedOnCriteria")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public ResponseEntity<List<CandidateMidas>> fetchResumeUploadsBasedOnCriteria(
//            @RequestBody Map<String, List<String>> options,
//            @RequestParam(required = false, defaultValue = "1") int page,
//            @RequestParam(required = false, defaultValue = "10") int size) {
//        try {
//            // If page or size are missing from the request, use default values
//            page = (page > 0) ? page : 1;  // Ensure page is at least 1
//            size = (size > 0) ? size : 10;  // Ensure size is at least 1
//            return ResponseEntity.ok(null);
//        } catch (Exception e) {
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
//        }
//    }
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    @PostMapping("/fetchNotesBasedOnUserAndCriteria")
//    public ResponseEntity<List<Note>> fetchNotesBasedOnUserAndCriteria(
//            @RequestBody Map<String, Object> options) {
//        try {
//            List<Note> notes = noteService.fetchNotesBasedOnUserAndCriteria(options);
//            return ResponseEntity.ok(notes);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
//        }
//    }
//    // Later controllers
//    @PostMapping("/fetchNotesBasedOnCriteriaV2")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public ResponseEntity<List<Note>> fetchNotesBasedOnCriteriaV2(
//            @RequestBody
//            @Schema(
//                    description = "Filter options (only 'userEmail' and 'dateCreated' supported)",
//                    example = "{\"userEmail\": [\"user1@example.com\"], \"dateCreated\": [\"2025-01-01\", \"2025-02-01\"]}"
//            )
//            Map<String, List<String>> options) {
//
//        if (options == null || options.getOrDefault("userEmail", Collections.emptyList()).isEmpty()) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Collections.emptyList());
//        }
//        try {
//            List<Note> notes = noteService.fetchNotesBasedOnCriteria(options);
//            log.info("Fetched {} notes", notes.size());
//            return ResponseEntity.ok(notes);
//        } catch (IllegalArgumentException iae) {
//            log.warn("Invalid filter criteria", iae);
//            return ResponseEntity.badRequest().body(Collections.emptyList());
//        } catch (Exception e) {
//            log.error("Error fetching notes", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.emptyList());
//        }
//    }
//
//}