package com.midas.consulting.controller.v1.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.midas.consulting.controller.v1.request.UserSignupRequest;
import com.midas.consulting.controller.v1.response.LoginResponse;
import com.midas.consulting.controller.v1.response.LoginResponseHwl;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.model.user.LoginStats;
import com.midas.consulting.model.user.User;
import com.midas.consulting.security.CustomUserDetailsService;
import com.midas.consulting.security.api.JwtUtils;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.LoginStatsService;
import io.jsonwebtoken.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

import static com.midas.consulting.security.SecurityConstants.EXPIRATION_TIME;
import static com.midas.consulting.security.SecurityConstants.SECRET;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/user")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class JwtAuthenticationController {

//    @Autowired
    private AuthenticationManager authenticationManager;


//    @Autowired
    private  org.modelmapper.ModelMapper modelMapper;

//    @Autowired
    private CustomUserDetailsService userDetailsService;


    private final JwtUtils jwtUtils;
    private UserService userService;
private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationController.class);
    private LoginStatsService loginStatsService;
    @Autowired
    public JwtAuthenticationController(LoginStatsService loginStatsService, AuthenticationManager authenticationManager, ModelMapper modelMapper, CustomUserDetailsService userDetailsService, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
        this.loginStatsService = loginStatsService;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;

    }

