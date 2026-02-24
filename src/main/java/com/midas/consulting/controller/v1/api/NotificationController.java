package com.midas.consulting.controller.v1.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.midas.consulting.controller.v1.request.jobs.JobsFeeds;
import com.midas.consulting.controller.v1.response.hwl.allmails.AllMailsRoot;
import com.midas.consulting.controller.v1.response.microsoft.attachment.AttachmentRoot;
import com.midas.consulting.controller.v1.response.vectoremails.VectorEmail;
import com.midas.consulting.service.crawlers.VectorEmailNotificationCrawlerService;
import com.midas.consulting.service.hrms.onedrive.OneDriveServices;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @Autowired
    private VectorEmailNotificationCrawlerService crawlerService;

    //    String baseDir = "C:\\Codebase\\server-vector-scrapper";
    @Value("${scrapper.path}")
    String baseDir;

    @Autowired
    private OneDriveServices oneDriveServices;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Gson gson = new Gson();

    private final AtomicReference<String> latestOtp = new AtomicReference<>();


    @GetMapping("/webhook/health-trust/get-otp")
    @ResponseBody
    public ResponseEntity<String> streamOtp() {
//        String otp = latestOtp.get();
        String otp = latestOtp.getAndSet(null); // Atomically retrieve and clear OTP
        if (otp != null) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(otp);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No OTP available");
        }
    }



    @PostMapping(value = "/webhook/health-trust/otp", consumes = {"application/json", "text/plain"}, produces = {"application/json", "text/plain"}) @ResponseBody
    public ResponseEntity<String> handleNotificationHealthTrustOTP(HttpServletRequest httpServletRequest,
                                                                   @RequestParam(name = "validationToken", required = false) String validationToken,
                                                                   @RequestBody(required = false) String requestBody) throws IOException {
        if (httpServletRequest.getParameter("validationToken") != null) {
            System.out.println("Returning.........." + validationToken);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(validationToken);
        }
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        JsonArray valueArray = jsonObject.getAsJsonArray("value");
        JsonElement element = valueArray.get(0);
        JsonObject notificationJson = element.getAsJsonObject();
        JsonObject resourceData = notificationJson.getAsJsonObject("resourceData");
        String messageId = resourceData.get("id").getAsString();
        String accessToken = oneDriveServices.GetAccessToken();
        JSONParser parser = new JSONParser();
        if (accessToken != null) {
            VectorEmail emailData = new VectorEmail();
            try {
                JSONObject json = (JSONObject) parser.parse(accessToken);
                String Token = (String) json.get("access_token");
                emailData = fetchEmail(Token, messageId);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (emailData != null) {
                String subject = emailData.getSubject();
                if (subject.toLowerCase().contains("MFA request".toLowerCase())) {
                    System.out.println("The HTML Email");
                    String emailBody=  emailData.getBody().getContent();
                    emailBody = Jsoup.parse(emailData.getBody().getContent()).text();

                    // Example: extract OTP using regex (adjust as per actual OTP format)
                    Matcher matcher = Pattern.compile("\\b\\d{6}\\b").matcher(emailBody);
                    if (matcher.find()) {
                        String otp = matcher.group();
                        latestOtp.set(otp); // Store the OTP
                        System.out.println("Extracted OTP: " + otp);
                    }
                    System.out.println("New Jobs Staffing Engine Received"+emailBody);
                }
            } else {
                System.out.println("No Data was there  is exceeded ");
            }
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("");
    }




    @PostMapping(value = "/webhook/staffingEngine", consumes = {"application/json", "text/plain"}, produces = {"application/json", "text/plain"}) @ResponseBody
    public ResponseEntity<String> handleNotificationStaffingEngine(HttpServletRequest httpServletRequest,
                                                                   @RequestParam(name = "validationToken", required = false) String validationToken,
                                                                   @RequestBody(required = false) String requestBody) throws IOException {
        if (httpServletRequest.getParameter("validationToken") != null) {
            System.out.println("Returning.........." + validationToken);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(validationToken);
        }
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        JsonArray valueArray = jsonObject.getAsJsonArray("value");
        JsonElement element = valueArray.get(0);
        JsonObject notificationJson = element.getAsJsonObject();
        JsonObject resourceData = notificationJson.getAsJsonObject("resourceData");
        String messageId = resourceData.get("id").getAsString();
        String accessToken = oneDriveServices.GetAccessToken();
        JSONParser parser = new JSONParser();
        if (accessToken != null) {
            VectorEmail emailData = new VectorEmail();
            try {
                JSONObject json = (JSONObject) parser.parse(accessToken);
                String Token = (String) json.get("access_token");
                emailData = fetchEmail(Token, messageId);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (emailData != null) {
                String subject = emailData.getSubject();
                if (subject.toLowerCase().contains("New StaffingEngine Job Posting".toLowerCase())) {
                    System.out.println("New StaffingEngine Job Posting");
                    Document doc = Jsoup.parse(emailData.getBody().getContent());
                    System.out.println("The HTML Email");
                    Map<String, String> newJob = getNewJobsParsed(emailData.getBody().getContent());
                    JobsFeeds jobsFeeds = getStaffingEngineJobsFeedsParsed(newJob);
                    updateJob(jobsFeeds);
                    System.out.println("New Jobs Staffing Engine Received"+ new Gson().toJson(jobsFeeds));
                }
                if (subject.toLowerCase().contains("Has Updated the Job Status,".toLowerCase())) {
                    String status="";
                    String jobIdPattern = "Job\\s(\\d+)";  // Extracts Job ID
                    String file = emailData.getBody().getContent();
                    Pattern idPattern = Pattern.compile(jobIdPattern);
                    Matcher idMatcher = idPattern.matcher(file);
                    String jobId = idMatcher.find() ? idMatcher.group(1) : "Not Found";
                    if (file.toLowerCase().contains(" On-Hold to Open.".toLowerCase()))
                        status="Open";
                    else if (file.toLowerCase().contains(" Open to On-Hold.".toLowerCase()))
                        status="OnHold";
                    else if(file.toLowerCase().contains(" Open to Open.".toLowerCase()))
                        status="Open";
                    else if(file.toLowerCase().contains(" Open to Closed.".toLowerCase()))
                        status="Closed";
                    else if(file.toLowerCase().contains(" Closed to Open.".toLowerCase()))
                        status="Open";
                    System.out.println("Job ID: " + jobId);
                    System.out.println("Job Status Updates: " + status);
                    OkHttpClient client = new OkHttpClient();
                    String urlProcess= "https://tenantapi.theartemis.ai/api/jobsmanual/getUpdateBySrcId/" + decodeUrl(jobId) + "/" + status +"/StaffingEngine";
                    System.out.println("url to be processed "+ urlProcess);
                    Request request = new Request.Builder()
                            .url(urlProcess)
                            .get()
                            .addHeader("User-Agent", "insomnia/9.2.0")
                            .addHeader("X-Tenant", "670a48b168b0640a262870c4")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        System.out.println("HWL Email Block Update Status : " + response.body().string());
                    } catch (IOException e) {
                        System.out.println("Exception in process URL API for update Job");
                        throw new RuntimeException(e);
                    }
                }
            } else {
                System.out.println("No Data was there  is exceeded ");
            }
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("");
    }






// ============================================================================
// SHIFT NORMALIZATION UTILITY METHODS
// Add these methods to your NotificationController class
// ============================================================================

    /**
     * Normalizes shift strings to standard shift types
     * Handles various formats like "8 HR Mids", "12 HR Days", "Evening", etc.
     *
     * @param rawShift The raw shift string from email parsing
     * @return Normalized shift type (Day, Night, Evening, Evening-Night, Rotating, Flexible, Unknown)
     */
    private String normalizeShift(String rawShift) {
        if (rawShift == null || rawShift.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = rawShift.toLowerCase().trim();

        // Remove common patterns to focus on shift type
        normalized = normalized
                .replaceAll("\\d+\\s*hr\\s*", "")           // Remove "8 HR", "12 HR"
                .replaceAll("\\d+\\s*hours?\\s*", "")       // Remove "8 hours", "12 hour"
                .replaceAll("\\s+", " ")                     // Normalize whitespace
                .trim();

        // Day shift patterns
        if (normalized.matches(".*(day|days|day\\s*shift).*") &&
                !normalized.contains("night") &&
                !normalized.contains("evening")) {
            return "Day";
        }

        // Night shift patterns (including "noc" which is common in healthcare)
        if (normalized.matches(".*(night|nights|night\\s*shift|noc|nocs|overnight).*")) {
            return "Night";
        }

        // Evening/Mid shift patterns
        if (normalized.matches(".*(mid|mids|evening|eve|eves|swing).*")) {
            // Check if it's evening-night combo
            if (normalized.contains("night") || normalized.contains("noc")) {
                return "Evening-Night";
            }
            return "Evening";
        }

        // PM shift (likely evening)
        if (normalized.matches(".*(pm).*") && !normalized.contains("am")) {
            return "Evening";
        }

        // Rotating shifts
        if (normalized.matches(".*(rotating|rotation|vary|variable|mixed).*")) {
            return "Rotating";
        }

        // Flexible shifts
        if (normalized.matches(".*(flex|flexible|varies).*")) {
            return "Flexible";
        }

        // Weekend specific
        if (normalized.matches(".*(weekend|weekends).*")) {
            return "Weekend";
        }

        // Try to detect by time ranges
        // Day: "7a-7p", "7am-7pm", etc.
        if (normalized.matches(".*\\d{1,2}\\s*a[m]?.*\\d{1,2}\\s*p[m]?.*")) {
            return "Day";
        }

        // Night: "7p-7a", "7pm-7am", etc.
        if (normalized.matches(".*\\d{1,2}\\s*p[m]?.*\\d{1,2}\\s*a[m]?.*")) {
            return "Night";
        }

        // If no pattern matched, check for numbers (might be a shift code)
        if (normalized.matches("\\d+")) {
            return "Unknown (Code: " + normalized + ")";
        }

        // Log unknown patterns for future improvement
        System.out.println("WARNING: Unable to normalize shift: '" + rawShift + "' -> Keeping original");

        // If we still can't determine, return the original trimmed value
        return rawShift.trim();
    }

    /**
     * Extracts shift duration in hours from raw shift string
     *
     * @param rawShift The raw shift string (e.g., "8 HR Mids", "12 HR Days")
     * @return Duration in hours as string, or empty string if not found
     */
    private String extractShiftDuration(String rawShift) {
        if (rawShift == null || rawShift.trim().isEmpty()) {
            return "";
        }

        // Pattern to match hour durations: "8 HR", "12 HR", "8 hours", "8-hour", etc.
        Pattern pattern = Pattern.compile("(\\d+)\\s*[-]?\\s*(?:hr|hour)s?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rawShift);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * Extracts shift details into a structured format
     * Returns both normalized shift and duration
     *
     * @param rawShift The raw shift string
     * @return Map containing "type", "duration", and "original" keys
     */
    private Map<String, String> parseShiftDetails(String rawShift) {
        Map<String, String> shiftDetails = new HashMap<>();

        shiftDetails.put("original", rawShift != null ? rawShift.trim() : "");
        shiftDetails.put("type", normalizeShift(rawShift));
        shiftDetails.put("duration", extractShiftDuration(rawShift));

        return shiftDetails;
    }
    private JobsFeeds getStaffingEngineJobsFeedsParsed(Map<String, String> requisitionDetails) {
        System.out.println(" Inside Staffing Engine getStaffingEngineJobsFeedsParsed");

        JobsFeeds jobsFeeds = new JobsFeeds();
        requisitionDetails.entrySet().stream().forEach(item -> {
            System.out.println(item.getKey() + ":" + item.getValue());

            if (item.getKey().contains("Job ID")) {
                jobsFeeds.setSourceID(item.getValue());
                jobsFeeds.setProviderJobID(item.getValue()+"55");
            }
            if (item.getKey().contains("Job Type")){
                jobsFeeds.setWorkType(item.getValue());
            }
            if (item.getKey().contains("Openings")) {
                jobsFeeds.setPositions(Integer.parseInt(item.getValue().trim()));
            }
            if (item.getKey().toLowerCase().contains("Client".toLowerCase())){
                jobsFeeds.setFacility(item.getValue());
            }

            if (item.getKey().contains("Description") ){
                jobsFeeds.setNote(item.getValue());
            }
            if (item.getKey().contains("Shift")) {
                String rawShift = item.getValue();
                Map<String, String> shiftDetails = parseShiftDetails(rawShift);

                jobsFeeds.setShift(shiftDetails.get("type"));

                String duration = shiftDetails.get("duration");
                if (!duration.isEmpty() && jobsFeeds.getDuration() == null) {
                    jobsFeeds.setDuration(duration);
                }

                if (!rawShift.equals(shiftDetails.get("type"))) {
                    String currentNote = jobsFeeds.getNote() != null ? jobsFeeds.getNote() : "";
                    jobsFeeds.setNote(currentNote + "\nOriginal Shift: " + rawShift);
                }
            }
            if (item.getKey().contains("Contract Start Date")) {
                String dateArray[]= item.getValue().split("-");
                jobsFeeds.setFormattedStartDate(dateArray[0].trim()

                );
                jobsFeeds.setStartDate(dateArray[0].trim());

            }
            if (item.getKey().contains("Shifts Per Week")) {
                jobsFeeds.setDurationWeeks(item.getValue().trim());
            }
            if (item.getValue().contains("Additional Details")){

                jobsFeeds.setNote(jobsFeeds.getNote()+".                 "+item.getValue());
            }
            if (item.getKey().contains("Client")) {
                jobsFeeds.setFacility(item.getValue());
            }
            if (item.getKey().contains("Job Title(s)")) {
                jobsFeeds.setTitle(item.getValue());
            }
            if (item.getKey().contains("Shifts Per Week")) {
                jobsFeeds.setShift(item.getValue());
            }

            if (item.getKey().contains("Agency Regular Bill Rate")){
                jobsFeeds.setBillRate(item.getValue());
                jobsFeeds.setbRate(item.getValue());
            }
            if(item.getKey().contains("Profession/ Speciality")){
                String [] proSpec= item.getValue().split("/");
                switch (proSpec.length){
                    case 2:{
                        jobsFeeds.setDegree(proSpec[0]);
                        jobsFeeds.setJobSpecialty(proSpec[1]);
                        break;
                    }
                    case  1:{
                        jobsFeeds.setDegree(proSpec[0]);
                        break;
                    }
                    default:
                        System.out.println("No Profession Summary was found");
                        break;

                }
            }

            if (item.getKey().contains("Location")) {
                String[] location= item.getValue().split(",");
                switch (location.length){
                    case 1:{
                        jobsFeeds.setCity(location[0]);
                        break;
                    }
                    case  2:
                    {
                        jobsFeeds.setCity(location[0]);
                        jobsFeeds.setState(location[1]);
                        break;
                    }
                    case 3: {
                        jobsFeeds.setCity(location[0]);
                        jobsFeeds.setState(location[1]);
                        jobsFeeds.setZip(location[2]);
                        break;
                    }
                    default:{
                        System.out.println("Location Not Found ");
                    }
                }
            }


        });
        jobsFeeds.setStatusString("Open");//"Staffing Engine");
        jobsFeeds.setAlias("StaffingEngine");
        jobsFeeds.setSourceName("StaffingEngine");

//        jobsFeeds.set
        return jobsFeeds;
    }
    private Map<String, String> getNewJobsParsed(String content) {
        String emailHtml = content;//.get(0).toString();//"<html><head><meta http-equiv='content-type' content='text/html; charset=utf-8'></head><body><strong><div><font face='tahoma' color='#000000' size='2'>&nbsp;</font></div></strong><hr tabindex='-1' style='display:inline-block; width:98%'><font face='tahoma' size='2'><b>from:</b> notifications@staffingengine.com &lt;notifications@staffingengine.com&gt;<br><b>sent:</b> thursday, march 13, 2025 11:22:08 am (utc-06:00) central time (us &amp; canada)<br><b>to:</b> nitish vats &lt;nitish.vats@midasconsulting.org&gt;<br><b>subject:</b> new staffingengine job posting 32646 for rn/ cath lab - texas<br></font><br><div></div><div>job id: 32646 <br>description: assist with the insertion of catheters into the heart, and are responsible for measuring and administering special fluids.<br><br>job summary the cardiovascular invasive specialist, under general supervision, helps physicians during catheterization / electrophysiology lab procedures. prepares room and equipment, instructs patients, monitors patient's condition and provides routine and emergency patient care during procedures. contributes to the orientation and training of other team members. help in daily, monthly, and or quarterly quality control initiative through auditing and reporting. <br>openings: 1 <br>job type: permanent <br>profession/ speciality: rn/ cath lab <br>client: baylor scott &amp; white medical center - lakeway <br>location: lakeway, texas, 78738 <br>shift: 12 hr days <br>shifts per week: 3 <br>start and end date: 03/13/2025 - <br><br>agency regular bill rate: 0.00 <br>agency &gt;40 bill rate: 0.00 <br><br>additional details cath lab tech<br><br>****$15,000 sign on bonus for cath lab techs ****<br>baylor scott &amp; white medical center – lakeway<br>address: 100 medical parkway, lakeway, tx 78738<br><br>we’re offering a $15,000 sign on bonus &amp; relo to cath lab (cvis) professionals<br>must hold an rcis, arrt certification * open to training rad techs interested in transitioning into the cath lab*<br>work schedule : 4 -10 hour day shifts, monday - thursday call only. no weekend call<br><br>essential functions of the role<br><br>helps physicians with arteriograms/catheterizations, permanent pacemaker implantations, implantable cardiac defibrillators, diagnostic and interventional, peripheral and coronary, interventional and thrombolytic therapy.<br>follows national and state radiation protection regulations for patients, self and staff. provides emergency procedures (cardiopulmonary resuscitation (cpr), defibrillation, etc.) as needed.<br>performs pre and post-procedure care and monitoring.<br>orients patients for catheterization or electrophysiology procedures.<br>answers related questions to make patient relaxed<br>monitors patient’s ecg, pressures, temperature and impendences while in electrophysiology lab; notifies physician of variances.<br>helps in running rf/cryo ablation systems, 3d mapping, and records other procedural documentation.<br><br>key success factors<br><br>education and/or experience requirements (must meet one of the following): - associates degree in a related field of, or - completion of us military training program and experience equivalent to an associate’s equivalent or 2 years of related cardiovascular lab experience, or - 2 years of related cardiovascular lab experience.<br>must be available for on call response requirements per facility/department policy.<br>knowledge and ability to apply complex invasive cardiac and vascular values, instrumentation and techniques.<br>knowledge of cardiovascular anatomy and physiology.<br>benefits<br><br>our competitive benefits package includes the following immediate eligibility for health and welfare benefits<br><br>401(k) savings plan with dollar-for-dollar match up to 5%<br>tuition reimbursement<br>pto accrual beginning day 1<br><br>qualifications<br><br>education - associate's or 2 years of work experience above the minimum qualification<br>experience - less than 1 year of experience<br>certification/license/registration -<br><br>acls (acls): acls within 30 days of hire/transfer.<br>basic life support (bls): bls within 30 days of hire/transfer.<br>arrt-r radiography (arrt-r), cert cardiac device spec (ccds), cert ep specialist (ceps), medical radiologic tech (mrt), reg cardio electrophysiology (rces), reg cardiovascular invasive sp (rcis): must meet one of the following:american registry of radiologic tech(arrt-r) and mrtnthru tx medical board, or cardio invasive spec(rcis), or reg cardiac electro spec(rces), or cert electro spec(ceps), or cert cardiac device spec (ccds).</div></body></html>";
        // Parse HTML using Jsoup
        Document doc = Jsoup.parse(emailHtml);
//        Document doc = Jsoup.parse(html);

        // Find all div elements
        Elements divs = doc.select("div");

        // LinkedHashMap maintains insertion order
        Map<String, String> jobDetails = new LinkedHashMap<>();

        for (Element div : divs) {
            String[] lines = div.html().split("<br>"); // Split using <br> tag
            String lastKey = null;

            for (String line : lines) {
                line = Jsoup.parse(line).text().trim(); // Clean and extract text

                if (line.contains(":")) {  // Key-value pattern
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    jobDetails.put(key, value);
                    lastKey = key;
                } else if (lastKey != null && !line.isEmpty()) {
                    // Append to previous key if value spans multiple lines
                    jobDetails.put(lastKey, jobDetails.get(lastKey) + " " + line);
                }
            }
        }

        // Print extracted key-value pairs
//        System.out.println("The Map Size " + jobDetails.size());
//        jobDetails.forEach((key, value) -> System.out.println(key + ": " + value));
        return  jobDetails;
    }
    @GetMapping(value = "/ingestOldMails", consumes = {"application/json", "text/plain"}, produces = {"application/json", "text/plain"})
    @ResponseBody
    public ResponseEntity   ingestOldMails() throws IOException {

        OkHttpClient clientO = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        String Token="";
//        OkHttpClient clientO = new OkHttpClient();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n \n}");
        String accessToken = oneDriveServices.getAccessToken();
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(accessToken);
            Token    = (String) json.get("access_token");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Request requestO = new Request.Builder()
//                .url("https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders(%27AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=%27)/messages?%24orderby=receivedDateTime%20asc&%24top=1500")
//                .url("https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages?$orderby=receivedDateTime asc&$top=1000")
//                .url("https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages?$'subject:FW: HWL'&$top=1000")
                .url("https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages?$filter=contains(subject, 'FW: HWL')&$top=1000")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/9.2.0")
                .addHeader("Authorization", "Bearer "+Token)
                .build();

        Response responseO = clientO.newCall(requestO).execute();
        String data = responseO.body().string();
        AllMailsRoot root= objectMapper.readValue(data, AllMailsRoot.class);
        ModelMapper modelMapper = new ModelMapper();
//        root.getValue().stream().filter(x->)
        List<com.midas.consulting.controller.v1.response.hwl.allmails.Value> values = root.getValue();

        // Define the DateTimeFormatter based on the format of the sentDateTime string
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME; // Adjust format if necessary

        List<com.midas.consulting.controller.v1.response.hwl.allmails.Value> sortedValues = values.stream()
                .sorted(Comparator.comparing(value -> LocalDateTime.parse(value.getSentDateTime(), formatter), Comparator.naturalOrder()))
                .collect(Collectors.toList());

        List<com.midas.consulting.controller.v1.response.hwl.allmails.Value> sortedValues3 = values.stream()
                .sorted(Comparator.comparing(value -> LocalDateTime.parse(value.getSentDateTime(), formatter), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        sortedValues.stream().forEach(emailData ->{
            if (emailData.getSubject().contains("HWL"))
            {
                emailData.getLastModifiedDateTime();
                Document doc = Jsoup.parse(emailData.getBody().getContent());
                String jobBinderKey = "default";
                System.out.println("Subject :"+emailData.getSubject()+"\nProcessDate Time : "+ emailData.getSentDateTime());
                if (emailData.getSubject().toLowerCase().contains("Profile(s) requested for requisition".toLowerCase())) {
                    try {
                        jobBinderKey = mailSelector(doc, jobBinderKey, modelMapper.map(emailData, VectorEmail.class));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (emailData.getSubject().toLowerCase().contains("Requisition  is temporarily blocked".toLowerCase())) {
                    parseToStatus(emailData.getBody().getContent(), "Temp Block");
                    System.out.println(emailData.getBody().getContent());
                }
                if (emailData.getSubject().toLowerCase().contains("Requisition  is closed".toLowerCase())) {
                    parseToStatus(emailData.getBody().getContent(), "Closed");
                    System.out.println(emailData.getBody().getContent());
                }
                if (emailData.getSubject().toLowerCase().contains("has been modified".toLowerCase())) {
                    try {
                        modifiedEmails( modelMapper.map(emailData, VectorEmail.class), doc, jobBinderKey);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (emailData.getSubject().toLowerCase().contains("on hold.".toLowerCase())) {
                    parseToStatus(emailData.getBody().getContent(), "On Hold");
                    System.out.println(emailData.getBody().getContent());
                }
                if (emailData.getSubject().toLowerCase().contains("Requisition  is reopened".toLowerCase())) {
                    parseToStatus(emailData.getBody().getContent(), "Open(re)");
                    System.out.println(emailData.getBody().getContent());

                }
                if (emailData.getSubject().contains("Broadcasted Requisition  is withdrawn")){
                    Element divElement = doc.select("div:contains(Please note that previously broadcasted requisition)").first();

                    if (divElement != null) {
                        Element idElement = divElement.select("b").first();
                        if (idElement != null) {
                            String requisitionId = idElement.text();
                            System.out.println("Requisition ID: " + requisitionId);
                            OkHttpClient client = new OkHttpClient();
                            String urlProcess= "http://192.168.2.28:9292/api/jobsmanual/getUpdateBySrcId/" + decodeUrl(requisitionId) + "/" + "Withdrawn/HWL" ;
//                            String urlProcess= "http://localhost:9292/api/allvms/updateJobBySrc/" + decodeUrl(requisitionId) + "/" + "Withdrawn/HWL" ;

                            System.out.println("url to be processed "+ urlProcess);
                            Request request = new Request.Builder()
                                    .url(urlProcess)
                                    .get()
                                    .addHeader("User-Agent", "insomnia/9.2.0")
                                    .build();

                            try {
                                Response response = client.newCall(request).execute();
                                System.out.println("HWL Email Block Update Status : " + response.body().string());
                            } catch (IOException e) {
                                System.out.println("Exception in process URL API for update Job");
                                throw new RuntimeException(e);
                            }

                        } else {
                            System.out.println("Requisition ID not found.");
                        }
                    } else {
                        System.out.println("Specified div not found.");
                    }
                }
            }
            else{
                System.out.println("Skipping as not HWL");
            }
        });
        return new ResponseEntity<>(root, HttpStatus.ACCEPTED);
    }




    @PostMapping(value = "/notification", consumes = {"application/json", "text/plain"}, produces = {"application/json", "text/plain"})

    @ResponseBody
    public ResponseEntity<String> handleNotification(HttpServletRequest httpServletRequest,
                                                     @RequestParam(name = "validationToken", required = false) String validationToken,
                                                     @RequestBody(required = false) String requestBody) throws IOException {

//        System.out.println("request body" + httpServletRequest.getParameter("validationToken"));
        if (httpServletRequest.getParameter("validationToken") != null) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(validationToken);
        }
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        JsonArray valueArray = jsonObject.getAsJsonArray("value");
//        for (JsonElement element : valueArray) {
        JsonElement element = valueArray.get(0);
        JsonObject notificationJson = element.getAsJsonObject();
        JsonObject resourceData = notificationJson.getAsJsonObject("resourceData");
        String messageId = resourceData.get("id").getAsString();

        // Fetch the complete email
        String accessToken = oneDriveServices.getAccessToken();
        JSONParser parser = new JSONParser();

        if (accessToken != null) {
            VectorEmail emailData = new VectorEmail();
            try {
                JSONObject json = (JSONObject) parser.parse(accessToken);
                String Token = (String) json.get("access_token");
//                System.out.println("Access Token: " + Token);
                emailData = fetchEmail(Token, messageId);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (emailData != null ) {
                String link = extractLinkIfThere(emailData.getBody().getContent());
                if (link != null) {
                    crawlerService.startCrawler(baseDir, link);

                } else if (emailData.getBody().getContent().toLowerCase().contains("hwl")) {
                    emailData.getLastModifiedDateTime();
                    Document doc = Jsoup.parse(emailData.getBody().getContent());
                    String jobBinderKey = "default";
                    System.out.println("Subject :"+emailData.getSubject()+"\nProcessDate Time : "+ emailData.getSentDateTime());
                    if (emailData.getSubject().toLowerCase().contains("Profile(s) requested for requisition".toLowerCase())) {
                        jobBinderKey = mailSelector(doc, jobBinderKey, emailData);
                    }
                    if (emailData.getSubject().toLowerCase().contains("Requisition  is temporarily blocked".toLowerCase())) {
                        parseToStatus(emailData.getBody().getContent(), "Temp Block");
                        System.out.println(emailData.getBody().getContent());
                    }
                    if (emailData.getSubject().toLowerCase().contains("Requisition  is closed".toLowerCase())) {
                        parseToStatus(emailData.getBody().getContent(), "Closed");
                        System.out.println(emailData.getBody().getContent());
                    }
                    if (emailData.getSubject().toLowerCase().contains("has been modified".toLowerCase())) {
                        modifiedEmails(emailData, doc, jobBinderKey);
                    }

                    if (emailData.getSubject().toLowerCase().contains("on hold.".toLowerCase())) {
                        parseToStatus(emailData.getBody().getContent(), "On Hold");
                        System.out.println(emailData.getBody().getContent());
                    }
                    if (emailData.getSubject().toLowerCase().contains("Requisition  is reopened".toLowerCase())) {
                        parseToStatus(emailData.getBody().getContent(), "Open(re)");
                        System.out.println(emailData.getBody().getContent());

                    }
                    if (emailData.getSubject().contains("Broadcasted Requisition  is withdrawn")){
                        Element divElement = doc.select("div:contains(Please note that previously broadcasted requisition)").first();

                        if (divElement != null) {
                            Element idElement = divElement.select("b").first();
                            if (idElement != null) {
                                String requisitionId = idElement.text();
                                System.out.println("Requisition ID: " + requisitionId);
                                OkHttpClient client = new OkHttpClient();
                                String urlProcess= "http://34.230.215.187:9292/api/jobsmanual/getUpdateBySrcId/" + decodeUrl(requisitionId) + "/" + "Withdrawn/HWL" ;
//                                String urlProcess= "http://localhost:9292/api/allvms/updateJobBySrc/" + decodeUrl(requisitionId) + "/" + "Withdrawn/HWL" ;

                                System.out.println("url to be processed "+ urlProcess);
                                Request request = new Request.Builder()
                                        .url(urlProcess)
                                        .get()
                                        .addHeader("User-Agent", "insomnia/9.2.0")
                                        .build();

                                try {
                                    Response response = client.newCall(request).execute();
                                    System.out.println("HWL Email Block Update Status : " + response.body().string());
                                } catch (IOException e) {
                                    System.out.println("Exception in process URL API for update Job");
                                    throw new RuntimeException(e);
                                }

                            } else {
                                System.out.println("Requisition ID not found.");
                            }
                        } else {
                            System.out.println("Specified div not found.");
                        }
                    }
                }

            } else {
                System.out.println("No Data was there  is exceeded ");
            }
        }
        return new ResponseEntity<>("Notification received", HttpStatus.ACCEPTED);
    }

    private void modifiedEmails(VectorEmail emailData, Document doc, String jobBinderKey) throws IOException {
        if (doc.select("table").size() == 3) {
            System.out.println("Inside the has been modified doc.select(\"table\").size()==3");
            HashMap<String, String> requisitionDetails = new HashMap<>();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                if (row.select("td").first().text().equals("Requisition Details")) {
                    continue;
                }
                if (row.text().contains("Description / Comment") || row.text().toLowerCase().contains("Description".toLowerCase())) {
                    Elements desChilds = row.select("td:nth-child(1)");
                    if (desChilds.get(0).childNodeSize() == 2) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                }
                if (row.childNodeSize() == 1) {
                    Elements ratesByJobTitle = row.select("td");

                    if (ratesByJobTitle.text().contains("Rates for ")) {
                        Elements desChilds = row.select("td:nth-child(1)");
                        if (desChilds.get(0).childNodeSize() == 1) {
                            jobBinderKey = desChilds.get(0).text();
//                                        requisitionDetails.put("jobRateBinder",  desChilds.get(0).child(0).text());
                        }

                    }
                }
                if (row.childNodeSize() == 4) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    String key2 = row.select("td:nth-child(3)").text().trim();
                    String value2 = row.select("td:nth-child(4)").text().trim();


                    requisitionDetails.put(key, value);
                    requisitionDetails.put(key2, value2);


                }
                if (row.childNodeSize() == 2) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    if (requisitionDetails.containsKey(key)) {
                        requisitionDetails.put((key.replaceAll(" ", "-")), value);
                    }
//                                   requisitionDetails.put(key, value);
                    if (row.text().contains("Rates for ")) {
                        if (jobBinderKey.equalsIgnoreCase("default")) {
                            requisitionDetails.put(key, value);
                        }
                    } else {
                        requisitionDetails.put((jobBinderKey + key), value);
                    }
                }
            }
            System.out.println("Requisition Details:");

            JobsFeeds jobsFeeds = getDefaultValues();
            jobsFeeds = getJobsFeedsParsed(requisitionDetails, jobsFeeds);

            if (emailData.getHasAttachments()) {
                String attachmentLink = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" +
                        emailData.getId() + "/attachments";
                AttachmentRoot attachment = getAttachment(emailData.getId());
                String urls = "";
                for (com.midas.consulting.controller.v1.response.microsoft.attachment.Value x
                        : attachment.getValue()) {
                    MultipartFile multipartFile = new MockMultipartFile(
                            "HWL" + emailData.getSubject(),                     // Name of the file parameter
                            x.getName(),                // Original filename
                            x.getContentType(),               // Content type
                            x.getContentType().getBytes()                // Ciepal content
                    );
                    try {
                        Response response = oneDriveServices.upload(multipartFile);
                        // Parse the JSON response
                        org.json.JSONObject jsonObject2 = new org.json.JSONObject(response.body().string());
                        // Extract the values
                        String webUrl = jsonObject2.getString("webUrl");
                        String downloadUrl = jsonObject2.getString("@microsoft.graph.downloadUrl");
                        // Print the extracted values
                        System.out.println("Web URL: " + webUrl);
                        System.out.println("Download URL: " + downloadUrl);
                        urls += webUrl + ",";
                        urls += downloadUrl + ",";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                jobsFeeds.setUrl(urls);
            }
            updateJob(jobsFeeds);
        }
        if (doc.select("table").size() == 5) {
            System.out.println("Inside the has been modified oc.select(\"table\").size()==5");
            HashMap<String, String> requisitionDetails = new HashMap<>();
            Elements rows = doc.select("tr");
            for (Element row : rows) {

                if (row.text().contains("Description / Comment") || row.text().toLowerCase().contains("Description".toLowerCase())) {
                    Elements desChilds = row.select("td:nth-child(1)");
                    if (desChilds.get(0).childNodeSize() == 2) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                }

                if (row.select("td").first().text().equals("Requisition Details")) {
                    continue;
                }
                if (row.childNodeSize() == 1) {
                    Elements ratesByJobTitle = row.select("td");
                    if (ratesByJobTitle.text().contains("Rates for ")) {
                        Elements desChilds = row.select("td:nth-child(1)");
                        if (desChilds.get(0).childNodeSize() == 1) {
                            jobBinderKey = desChilds.get(0).text();
//                                        requisitionDetails.put("jobRateBinder",  desChilds.get(0).child(0).text());
                        }
                    }
                }
                if (row.childNodeSize() == 4) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    String key2 = row.select("td:nth-child(3)").text().trim();
                    String value2 = row.select("td:nth-child(4)").text().trim();


                    requisitionDetails.put(key, value);
                    requisitionDetails.put(key2, value2);


                }
                if (row.childNodeSize() == 2) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    if (requisitionDetails.containsKey(key)) {
                        requisitionDetails.put((key.replaceAll(" ", "-")), value);
                    }
//                                   requisitionDetails.put(key, value);
                    if (row.text().contains("Rates for ")) {
                        if (jobBinderKey.equalsIgnoreCase("default")) {
                            requisitionDetails.put(key, value);
                        }
                    } else {
                        requisitionDetails.put((jobBinderKey + key), value);
                    }
                }
            }
            System.out.println("Requisition Details:");

            JobsFeeds jobsFeeds = getDefaultValues();
            jobsFeeds = getJobsFeedsParsed(requisitionDetails, jobsFeeds);
            String titleArr[] = jobsFeeds.getTitle().split(",");

            if (emailData.getHasAttachments()) {
                String attachmentLink = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" +
                        emailData.getId() + "/attachments";
                AttachmentRoot attachment = getAttachment(emailData.getId());
                String urls = "";
                for (com.midas.consulting.controller.v1.response.microsoft.attachment.Value x
                        : attachment.getValue()) {
                    MultipartFile multipartFile = new MockMultipartFile(
                            "HWL" + emailData.getSubject(),                     // Name of the file parameter
                            x.getName(),                // Original filename
                            x.getContentType(),               // Content type
                            x.getContentType().getBytes()                // Ciepal content
                    );
                    try {
                        Response response = oneDriveServices.upload(multipartFile);
                        // Parse the JSON response
                        org.json.JSONObject jsonObject2 = new org.json.JSONObject(response.body().string());
                        // Extract the values
                        String webUrl = jsonObject2.getString("webUrl");
                        String downloadUrl = jsonObject2.getString("@microsoft.graph.downloadUrl");
                        // Print the extracted values
                        System.out.println("Web URL: " + webUrl);
                        System.out.println("Download URL: " + downloadUrl);
                        urls += webUrl + ",";
                        urls += downloadUrl + ",";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                jobsFeeds.setUrl(urls);
            }
            if (titleArr.length > 1) {
                JobsFeeds jobsFeeds1 = jobsFeeds;
//                                jobsFeeds1.setBillRate();
                jobsFeeds1.setSourceID(jobsFeeds.getSourceID());
                jobsFeeds1.setProviderJobID(jobsFeeds.getProviderJobID() );
                updateJob(jobsFeeds1);
            }
            updateJob(jobsFeeds);
        }
        if (doc.select("table").size() == 2) {
            System.out.println("Inside the has been modified oc.select(\"table\").size()==2");
            HashMap<String, String> requisitionDetails = new HashMap<>();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                if (row.select("td").first().text().equals("Requisition Details")) {
                    continue;
                }
                if (row.text().contains("Description / Comment") || row.text().toLowerCase().contains("Description".toLowerCase())) {
                    Elements desChilds = row.select("td:nth-child(1)");
                    if (desChilds.get(0).childNodeSize() == 2) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                    if (desChilds.get(0).childNodeSize() == 1) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                    if (desChilds.get(0).childNodeSize() == 3) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                }
                if (row.childNodeSize() == 1) {
                    Elements ratesByJobTitle = row.select("td");
                    if (ratesByJobTitle.text().contains("Rates for ")) {
                        Elements desChilds = row.select("td:nth-child(1)");
                        if (desChilds.get(0).childNodeSize() == 1) {
                            jobBinderKey = desChilds.get(0).text();
//                                        requisitionDetails.put("jobRateBinder",  desChilds.get(0).child(0).text());
                        }
                    }
                }
                if (row.childNodeSize() == 4) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    String key2 = row.select("td:nth-child(3)").text().trim();
                    String value2 = row.select("td:nth-child(4)").text().trim();


                    requisitionDetails.put(key, value);
                    requisitionDetails.put(key2, value2);


                }
                if (row.childNodeSize() == 2) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    if (requisitionDetails.containsKey(key)) {
                        requisitionDetails.put((key.replaceAll(" ", "-")), value);
                    }
                    if (row.text().contains("Rates for ")) {
                        if (jobBinderKey.equalsIgnoreCase("default")) {
                            requisitionDetails.put(key, value);
                        }
                    } else {
                        requisitionDetails.put((jobBinderKey + key), value);
                    }
                }

            }
            System.out.println("Requisition Details:");

            JobsFeeds jobsFeeds = getDefaultValues();
            jobsFeeds = getJobsFeedsParsed(requisitionDetails, jobsFeeds);
            String titleArr[] = jobsFeeds.getTitle().split(",");

            if (emailData.getHasAttachments() && false) {
                String attachmentLink = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" +
                        emailData.getId() + "/attachments";
                AttachmentRoot attachment = getAttachment(emailData.getId());
                String urls = "";
                for (com.midas.consulting.controller.v1.response.microsoft.attachment.Value x
                        : attachment.getValue()) {
                    MultipartFile multipartFile = new MockMultipartFile(
                            "HWL" + emailData.getSubject(),                     // Name of the file parameter
                            x.getName(),                // Original filename
                            x.getContentType(),               // Content type
                            x.getContentType().getBytes()                // Ciepal content
                    );
                    try {
                        Response response = oneDriveServices.upload(multipartFile);
                        // Parse the JSON response
                        org.json.JSONObject jsonObject2 = new org.json.JSONObject(response.body().string());
                        // Extract the values
//                        String webUrl = jsonObject2.getString("webUrl");
//                        String downloadUrl = jsonObject2.getString("@microsoft.graph.downloadUrl");
                        // Print the extracted values
//                        System.out.println("Web URL: " + webUrl);
//                        System.out.println("Download URL: " + downloadUrl);
//                        urls += webUrl + ",";
//                        urls += downloadUrl + ",";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                jobsFeeds.setUrl(urls);
            }
            if (titleArr.length > 1) {
                JobsFeeds jobsFeeds1 = jobsFeeds;
//                                jobsFeeds1.setBillRate();
                jobsFeeds1.setSourceID(jobsFeeds.getSourceID());
                jobsFeeds1.setProviderJobID(jobsFeeds.getProviderJobID());
                updateJob(jobsFeeds1);
            }
            updateJob(jobsFeeds);
        }
    }


    private String mailSelector(Document doc, String jobBinderKey, VectorEmail emailData) throws IOException {
        if (doc.select("table").size() == 3 || doc.select("table").size() == 2) {
            System.out.println("doc.select(\"table\").size() == 3 || doc.select(\"table\").size() == 2");
            HashMap<String, String> requisitionDetails = new HashMap<>();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                if (row.select("td").first().text().equals("Requisition Details")) {
                    continue;
                }
                if (row.text().contains("Description / Comment") || row.text().toLowerCase().contains("Description".toLowerCase())) {
                    Elements desChilds = row.select("td:nth-child(1)");
                    if (desChilds.get(0).childNodeSize() == 2) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                }
                if (row.childNodeSize() == 1) {
                    Elements ratesByJobTitle = row.select("td");
                    if (ratesByJobTitle.text().contains("Rates for ")) {
                        Elements desChilds = row.select("td:nth-child(1)");
                        if (desChilds.get(0).childNodeSize() == 1) {
                            jobBinderKey = desChilds.get(0).text();
//                                        requisitionDetails.put("jobRateBinder",  desChilds.get(0).child(0).text());
                        }
                    }
                }
                if (row.childNodeSize() == 4) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    String key2 = row.select("td:nth-child(3)").text().trim();
                    String value2 = row.select("td:nth-child(4)").text().trim();


                    requisitionDetails.put(key, value);
                    requisitionDetails.put(key2, value2);


                }
                if (row.childNodeSize() == 2) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    if (!requisitionDetails.containsKey(key)) {
                        requisitionDetails.put((key.replaceAll(" ", "-")), value);
                    }
//                                   requisitionDetails.put(key, value);
                    if (row.text().contains("Rates for ")) {
                        if (jobBinderKey.equalsIgnoreCase("default")) {
                            requisitionDetails.put(key, value);
                        }
                    } else {
                        requisitionDetails.put((jobBinderKey + key), value);
                    }
                }
            }
            JobsFeeds jobsFeeds = getDefaultValues();
            jobsFeeds = getJobsFeedsParsed(requisitionDetails, jobsFeeds);


            if (emailData.getHasAttachments() && false) {
                String attachmentLink = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" +
                        emailData.getId() + "/attachments";
                AttachmentRoot attachment = getAttachment(emailData.getId());
                String urls = "";
                for (com.midas.consulting.controller.v1.response.microsoft.attachment.Value x
                        : attachment.getValue()) {
                    MultipartFile multipartFile = new MockMultipartFile(
                            "HWL" + emailData.getSubject(),                     // Name of the file parameter
                            x.getName(),                // Original filename
                            x.getContentType(),               // Content type
                            x.getContentType().getBytes()                // Ciepal content
                    );
                    try {
                        Response response = oneDriveServices.upload(multipartFile);
                        // Parse the JSON response
                        org.json.JSONObject jsonObject2 = new org.json.JSONObject(response.body().string());
                        // Extract the values
                        String webUrl = jsonObject2.getString("webUrl");
                        String downloadUrl = jsonObject2.getString("@microsoft.graph.downloadUrl");
                        // Print the extracted values
                        System.out.println("Web URL: " + webUrl);
                        System.out.println("Download URL: " + downloadUrl);
                        urls += webUrl + ",";
                        urls += downloadUrl + ",";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                jobsFeeds.setUrl(urls);
            }
            updateJob(jobsFeeds);
        }
        if (doc.select("table").size() == 5) {
            System.out.println("Profile(s) requested for requisition doc.select(\"table\").size()==3)");
            HashMap<String, String> requisitionDetails = new HashMap<>();
            Elements rows = doc.select("tr");
            for (Element row : rows) {
                if (row.select("td").first().text().equals("Requisition Details")) {
                    continue;
                }
                if (row.text().contains("Description / Comment") || row.text().toLowerCase().contains("Description".toLowerCase())) {
                    Elements desChilds = row.select("td:nth-child(1)");
                    if (desChilds.get(0).childNodeSize() == 2) {
                        requisitionDetails.put("description", desChilds.get(0).child(1).text());
                    }
                }
                if (row.childNodeSize() == 1) {
                    Elements ratesByJobTitle = row.select("td");
                    if (ratesByJobTitle.text().contains("Rates for ")) {
                        Elements desChilds = row.select("td:nth-child(1)");
                        if (desChilds.get(0).childNodeSize() == 1) {
                            jobBinderKey = desChilds.get(0).text();
//                                        requisitionDetails.put("jobRateBinder",  desChilds.get(0).child(0).text());
                        }
                    }
                }
                if (row.childNodeSize() == 4) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    String key2 = row.select("td:nth-child(3)").text().trim();
                    String value2 = row.select("td:nth-child(4)").text().trim();


                    requisitionDetails.put(key, value);
                    requisitionDetails.put(key2, value2);


                }
                if (row.childNodeSize() == 2) {
                    String key = row.select("td:nth-child(1)").text().trim();
                    String value = row.select("td:nth-child(2)").text().trim();
                    if (requisitionDetails.containsKey(key)) {
                        requisitionDetails.put((key.replaceAll(" ", "-")), value);
                    }
//                                   requisitionDetails.put(key, value);
                    if (row.text().contains("Rates for ")) {
                        if (jobBinderKey.equalsIgnoreCase("default")) {
                            requisitionDetails.put(key, value);
                        }
                    } else {
                        requisitionDetails.put((jobBinderKey + key), value);
                    }
                }
            }
            System.out.println("Requisition Details:");

            JobsFeeds jobsFeeds = getDefaultValues();
            jobsFeeds = getJobsFeedsParsed(requisitionDetails, jobsFeeds);
            String titleArr[] = jobsFeeds.getTitle().split(",");
            if (emailData.getHasAttachments()) {
                String attachmentLink = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" +
                        emailData.getId() + "/attachments";
                AttachmentRoot attachment = getAttachment(emailData.getId());
                String urls = "";
                for (com.midas.consulting.controller.v1.response.microsoft.attachment.Value x
                        : attachment.getValue()) {
                    MultipartFile multipartFile = new MockMultipartFile(
                            "HWL" + emailData.getSubject(),                     // Name of the file parameter
                            x.getName(),                // Original filename
                            x.getContentType(),               // Content type
                            x.getContentType().getBytes()                // Ciepal content
                    );
                    try {
                        Response response = oneDriveServices.upload(multipartFile);
                        // Parse the JSON response
                        org.json.JSONObject jsonObject2 = new org.json.JSONObject(response.body().string());
                        // Extract the values
                        String webUrl = jsonObject2.getString("webUrl");
                        String downloadUrl = jsonObject2.getString("@microsoft.graph.downloadUrl");
                        // Print the extracted values
                        System.out.println("Web URL: " + webUrl);
                        System.out.println("Download URL: " + downloadUrl);
                        urls += webUrl + ",";
                        urls += downloadUrl + ",";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ZonedDateTime zdtLastModified = ZonedDateTime.parse(emailData.getSentDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                jobsFeeds.setPostDate(zdtLastModified.format(formatter));
                jobsFeeds.setUrl(urls);
            }
            if (titleArr.length > 1) {
                JobsFeeds jobsFeeds1 = jobsFeeds;
//                                jobsFeeds1.setBillRate();
                jobsFeeds1.setSourceID(jobsFeeds.getSourceID());
                jobsFeeds1.setProviderJobID(jobsFeeds.getProviderJobID());
                updateJob(jobsFeeds1);
            }
            updateJob(jobsFeeds);
        }

        return jobBinderKey;
    }

    private AttachmentRoot getAttachment(String id) throws IOException {


        AttachmentRoot attachmentRoot = new AttachmentRoot();
        OkHttpClient client = new OkHttpClient();
        String accessToken = oneDriveServices.getAccessToken();
        JSONParser parser = new JSONParser();
        String Token = "";
        if (accessToken != null) {
            VectorEmail emailData = new VectorEmail();
            try {
                JSONObject json = (JSONObject) parser.parse(accessToken);
                Token = (String) json.get("access_token");
                System.out.println("Access Token: " + Token);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        client = new OkHttpClient();

//        String apiResource = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgBGAAAAAADihOybE9EeTK6fgY9oI2lSBwDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAADt7O74d_WqTrqCrmwiEnoIAACizlRrAAA=/attachments";
        String apiResource = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/" + id + "/attachments";

////                              https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgBGAAAAAADihOybE9EeTK6fgY9oI2lSBwDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAADt7O74d_WqTrqCrmwiEnoIAACizlRuAAA=/attachments
//                                https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/mailFolders('AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgAuAAAAAADihOybE9EeTK6fgY9oI2lSAQDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAAA=')/messages/AAMkADcyNTFkMDA5LTBhNGItNGY4Mi1hNjdjLTljZGRiNDUwMDMxZgBGAAAAAADihOybE9EeTK6fgY9oI2lSBwDt7O74d_WqTrqCrmwiEnoIAAAl-z6BAADt7O74d_WqTrqCrmwiEnoIAACizlRdAAA=/attachments
//


        Request request = new Request.Builder()
                .url(apiResource)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/9.2.0")
                .addHeader("Authorization", "Bearer " + Token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                // Read the response body
                String responseBody = response.body().string();
                // Parse the JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                attachmentRoot = objectMapper.readValue(responseBody, AttachmentRoot.class);
                // Handle the parsed object as needed
                System.out.println(attachmentRoot);
            } else {
                System.out.println("Request not successful: " + response.body().string());
            }
            // Close the response
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return attachmentRoot;
    }

    private void parseToStatus(String html, String status) {
        // Parse HTML using Jsoup
        Document doc = Jsoup.parse(html);

        // Find all elements containing "<b>" tag (potential requisition details)
        Elements details = doc.select("div");//doc.select("b");
        String requisition = null;
        String clear = "";

        // Loop through each element to find "Requisition:"
        for (Element element : details) {
            if (element.childNodeSize() > 0) {
                if (element.html().contains("Please note that previously broadcasted")) {
                    String[] lines = String.valueOf(element.html()).split("<br><b>");
                    for (String cols : lines) {
                        if (cols.startsWith("Requisition:")) {
                            if (cols.contains(";")){
                                String[] col = cols.split(";");
                                if (col.length > 1) {
                                    requisition = col[col.length - 1].trim();
                                }
                            }else if (cols.contains(":</b> ")){
                                String[] col = cols.split(":</b> ");
                                if (col.length > 1) {
                                    requisition = col[col.length - 1].trim();
                                }
                            }
                        }
                    }
                    System.out.println("YYY" + element.html());
                }
            }
        }
//                requisition   = text.split(":")[1].trim();

        // Extract other information using similar logic
        OkHttpClient client = new OkHttpClient();
        String urlProcess= "http://34.230.215.187:9292/api/jobsmanual/getUpdateBySrcId/" + decodeUrl(requisition) + "/" + status +"/HWL";
//        String urlProcess= "http://localhost:9292/api/allvms/updateJobBySrc/" + decodeUrl(requisition) + "/" + status +"/HWL";

        System.out.println("url to be processed "+ urlProcess);
        Request request = new Request.Builder()
                .url(urlProcess)
                .get()
                .addHeader("User-Agent", "insomnia/9.2.0")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println("HWL Email Block Update Status : " + response.body().string());
        } catch (IOException e) {
         System.out.println("Exception in process URL API for update Job");
            throw new RuntimeException(e);
        }


//        here need to update the cache by extracting the ID

        // Print or use the extracted information as needed
//            System.out.println("Requisition: " + requisition);
//            System.out.println("System: " + system);
//            System.out.println("Facility: " + facility);
//            System.out.println("Department: " + department);
//            System.out.println("Job Title(s): " + jobTitle);
//            System.out.println("Temporarily Blocked By: " + blockedBy);
//            System.out.println("Temporarily Blocked Date: " + blockedDate);
    }

    public static String decodeUrl(String encodedUrl) {
        String decodedUrl = null;
        try {
            System.out.println("URL to be built"+encodedUrl);
            decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error decoding URL: " + e.getMessage());
            e.printStackTrace();
        }
        return decodedUrl;
    }

    private JobsFeeds getJobsFeedsParsed(HashMap<String, String> requisitionDetails, JobsFeeds jobsFeeds) {

        requisitionDetails.entrySet().stream().forEach(item -> {
            if (item.getKey().contains("Selected Contract Type")) {
                String rawShift = item.getValue();
                Map<String, String> shiftDetails = parseShiftDetails(rawShift);

                jobsFeeds.setShift(shiftDetails.get("type"));

                String currentNote = jobsFeeds.getNote() != null ? jobsFeeds.getNote() : "";
                if (!rawShift.equals(shiftDetails.get("type"))) {
                    jobsFeeds.setNote(currentNote + "\nOriginal Contract Type: " + rawShift);
                }
            }
            if (item.getKey().equals("Type")) {
                jobsFeeds.setWorkType(item.getValue());
            }
            if (item.getKey().toLowerCase().contains("Shift Duration (Hrs)".toLowerCase())){
                jobsFeeds.setDuration(item.getValue());
            }




            if (item.getKey().contains("Drive Time") ){
//                jobsFeeds.setMinBillRate(range[1].replace("$",""));
                    jobsFeeds.setNote(jobsFeeds.getNote()+"\n"+item.getKey()+" :"+ item.getValue());
            }
            if (item.getKey().contains("Shift Type")) {
                String rawShiftType = item.getValue();
                String currentShift = jobsFeeds.getShift();

                if (currentShift != null && !currentShift.isEmpty() &&
                        !currentShift.equals("Unknown")) {

                    Map<String, String> shiftTypeDetails = parseShiftDetails(rawShiftType);
                    String normalizedShiftType = shiftTypeDetails.get("type");

                    if (!normalizedShiftType.equals("Unknown") &&
                            !normalizedShiftType.equals(currentShift)) {
                        jobsFeeds.setShift(normalizedShiftType);
                    }

                    String currentNote = jobsFeeds.getNote() != null ? jobsFeeds.getNote() : "";
                    jobsFeeds.setNote(currentNote + "\nShift Type: " + rawShiftType);
                } else {
                    Map<String, String> shiftDetails = parseShiftDetails(rawShiftType);
                    jobsFeeds.setShift(shiftDetails.get("type"));

                    if (!rawShiftType.equals(shiftDetails.get("type"))) {
                        String currentNote = jobsFeeds.getNote() != null ? jobsFeeds.getNote() : "";
                        jobsFeeds.setNote(currentNote + "\nOriginal Shift Type: " + rawShiftType);
                    }
                }
            }
            if (item.getKey().contains("description") || item.getKey().contains("Description") ||item.getKey().contains("Comment")) {
                jobsFeeds.setNote(jobsFeeds.getNote()+"\n "+ item.getValue());
            }

            if (item.getKey().contains("Contract End Date")) {
                jobsFeeds.setFormattedEndDate(item.getValue());
                jobsFeeds.setEndDate(item.getValue());
            }
            if (item.getKey().contains("Contract Start Date")) {
                jobsFeeds.setFormattedStartDate(item.getValue());
                jobsFeeds.setStartDate(item.getValue());

            }
            if (item.getKey().contains("Weeks")) {
                jobsFeeds.setDurationWeeks(item.getValue().trim());
            }
            if (item.getKey().contains("City")) {
                jobsFeeds.setCity(item.getValue());
            }
            if (item.getKey().contains("Facility")) {
                jobsFeeds.setFacility(item.getValue());
            }
            if (item.getKey().contains("Job Title(s)")) {
                jobsFeeds.setTitle(item.getValue());
            }
            if (item.getKey().contains("Job Title(s)")) {
                jobsFeeds.setDegree(item.getValue());
            }
            if (item.getKey().contains("State")) {
                jobsFeeds.setState(item.getValue());
            }

            if (item.getKey().contains("Guaranteed Hours")) {
                jobsFeeds.setGuaranteedHours(item.getValue().trim());
//                jobsFeeds.setGuaranteedHours("40");
            }
            String reqArr[] = item.getKey().split(" ");
            if (reqArr.length == 1 && reqArr[0].equalsIgnoreCase("Requisition")) {
                jobsFeeds.setProviderJobID(String.valueOf(extractProviderJobId(item.getValue())));
                jobsFeeds.setSourceID(item.getValue());
            }

            if (item.getKey().contains("Day") ){
                if (item.getValue().contains("Range")){
                    String []range=   item.getValue().split(" ");
                    if (range.length==3){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[2].replace("$",""));
                    }
                    if (range.length==5){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[2].replace("$",""));
                    }
//                    jobsFeeds.setNote(jobsFeeds.getNote()+"\n"+item.getKey()+" :"+ item.getValue());
                }
                if (item.getKey().contains("8hr Day") && item.getKey().endsWith("8hr Day")){
                    String []range=   item.getValue().split(" ");
                    if (range.length==2){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[1].replace("$",""));
                    }
                }
                if (item.getKey().contains("Day") && item.getKey().endsWith("Day")){
                    String []range=   item.getValue().split(" ");
                    if (range.length==2){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[1].replace("$",""));
                    }
                    if (range.length==3){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[1].replace("$",""));
                    }
                }
                else  if (item.getKey().contains("8hr Days") && item.getKey().endsWith("8hr Days")){
                    String []range=   item.getValue().split(" ");
                    if (range.length==2){
                        jobsFeeds.setMinBillRate(range[1].replace("$",""));
                        jobsFeeds.setBillRate(range[1].replace("$",""));
                    }
                }
            }

        });

        jobsFeeds.setAlias("HWL");
        jobsFeeds.setSourceName("HWL");

//        jobsFeeds.set
        return jobsFeeds;
    }

    private static boolean testRateFromStr(String text) {
//        String text = "Rates for Physical Therapist8hr Day";
        // Define the regular expression pattern
        if (text
                .contains("(Holiday)") || text
                .contains("(Orientation)"))
            return false;
        Pattern pattern = Pattern.compile("Rates\\sfor\\s.+?8hr\\sDay(?![^\\s])");


        // Use a matcher to find matches of the pattern
        Matcher matcher = pattern.matcher(text);

        // Check if the pattern matches
        return (matcher.find());
    }

    public static Double getNumberFromString(String input) {
//        String input = "Rates for Physical Therapist8hr Day";
        String regex = "\\d+";
        Double number = 0d;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
//            System.out.println("Found number: " + matcher.group());
            number = Double.valueOf(matcher.group());
        }
        return number;
    }

    public static float getRateFromStr(String input) {

        // Compile the regex pattern to extract the number with decimal
        Pattern pattern = Pattern.compile("\\$(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Extract the number as a string
            String numberString = matcher.group(1);

            // Convert the extracted string to a float
            return Float.parseFloat(numberString);

        } else {
            System.out.println("No match found");
        }
        return 0;
    }


    private static void updateJob(JobsFeeds jobsFeeds) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert JobsFeeds object to JSON using ObjectMapper
        String jobsFeedsJson = objectMapper.writeValueAsString(jobsFeeds);
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jobsFeedsJson);

        // Prepare the request
        Request request = new Request.Builder()
                // Replace with the appropriate URL (UAT or PROD)
                .url("http://34.230.215.187:9292/api/jobsmanual")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/8.6.1")
                .addHeader("X-Tenant", "670a48b168b0640a262870c4")
                .build();

        // Execute the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Print the response
            System.out.println("Before jobsFeedsJson : " + jobsFeedsJson);
            System.out.println("Before Sending : " + body);
            System.out.println("Execution : " + response.body().string());
        } catch (Exception e) {
            // Handle any exceptions
            System.err.println("Error during API call: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //
//    private static void updateJob(JobsFeeds jobsFeeds) throws IOException {
//        OkHttpClient client = new OkHttpClient();
////        JobsFeeds  jobsFeeds1 = getFeedParsed(jobsFeeds)
//        ObjectMapper mapper = new ObjectMapper();
//
//
//        Gson gson = new Gson();
//        String jobsFeedsJson = gson.toJson(jobsFeeds);
//        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
//        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, jobsFeedsJson);
//        Request request = new Request.Builder()
////                UAT
//                .url("http://34.230.215.187:9292/api/jobsmanual")
////                PROD
////                .url("http://localhost:9292/jobsmanual")
////        .url("http://34.230.215.187:9291/api/medical-solutions/updateJobHWL")
//                .post(body)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("User-Agent", "insomnia/8.6.1")
//                .build();
//
//        Response response = client.newCall(request).execute();
//        System.out.println("Before jobsFeedsJson : " + jobsFeedsJson );
//        System.out.println("Before Sending : " + body );
//        System.out.println("Execution : " + response.body().string());
//    }

    public static Integer extractProviderJobId(String inputString) {
//        String inputString = "DPTI-Hummelstown, PA (2010-1614)-AN-0001";
        Integer uniqueNumber = 0;
        try {
            // Create a SHA-256 hash instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Calculate hash value for the input string
            byte[] hashBytes = digest.digest(inputString.getBytes(StandardCharsets.UTF_8));

            // Convert the hash bytes to a positive BigInteger
            for (int i = 0; i < 4; i++) {
                uniqueNumber <<= 8;
                uniqueNumber |= hashBytes[i] & 0xFF;
            }

            // Ensure the generated number is non-negative
            uniqueNumber = Math.abs(uniqueNumber);

            // Print the generated unique number
            System.out.println("Generated Unique Number: " + uniqueNumber);

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: SHA-256 algorithm not found.");
            e.printStackTrace();
        }
        return uniqueNumber;
    }


    private static String extractValue(String content, int index, String delimiter) {
        if (index > 0 && content.length() > index + delimiter.length()) {
            return content.substring(index + delimiter.length()).trim();
        } else {
            return "";  // Return empty string if value not found
        }
    }

    private static String extractValue(String content, String targetText) {
        int index = content.indexOf(targetText);
        if (index > 0) {
            int nextSpace = content.indexOf(" ", index);  // Find next space after target text
            if (nextSpace > index) {
                return content.substring(index + targetText.length(), nextSpace).trim();
            } else {
                return content.substring(index + targetText.length()).trim();  // Handle cases without following space
            }
        } else {
            return "";
        }
    }

    private static JobsFeeds getDefaultValues() {
        JobsFeeds jobsFeedsToBeReturned = new JobsFeeds();


        jobsFeedsToBeReturned.setProviderJobID(String.valueOf(12));//": 47915739,
        jobsFeedsToBeReturned.setReferenceID(String.valueOf(12));//": "47915739",
        jobsFeedsToBeReturned.setSourceID("HWL");//": "265461",

        jobsFeedsToBeReturned.setAbbreviatedTitle("");
        jobsFeedsToBeReturned.setAddress("");
        jobsFeedsToBeReturned.setAlias("PrimeTimeHealth");
        jobsFeedsToBeReturned.setAssociation("");
        jobsFeedsToBeReturned.setAtsID("999999739");
        jobsFeedsToBeReturned.setAutoOffer_Fl(false);
        jobsFeedsToBeReturned.setbCBRate(String.valueOf(0d));
        jobsFeedsToBeReturned.setbCRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setbHRate(String.valueOf(0d));//": 140,
//          jobsFeedsToBeReturned.setBillRate();//": 130,
        jobsFeedsToBeReturned.setbMinPRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setbMinRate(String.valueOf(0d));//": 130,
        jobsFeedsToBeReturned.setbOCRate(String.valueOf(0d));//": 2,
        jobsFeedsToBeReturned.setBonus(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setbOrRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setbOTRate(String.valueOf(0d));//": 140,
        jobsFeedsToBeReturned.setbPRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setBillRate(String.valueOf(0d));//": 130,
        jobsFeedsToBeReturned.setBuyer("");
        jobsFeedsToBeReturned.setCallbackRate("0");//": "130 (All Inclusive Rate)",
        jobsFeedsToBeReturned.setCategory("Other");//,
        jobsFeedsToBeReturned.setChangeDate(new Date().toString());//": "2024-01-30T18:15:44",
        jobsFeedsToBeReturned.setChargeRate("");
        jobsFeedsToBeReturned.setCleanDegree("NA");//": "RN",
        jobsFeedsToBeReturned.setCleanDegreeID("0");//": 0,
        jobsFeedsToBeReturned.setCleanShift("Unknown");
        jobsFeedsToBeReturned.setCleanSpecialty("");//": "Med Surg Peds Adult",
        jobsFeedsToBeReturned.setCleanSpecialtyID("0");//": 0,
        jobsFeedsToBeReturned.setCoordinator("");//": "Breanne Erickson",
        jobsFeedsToBeReturned.setCustomerID("0");//": 3219,
        jobsFeedsToBeReturned.setCustomField1("");
        jobsFeedsToBeReturned.setCustomField2("");
        jobsFeedsToBeReturned.setDegree("");//": "RN",

        jobsFeedsToBeReturned.setHoldFl(false);// ": false,
        jobsFeedsToBeReturned.setHolidayRate("0");//": "140 (All Inclusive Rate + $10)",
        jobsFeedsToBeReturned.setHotFl(false);//": false,
        jobsFeedsToBeReturned.setIndustry("");
        jobsFeedsToBeReturned.setASAP(true);//": false,
        jobsFeedsToBeReturned.setJobBoardDegree("");
        jobsFeedsToBeReturned.setJobBoardSpecialty("");
        jobsFeedsToBeReturned.setJobURL("");//": "https://vms.medefis5.com/jobs/265461",
        jobsFeedsToBeReturned.setLicenses("");//": "State License Details: Must Be Currently Active;  General Certifications (BLS/BCLS)",
        jobsFeedsToBeReturned.setLocal(String.valueOf(false));//": false,
        jobsFeedsToBeReturned.setLodgingStipendW(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setMaxPayRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setMealStipendW(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setMinBillRate(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setMinExperienceRequired("");//": "2",

        jobsFeedsToBeReturned.setShiftRateDifferential("");
        jobsFeedsToBeReturned.setSkills("");

        jobsFeedsToBeReturned.setSourceName("MSP Prime Time Health");//": "Medefis5",
        jobsFeedsToBeReturned.setTotalStipendH(String.valueOf(0d));//": 0,
        jobsFeedsToBeReturned.setUnit("NA");//": "Med-surg Adult, Med-surg Mother/Baby, Med-surg Peds, Med-Surg Ortho, Critical Access Hospital RN",
        jobsFeedsToBeReturned.setWinterPlanNeed("No");//": "No",
        jobsFeedsToBeReturned.setWorkLevel("");
        jobsFeedsToBeReturned.setWorkType("1");//": "1",
        jobsFeedsToBeReturned.setZip("");
        jobsFeedsToBeReturned.setStatusString("Open");
        return jobsFeedsToBeReturned;
    }

    @ResponseBody
    @GetMapping(value = "/notification")
    public ResponseEntity<String> handleValidation(@RequestParam("validationToken") String validationToken) {
//        String validationToken = request.getParameter("validationToken");
        System.out.println("Received validation request: " + validationToken);
        return ResponseEntity.ok(validationToken);
    }

    private String extractLinkIfThere(String content) {
        // Define the regular expression pattern
        String regex = "https://vms\\.vectorvms\\.com/7000/7020\\.aspx\\?reqID=\\d+";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher for the given text
        Matcher matcher = pattern.matcher(content);

        // Check if the pattern matches the text
        if (matcher.find()) {
            // Extract the matched URL
            String url = matcher.group();

            // Output the extracted URL
            System.out.println("Extracted URL: " + url);
            return url;
        } else {
            System.out.println("No match found.");
            return null;
        }
    }

    private VectorEmail fetchEmail(String accessToken, String messageId) throws IOException {
        OkHttpClient client = new OkHttpClient();
//        String emailUrl = "https://graph.microsoft.com/v1.0/me/messages/" + messageId;

        String emailUrl = "https://graph.microsoft.com/v1.0/users/dheeraj.singh@midasconsulting.org/messages/" + messageId;


        Request request = new Request.Builder()
                .url(emailUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {

                System.out.println("REVEIVED EMAIL");
                String responseBody = response.body().string();
                VectorEmail vectorEmail = objectMapper.readValue(responseBody, VectorEmail.class);
                return vectorEmail;
            } else {
                String errorMessage = "Failed to fetch email: " + response.body().string();
                System.out.println(errorMessage);
                // Optionally log more details like response code, headers, etc.
                System.out.println("Response code: " + response.code());
                System.out.println("Response body: " + response.body().string());
                return null;
            }
        } catch (IOException e) {
            // Handle IO exceptions such as network issues
            e.printStackTrace();
            return null;
        }
    }


    @ResponseBody
    @GetMapping(value = "/fetchHWLProfileRequired")
    public ResponseEntity<String> fetchHWLProfileRequired() {
        return ResponseEntity.ok("validationToken");
    }

}