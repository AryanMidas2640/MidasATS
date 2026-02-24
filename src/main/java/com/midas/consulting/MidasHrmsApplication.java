package com.midas.consulting;

import com.midas.consulting.controller.v1.response.microsoft.attachment.Value;

import com.midas.consulting.service.MSPEmailServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.ConsoleHandler;


@EnableAsync
@EnableSwagger2
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class MidasHrmsApplication {
@Autowired
    public static void main(String[] args) throws Exception {
        ConsoleHandler consoleHandler = new ConsoleHandler();

//        SpringApplication.run(MultiTenancyApplication.class, args);

        SpringApplication.run(MidasHrmsApplication.class, args);

//        saveAttachments("");

//        getAuth();
//        LocalDate date = new org.joda.time.LocalDate();
//        String searchMailString = String.format("*** MSP Open Jobs - %s %dth %d ***", date.monthOfYear().getAsText(), 25, date.getYear());
//        System.out.println(searchMailString);
//        OkHttpClient client = new OkHttpClient();
//        ObjectMapper om = new ObjectMapper();
//        OneDriveAuthResponse root= null;
////        System.out.println(new OneDriveAuthService().GetAccessToken());
//        try {
//            root   = om.readValue(new OneDriveAuthService().GetAccessToken(), OneDriveAuthResponse.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(root.access_token);
    }

    private static void writeAttachment(String path, Value attachmentRoot) throws IOException {
        String nomeFile = attachmentRoot.getName();
        byte[] byteArr = Base64.getMimeDecoder().decode(attachmentRoot.getContentBytes().getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(path + nomeFile), byteArr);
    }

//    public static void saveAttachments(String accessToken) throws Exception {
////        ensureGraphClient(accessToken);
//
//        final String mailId = "message-id";
//        GraphServiceClient<Request> graphClient = new MicrosoftAuthenticationProvider().getGraphClient();
//
//        // Get the list of attachments
////        List<Attachment> attachments =graphClient
////                .me()
////                .messages(mailId)
////                .attachments()
////                .buildRequest()
////                // Use select here to avoid getting the content in this first request
////                .select("id,contentType,size")
////                .get()
////                .getCurrentPage();
////
////        for(Attachment attachment : attachments) {
////            // Attachment is a base type - need to re-get the attachment as a fileattachment
////            if (attachment.oDataType.equals("#microsoft.graph.fileAttachment")) {
////
////                // Use the client to generate the request URL
////                String requestUrl = graphClient
////                        .me()
////                        .messages(mailId)
////                        .attachments(attachment.id)
////                        .buildRequest()
////                        .getRequestUrl()
////                        .toString();
////
////                // Build a new file attachment request
////                FileAttachmentRequestBuilder requestBuilder =
////                        new FileAttachmentRequestBuilder(requestUrl, graphClient, null);
////
////                FileAttachment fileAttachment = requestBuilder.buildRequest().get();
////
////                // Get the content directly from the FileAttachment
////                byte[] content = fileAttachment.contentBytes;
////
////                try (FileOutputStream stream = new FileOutputStream("C:\\Source\\test.png")) {
////                    stream.write(content);
////                } catch (IOException exception) {
////                    // Handle it
////                }
//            }
//        }
//    }

    @Bean
    CommandLineRunner init(MSPEmailServices emailServices, BCryptPasswordEncoder bCryptPasswordEncoder    ) {
        return args -> {

//            try {
//                AttachmentRoot attachmentRoot=    emailServices.getFoldersMSPOpenJobs("*** MSP Open Jobs - February 1st 2024 ***");
//                attachmentRoot.getValue().stream().forEach(x->{
//                    try {
//                        if (x.getName().contains(".xlsx"))
//                            writeAttachment("c:\\Codebase\\"+x.getName(),x);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//
//            } catch (IOException e) {
//
//
//            }


////            ADMIN, RECRUITER,
//            Role ADMIN = roleRepository.findByRole("ADMIN");
//                if (ADMIN == null) {
//                    ADMIN = new Role();
//                    ADMIN.setRole("ADMIN");
//                    roleRepository.save(ADMIN);
//                }
//            Role RECRUITER = roleRepository.findByRole("RECRUITER");
//            if (RECRUITER == null) {
//                RECRUITER = new Role();
//                RECRUITER.setRole("RECRUITER");
//                roleRepository.save(RECRUITER);
//            }
//            Role SUPERADMIN = roleRepository.findByRole("SUPERADMIN");
//            if (SUPERADMIN == null) {
//                SUPERADMIN = new Role();
//                SUPERADMIN.setRole("SUPERADMIN");
//                roleRepository.save(SUPERADMIN);
//            }
//
//            Role ONBOARD = roleRepository.findByRole("ONBOARD");
//            if (ONBOARD == null) {
//                ONBOARD = new Role();
//                ONBOARD.setRole("ONBOARD");
//                roleRepository.save(ONBOARD);
//            }
//            Role TEAMLEAD = roleRepository.findByRole("TEAMLEAD");
//            if (TEAMLEAD == null) {
//                TEAMLEAD = new Role();
//                TEAMLEAD.setRole("TEAMLEAD");
//                roleRepository.save(TEAMLEAD);
//            }
//            Role MODERATOR = roleRepository.findByRole("MODERATOR");
//            if (MODERATOR == null) {
//                MODERATOR = new Role();
//                MODERATOR.setRole("MODERATOR");
//                roleRepository.save(MODERATOR);
//            }
//            Role ACCOUNTMANAGER = roleRepository.findByRole("ACCOUNTMANAGER");
//            if (ACCOUNTMANAGER == null) {
//                ACCOUNTMANAGER = new Role();
//                ACCOUNTMANAGER.setRole("ACCOUNTMANAGER");
//                roleRepository.save(ACCOUNTMANAGER);
//            }
//            Role GENERALMANAGER = roleRepository.findByRole("GENERALMANAGER");
//            if (GENERALMANAGER == null) {
//                GENERALMANAGER = new Role();
//                GENERALMANAGER.setRole("GENERALMANAGER");
//                roleRepository.save(GENERALMANAGER);
//            }
//            User dheeraj= userRepository.findByEmail("dheeraj.singh@midasconsulting.org");
//                if (dheeraj==null) {
//                dheeraj = new User()
//                        .setUserType(UserType.INTERNAL)
//                        .setActive(true)
//                        .setEmail("dheeraj.singh@midasconsulting.org")
//                        .setPassword("123456")
//                        .setFirstName("Dheeraj")
//                        .setLastName("Singh")
//                        .setDateCreated(new Date())
//                        .setDateModified(new Date())
//                        .setManager(null)
//                        .setMobileNumber("+919718910927")
//                        .setProfilePicture("")
//                        .setRoles(Collections.singleton(ADMIN))
//                ;
//
//
//                    userRepository.save(dheeraj);
//                }

//
//            columns = {String[10]@11714} ["id", "create_date", "modify_date", "name", "password", +5 more]
//            0 = "id"
//            1 = "create_date"
//            2 = "modify_date"
//            3 = "name"
//            4 = "password"
//            5 = "roll_id"
//            6 = "status"
//            7 = "type"
//            8 = "username"
//            9 = "manager_id"


//            0 = "1"
//            1 = ""
//            2 = ""
//            3 = "Archit"
//            4 = "MidasAdmin@3321"
//            5 = "1"
//            6 = "1"
//            7 = "Internal"
//            8 = "archit.mishra@midastravel.org"


//            "C:\Users\dell\Documents\Book1.csv"
//            Map<Integer, String> employees = new HashMap<>();
//        if (true){
//
//            List<String> lines=    Files.readAllLines(Paths.get("C:\\Users\\dell\\Documents\\Book1.csv"));
//            for (String line : lines) {
//                String[] cols = line.split(",");
//                employees.put(Integer.valueOf(cols[0]), cols[1]);
//            }
//        }

//            if (false) {
//                Set<Role> roles = null;
//                List<String> lines = Files.readAllLines(Paths.get("C:\\Users\\dell\\Downloads\\AllEmployees.csv"));
//                for (String line : lines) {
//                    roles = new HashSet<>();
//                    String columns[] = line.split(",");
//                    for (int i = 0; i < columns.length; i++) {
//                        User user = userRepository.findByEmail(columns[8]);
//                        if (user == null) {
//                            user = new User();
//                            String[] nameParts = columns[3].split(" ");
//                            if (nameParts.length > 1) {
//                                user.setFirstName(nameParts[0]);
//                                user.setLastName(nameParts[1]);
//                            } else {
//                                user.setFirstName(columns[3]);
//                            }
//                            user.setEmail(columns[8]);
//                            user.setDateCreated(new Date());
//                            user.setDateModified(new Date());
//                            user.setMobileNumber("+912121212121");
//                            user.setActive(true);
//                            user.setProfilePicture("default");
//                            user.setPassword(bCryptPasswordEncoder.encode(columns[4]));
//                            user.setUserType(columns[7].equalsIgnoreCase("external") ? UserType.EXTERNAL : UserType.INTERNAL);
//                           if (columns.length==10){
//                               user.setManager(columns[9].equals("") ? null : userRepository.findByEmail(employees.get(Integer.valueOf(columns[9]))));
//                           }
//                           else {
//                               user.setManager(null);
//                           }
//
//
////                            allRoles.put(1, "SuperAdmin");
////            allRoles.put(2, "Admin");
////            allRoles.put(3, "Moderator");
////            allRoles.put(4, "Onboarding");
////            allRoles.put(5, "Recruiter");
////            allRoles.put(6, "TeamLead");
////            allRoles.put(7, "AccountManager");
////            allRoles.put(7, "GeneralManager");
//                            switch (columns[5]) {
//                                case "1": {
//                                    Role role = roleRepository.findByRole("SUPERADMIN");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "2": {
//                                    Role role = roleRepository.findByRole("ADMIN");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "3": {
//                                    Role role = roleRepository.findByRole("MODERATOR");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "4": {
//                                    Role role = roleRepository.findByRole("ONBOARD");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "5": {
//                                    Role role = roleRepository.findByRole("RECRUITER");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "6": {
//                                    Role role = roleRepository.findByRole("TEAMLEAD");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "7": {
//                                    Role role = roleRepository.findByRole("ACCOUNTMANAGER");
//                                    roles.add(role);
//                                    break;
//                                }
//                                case "8": {
//                                    Role role = roleRepository.findByRole("GENERALMANAGER");
//                                    roles.add(role);
//                                    break;
//                                }
//                            }
//
//                            user.setRoles(roles);
//                            System.out.println("User Exported " + userRepository.save(user));
//                        }
//                    }
//                }
//            }

            //

//            if(true){
//
//                List<Technology> technologyStack = new ArrayList<>();
//                List<User> user = new ArrayList<>();
//                List<Session> sessions = new ArrayList<Session>();
//
//                List<Bootcamp> bootcamps =  bootcampRepository.findByName("Full Stack Java");
//                if(bootcamps.size() < 1) {
//                    // we need to make one as it is zero
//                    Bootcamp bootcamp = new Bootcamp();
//                    bootcamp.setDescription("seome desc")
//                            .setEndDate(new Date())
//                            .setName("Full Stack Java")
//                            .setLongHtml("some long html")
//                            .setStartDate(new Date())
//                            .setTechnologyStack(technologyStack)
//                            .setUsers(user)
//                            .setSessions(sessions);
//                    bootcampRepository.save(bootcamp);
//                    System.out.println("Bootcamp Create Successfully !!!!");
//                }
//
//
//            }
//
//
//            if(false){
//
//                Role participantRole = roleRepository.findByRole("PARTICIPANT");
//                if (participantRole == null) {
//                    participantRole = new Role();
//                    participantRole.setRole("PARTICIPANT");
//                    roleRepository.save(participantRole);
//                }
//                HashSet<Role> roles = new HashSet<>();
//                roles.add(participantRole);
//                   User user = new User();
//                   HashSet<UserProfile> userProfiles =new HashSet<>();
//
//                   UserProfile userProfile = new UserProfile();
//                   userProfile.setName("dheeraj.singh@snva.com");
//
//
//                HashSet<Experience> experiences = new HashSet<>();
//
//                experiences.add(new Experience()
//                        .setName("Software Developer Intern")
//                        .setDescription("As a a developer i used to wbdkfkasjfkasfgsdjfklg")
//                        .setYears(1.5f));
//
//                HashSet<Education> educations = new HashSet<>();
//                educations.add(
//                        new Education()
//                                .setName("HighSchool")
//                                .setCity("Some city")
//                                .setCountry("USA")
//                                .setUniversity("Some University")
//                                .setState("Some state")
//                );
//
//                HashSet<Skill> skills = new HashSet<>();
//                skills.add(
//                        new Skill()
//                                .setSkillName("Java")
//                                .setProficieny(5)
//                                .setCertificationLink("some cert links")
//
//
//                );
//                HashSet<Project> projects= new HashSet<>();
//                projects.add(
//                        new Project()
//                                .setName("The Apirio")
//                                .setRoles("I was just a beginner i used to manage the docs")
//                                .setTeamSize(5)
//                                .setDetails("some lengthy details")
//                                .setResponsibilities("Some responsibilities")
//                                .setTechStack("MERN Stack")
//
//                );
//
//                userProfile.setSkills(skills);
//                userProfile.setEducation(educations);
//                userProfile.setProjects(projects);
//                userProfile.setExperience(experiences);
//
//
//                userProfile= profileRepository.save(userProfile);
//
//                System.out.println("The Profile was created !!!!!!!!!!!!!");
//
//                   user.setFirstName("Dheeraj");
//                    user.setLastName("Singh");
//                    user.setEmail("dheeraj.singh@snva.com");
//                    user.setRoles(roles);
//                    user.setMobileNumber("+1 111 11111");
//                    user.setAddress("SOme Address USA ");
//                    user.setPassword(new BCryptPasswordEncoder().encode("123456")) ;// "123456";
//                    user.setProfilePicture("");
//                    userProfiles.add(userProfile);
//                    user.setUserProfiles(userProfiles);
//
//                System.out.println("The User was created !!!!!!!!!!!!!"+ userRepository.save(user));
//            }
//
//            if (false){
//
//
//
//
//
//
//
//
//
//
//
//                Role participantRole = roleRepository.findByRole("PARTICIPANT");
//                if (participantRole == null) {
//                    participantRole = new Role();
//                    participantRole.setRole("PARTICIPANT");
//                    roleRepository.save(participantRole);
//                }
//                User admin = userRepository.findByEmail("dheeraj1@gmail.com");
//                if (admin == null) {
//
//                    Set<UserProfile> userProfiles = new HashSet<>();
//                    UserProfile userProfile = new UserProfile("dheeraj1@gmail.com");
//
//
//                    Experience experience = new Experience();
//                    experience.setYears(2);
//                    experience.setName("Developer Experience");
//
//                    Project project = new Project();
//                    project.setResponsibilities("team lead developing prodctus");
//                    project.setTechStack("SPRING BOOT REACT");
//                    project.setRoles("Lead");
//                    project.setResponsibilities("fjsfdlkjgfsldkjglksjfdg");
//                    project.setDetails("some Details");
//
//
//                    Education education = new Education();
//                    education.setCity("education city");
//                    education.setCountry("education country");
//                    education.setName("Greaducation");
//                    education.setUniversity("Some University");
//
//
//                Set<Education> educations = new HashSet<>();
//                Set<Experience> experiences = new HashSet<>();
//                Set<Project> projects = new HashSet<>();
//
//                educations.add(education);
//                experiences.add(experience);
//                projects.add(project);
//
//                    Optional<UserProfile> userProfileOptional= profileRepository.findByName("dheeraj1@gmail.com"+"p1");
//                    if (!userProfileOptional.isPresent()){
//                         userProfile = new UserProfile("dheeraj1@gmail.com");
//                        userProfile.setStackOverflow("Stack");
//                        userProfile.setExperience(experiences);
//                        userProfile.setGithub("github");
//                        userProfile.setProjects(projects);
//                        userProfile.setLinkedIn("Linked in ");
//                        userProfile.setName("dheeraj1@gmail.com"+"p1");
//                        userProfile.setEducation(educations);
//                        userProfile = profileRepository.save(userProfile);
//                    }
//                    userProfiles.add(userProfile);
//                    admin = new User()
//                            .setEmail("dheeraj1@gmail.com")
//                            .setPassword(new BCryptPasswordEncoder().encode("123456")) // "123456"
//                            .setFirstName("Dheeraj")
//                            .setLastName("Singh")
//                            .setMobileNumber("123456789")
//                            .setProfilePicture("https://avatars.githubusercontent.com/u/32265439?v=4")
//                            .setUserProfiles(userProfiles)
//                            .setRoles(new HashSet<>(Arrays.asList(participantRole)));
//                    userRepository.save(admin);
//                }
//
//
//
//
//            }else {
//
//
//                //Create Admin and Passenger Roles
//                Role adminRole = roleRepository.findByRole("ADMIN");
//                if (adminRole == null) {
//                    adminRole = new Role();
//                    adminRole.setRole("ADMIN");
//                    roleRepository.save(adminRole);
//                }
//
//
//
//                Role userRole = roleRepository.findByRole("PASSENGER");
//                if (userRole == null) {
//                    userRole = new Role();
//                    userRole.setRole("PASSENGER");
//                    roleRepository.save(userRole);
//                }
//
//                //Create an Admin user
//                User admin = userRepository.findByEmail("admin.agencya@gmail.com");
//                if (admin == null) {
//                    admin = new User()
//                            .setEmail("admin.agencya@gmail.com")
//                            .setPassword("$2a$10$7PtcjEnWb/ZkgyXyxY1/Iei2dGgGQUbqIIll/dt.qJ8l8nQBWMbYO") // "123456"
//                            .setFirstName("John")
//                            .setLastName("Doe")
//                            .setMobileNumber("9425094250")
//                            .setRoles(new HashSet<>(Arrays.asList(adminRole)));
//                    userRepository.save(admin);
//                }
//
//                //Create four stops
//                Stop stopA = stopRepository.findByCode("STPA");
//                if (stopA == null) {
//                    stopA = new Stop()
//                            .setName("Stop A")
//                            .setDetail("Near hills")
//                            .setCode("STPA");
//                    stopRepository.save(stopA);
//                }
//
//                Stop stopB = stopRepository.findByCode("STPB");
//                if (stopB == null) {
//                    stopB = new Stop()
//                            .setName("Stop B")
//                            .setDetail("Near river")
//                            .setCode("STPB");
//                    stopRepository.save(stopB);
//                }
//
//                Stop stopC = stopRepository.findByCode("STPC");
//                if (stopC == null) {
//                    stopC = new Stop()
//                            .setName("Stop C")
//                            .setDetail("Near desert")
//                            .setCode("STPC");
//                    stopRepository.save(stopC);
//                }
//
//                Stop stopD = stopRepository.findByCode("STPD");
//                if (stopD == null) {
//                    stopD = new Stop()
//                            .setName("Stop D")
//                            .setDetail("Near lake")
//                            .setCode("STPD");
//                    stopRepository.save(stopD);
//                }
//
//                //Create an Agency
//                Agency agencyA = agencyRepository.findByCode("AGENCYA");
//                if (agencyA == null) {
//                    agencyA = new Agency()
//                            .setName("Green Mile Agency")
//                            .setCode("AGENCYA")
//                            .setDetails("Reaching desitnations with ease")
//                            .setOwner(admin);
//                    agencyRepository.save(agencyA);
//                }
//
//                //Create a bus
//                Bus busA = busRepository.findByCode("AGENCYA-1");
//                if (busA == null) {
//                    busA = new Bus()
//                            .setCode("AGENCYA-1")
//                            .setAgency(agencyA)
//                            .setCapacity(60);
//                    busRepository.save(busA);
//                }
//
//                //Add busA to set of buses owned by Agency 'AGENCYA'
//                if (agencyA.getBuses() == null) {
//                    Set<Bus> buses = new HashSet<>();
//                    agencyA.setBuses(buses);
//                    agencyA.getBuses().add(busA);
//                    agencyRepository.save(agencyA);
//                }
//
//                //Create a Trip
//                Trip trip = tripRepository.findBySourceStopAndDestStopAndBus(stopA, stopB, busA);
//                if (trip == null) {
//                    trip = new Trip()
//                            .setSourceStop(stopA)
//                            .setDestStop(stopB)
//                            .setBus(busA)
//                            .setAgency(agencyA)
//                            .setFare(100)
//                            .setJourneyTime(60);
//                    tripRepository.save(trip);
//                }
//
//                //Create a trip schedule
//                TripSchedule tripSchedule = tripScheduleRepository.findByTripDetailAndTripDate(trip, DateUtils.todayStr());
//                if (tripSchedule == null) {
//                    tripSchedule = new TripSchedule()
//                            .setTripDetail(trip)
//                            .setTripDate(DateUtils.todayStr())
//                            .setAvailableSeats(trip.getBus().getCapacity());
//                    tripScheduleRepository.save(tripSchedule);
//                }
//            }
        };
    }
}