//
//    /mybootcamps
//    /myprogress
//    /leaderboard
//    /onlinePractice--> Code --> Compiled bt Judge API

    /**
     * Handles the incoming POST API "/v1/user/signup"
     *
     * @param userSignupRequest
     * @return
     */
    @PostMapping("/signup")
    public Response signup(@RequestBody @Valid UserSignupRequest userSignupRequest) {
        return Response.ok().setPayload(registerUser(userSignupRequest, false));
    }
    @PostMapping("/register")
    public Response register(@RequestBody @Valid UserSignupRequest userSignupRequest) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(userService.signup(userSignupRequest));
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return response;
        }
        return response;

    }
    /**
     * Register a new user in the database
     *
     * @param userSignupRequest
     * @return
     */
    private UserDto registerUser(UserSignupRequest userSignupRequest, boolean isAdmin) {
        UserDto userDto = new UserDto()
                .setEmail(userSignupRequest.getEmail())
                .setPassword(userSignupRequest.getPassword())
                .setFirstName(userSignupRequest.getFirstName())
                .setLastName(userSignupRequest.getLastName())
                .setMobileNumber(userSignupRequest.getMobileNumber())
                .setActive(false)
//                .setRoles()
                .setProfilePicture("https://i.pinimg.com/736x/65/49/ca/6549cacdca6c392649a70153981bd27d.jpg");

        return userService.signup(userDto);
    }

    @PatchMapping(value = "updateprofile", produces = MediaType.APPLICATION_JSON_VALUE)
    public  Response updateProfile(@RequestBody UserDto userDto){
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK);
            response.setPayload(userService.updateProfile(userDto));
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
           return  response;
        }
        return  response;
    }

    @PostMapping("/apiprofile")
    @ResponseBody
    @ApiOperation(value = "",  authorizations = {@Authorization(value = "apikey")})
    public  ResponseEntity<?> getProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> list= authentication.getAuthorities();
        String user=(String)authentication.getPrincipal();
        UserDto userDto= new UserDto();
        userDto=  userService.findUserByEmail(user);
        return  ResponseEntity.ok(userDto);
    }


    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updatePassword(@RequestBody PasswordResetRequest passwordResetRequest, Principal principal) throws Exception {
        Response response = new Response();

        response.setStatus(Response.Status.OK).setPayload(userService.updatePassword(passwordResetRequest));
        return response;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            // Invalid signature/claims
        } catch (ExpiredJwtException ex) {
            // Expired token
        } catch (UnsupportedJwtException ex) {
            // Unsupported JWT token
        } catch (MalformedJwtException ex) {
            // Malformed JWT token
        } catch (IllegalArgumentException ex) {
            // JWT token is empty
        }
        return false;
    }

    @RequestMapping(value = "/verifyToken/{token}", method = RequestMethod.GET)
    @ResponseBody
    public Boolean verifyToken(@PathVariable String token) {

        return jwtUtils.validateJwtToken(token);
//        return validateToken(token);
    }



    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthToken(@RequestBody @Valid LoginRequest loginRequest) {

        LoginResponse loginResponse = new LoginResponse();

        try {

            authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            UserDto userDetail = userService.findUserByEmail(loginRequest.getEmail());

            if (userDetail == null) {
                throw new RuntimeException("User not found");
            }

            if (!userDetail.isActive()) {
                loginResponse.setResponse("User is not active");
                loginResponse.setStatus("error");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loginResponse);
            }

            String token = jwtUtils.genrateJWTToken(UserMapper.toUser(userDetail));

            loginResponse.setResponse(token);
            loginResponse.setUser(userDetail);
            loginResponse.setStatus("success");

            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            loginResponse.setResponse(e.getMessage());
            loginResponse.setStatus("error");
            return ResponseEntity.badRequest().body(loginResponse);
        }
    }

    @RequestMapping(value ="/authenticateHwl", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createAuthTokenHwl(@RequestBody @Valid LoginRequest loginRequest)  {
        LoginResponseHwl loginResponse = new LoginResponseHwl();
        UUID uuid = UUID.randomUUID();
        try{
            authenticate(loginRequest.getEmail(),loginRequest.getPassword());
        }catch (Exception ee){
            loginResponse.setAccess_token("");
            loginResponse.setToken_type("");
            loginResponse.setSignature("");
            loginResponse.setInstance_url("");
            loginResponse.setIssued_at(LocalDate.now());
            loginResponse.setId(uuid.toString());
            return  ResponseEntity.accepted().body(loginResponse);
        }

        final UserDetails user = userDetailsService.loadUserByUsername(loginRequest.email);
        UserDto userDetail=
                userService.findUserByEmail(loginRequest.email);
        // generate the token
        // return that to the cleint
        if (!userDetail.isActive()){
            loginResponse.setAccess_token("");
            loginResponse.setToken_type("");
            loginResponse.setSignature("");
            loginResponse.setInstance_url("");
            loginResponse.setIssued_at(LocalDate.now());
            loginResponse.setId(uuid.toString());
            return  ResponseEntity.accepted().body(loginResponse);
        }


        String token= "";
        String login = user.getUsername();
        if (login != null && login.length() > 0) {
            Claims claims = Jwts.claims().setSubject(login);
            List<String> roles = new ArrayList<>();
            user.getAuthorities().stream().forEach(authority -> roles.add(authority.getAuthority()));
            claims.put("roles", roles);
            token  = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(SignatureAlgorithm.HS512, SECRET)
                    .compact();
            ;
            try {
                loginStatsService.saveLoginStats(userDetail.getId(),"0.0.0.0/all");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        loginResponse.setAccess_token(token);
        loginResponse.setToken_type("Bearer");
        loginResponse.setSignature(generateFakeSignature(SIGNATURE_LENGTH));
        loginResponse.setInstance_url("https://uatapi.midastech.org/api/requisitions");
        loginResponse.setIssued_at(LocalDate.now());
        loginResponse.setId(userDetail.getId().toString());
        return  ResponseEntity.accepted().body(loginResponse);
    }

    public static String generateFakeSignature(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder signature = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            signature.append(CHARACTERS.charAt(index));
        }

        return signature.toString();
    }
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SIGNATURE_LENGTH = 10; // Length of the signature


    private void authenticate(String email, String password) throws Exception {
        logger.info("Inside authenticate method for email: {}", email);
        try {
            logger.info("Calling authenticationManager.authenticate");
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            logger.info("Authentication successful: {}", auth);
        } catch (DisabledException dE) {
            logger.error("User disabled: ", dE);
            throw new Exception("User Disabled", dE);
        } catch (BadCredentialsException badCredentialsException) {
            logger.error("Bad credentials: ", badCredentialsException);
            throw new Exception("Invalid credentials provided for user " + email, badCredentialsException);
        } catch (Exception e) {
            logger.error("Unexpected authentication error: ", e);
            throw e;
        }
    }

    @RequestMapping(value ="/allRoles", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> allRoles()  {
        return  ResponseEntity.accepted().body(userService.allRoles());
    }

    @RequestMapping(value ="/allUsers", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> allUsers()  {
        return  ResponseEntity.accepted().body(userService.allUsers());
    }



    // Old Apis similar to what Apaar created
    @GetMapping("/getUserById/{id}")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getUserById(@PathVariable String id, Principal principal) {
        Response response = new Response();
        UserDto userDto = userService.findUserById(id);
//        User user = userService.findUserObjectById(id);
        response.setStatus(Response.Status.OK).setPayload(userDto);
        return response;
    }


    // Old Apis similar to what Apaar created
    @GetMapping("/getManagersByUser/{id}")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getManagersByUser(@PathVariable String id, Principal principal) {
        Response response = new Response();
        List<UserDto> userDto = userService.findManagersByUser(id);
//        User user = userService.findUserObjectById(id);
        response.setStatus(Response.Status.OK).setPayload(userDto);
        return response;
    }



    @GetMapping("/getUserObjById/{id}")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getUserObjById(@PathVariable String id, Principal principal) {
        Response response = new Response();

        User user = userService.findUserObjectById(id);
        response.setStatus(Response.Status.OK).setPayload(user);
        return response;
    }


    @GetMapping("/getAllLoginStats/all")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllLoginStats(Principal principal) {
        Response response = new Response();
        List<LoginStats> allLoginStats = loginStatsService.getAllLoginStats();
        response.setStatus(Response.Status.OK).setPayload(allLoginStats);
        return response;
    }
    @GetMapping("/getUserByEmail/{email}")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getUserByEmail(Principal principal, @PathVariable String email) {
        Response response = new Response();
        UserDto userData = userService.getUserByEmail(email);
        response.setStatus(Response.Status.OK).setPayload(userData);
        return response;
    }


    @GetMapping("/getAllLoginStats")
    public Response getAllLoginStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "loginTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Principal principal
    ) {
        Response response = new Response();
        Map<String, Object> payload = loginStatsService.getAllLoginStats(page, size, sortField, sortDirection);
        response.setStatus(Response.Status.OK).setPayload(payload);
        return response;
    }

    @GetMapping("/getLoginStatsBwDates")
    public Response getLoginStatsBwDates(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "loginTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Principal principal
    ) {
        Response response = new Response();
        Map<String, Object> payload = loginStatsService.getLoginStatsBwDates(
                startDate, endDate, page, size, sortField, sortDirection
        );
        response.setStatus(Response.Status.OK).setPayload(payload);
        return response;
    }

//    @GetMapping("/getLoginStatsBwDates")
////    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response getAllLoginStats(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, Principal principal) {
//        Response response = new Response();
//        Date dateStart = new Date();
//        Date dateEnd = new Date();
//        String pattern = "yyyy-MM-dd"; // Adjust the pattern according to your date format
//        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
//
//        try {
//            dateStart = dateFormat.parse(startDate);
//            dateEnd = dateFormat.parse(endDate);
//            System.out.println("Parsed Date: " + dateStart);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        List<LoginStats> allLoginStats = loginStatsService.getLoginStatsDate(dateStart,dateEnd);
//        response.setStatus(Response.Status.OK).setPayload(allLoginStats);
//        return response;
//    }

//    @RequestMapping(value = "/get-all-user", method = RequestMethod.GET)
//    public ResponseEntity<?> getUsers(){
//        List<User> users = userService.getAllUsers();
//        return ResponseEntity.ok(users);
//    }

//    @RequestMapping(value = "/get-all-team-leads", method = RequestMethod.GET)
//    public ResponseEntity<?> getAllTeamLead(){
//        List<User> users = userService.getAllTeamLeads(6);
//        return ResponseEntity.ok(users);
//    }
//    @GetMapping("/getAllTeamLeads")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response getAllTeamLeads( Principal principal) {
//        Response response = new Response();
//        List<User> userDto = userService.allTeamLeads();
//        response.setStatus(Response.Status.OK).setPayload(userDto);
//        return response;
//    }
    @RequestMapping(value = "/getUserByRole", method = RequestMethod.GET)
    public ResponseEntity<?> getUserByRoll(@RequestParam String roleId){
        List<User> users = userService.getUserByRoll(roleId);
        return ResponseEntity.ok(users);
    }

//    get-all-user--> /getAllUsers
    @GetMapping("/getAllUsers")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllUsers( Principal principal) {
        Response response = new Response();
        List<User> userDto = userService.allUsers();
        response.setStatus(Response.Status.OK).setPayload(userDto);
        return response;
    }


    @GetMapping("/getUsersByManager/{managerId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getUsersByManager( @PathVariable String managerId, Principal principal) {
        Response response = new Response();
        List<User> userDto = userService.getUsersByManager(managerId);
        response.setStatus(Response.Status.OK).setPayload(userDto);
        return response;
    }
    // End old api similar to what Apaar created

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequest {
        @NotNull(message = "{constraints.NotEmpty.message}")
        private String email;
        @NotNull(message = "{constraints.NotEmpty.message}")
        private String password;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PasswordResetRequest {
        @NotNull(message = "{constraints.NotEmpty.message}")
        private String userId;
        @NotNull(message = "{constraints.NotEmpty.message}")
        private String newPassword;

        private String passwordHash;
    }

}
