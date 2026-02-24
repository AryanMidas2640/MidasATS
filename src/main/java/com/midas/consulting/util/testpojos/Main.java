package com.midas.consulting.util.testpojos;

import okhttp3.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();

        // Step 1: Generate Authentication Token
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody authBody = RequestBody.create(mediaType, "{\n\t\"email\": \"dheeraj.singh@midasconsulting.org\",\n\t\"password\": \"Dheeraj@1989\",\n\t\"api_key\": \"d670d8c6e5d362937c4af10c63b986235e448376b8eb26a136244e7cccb3e783\",\n\t\"json\": 1\n}");

        Request authRequest = new Request.Builder()
                .url("https://api.ceipal.com/v1/createAuthtoken/")
                .post(authBody)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response authResponse = client.newCall(authRequest).execute();
            if (authResponse.isSuccessful()) {
                String authResponseBody = authResponse.body().string();
                System.out.println("Auth Response: " + authResponseBody);

                // Extract access token from the response (parsing required)
                String accessToken = extractToken(authResponseBody, "access_token");
                String refreshToken = extractToken(authResponseBody, "refresh_token");

                // Step 2: Use the Access Token to Make Another API Call
                RequestBody body = RequestBody.create(mediaType, "{\n\t\"job_code\": \"JPC-24593\",\n\t\"job_title\": \"Temp-RN-ICU-Neonatal(NICU)(Rotating)Robbinsdale, MN\",\n\t\"bill_rate\": \"95.0\",\n\t\"pay_rate\": \"95.5\",\n\t\"job_start_date\": \"\",\n\t\"job_end_date\": \"\",\n\t\"remote_job\": \"\",\n\t\"country\": \"{country}\",\n\t\"states\": \"MN\",\n\t\"city\": \"Robbinsdale\",\n\t\"zip_code\": \"55422\",\n\t\"job_status\": \"\",\n\t\"job_type\": \"{job_type}\",\n\t\"client\": \"{client}\",\n\t\"client_manager\": \"{client_manager}\",\n\t\"end_client\": \"{end_client}\",\n\t\"client_job_id\": \"50881\",\n\t\"required_documents_for_submissions\": \"\",\n\t\"turnaround_time\": \"\",\n\t\"priority\": \"\",\n\t\"duration\": \"12\",\n\t\"work_authorization\": \"{work_authorization}\",\n\t\"ceipal_ref__\": \"{ceipal_ref__}\",\n\t\"clearance\": \"{clearance}\",\n\t\"address\": \"3300 Oakdale Ave. N.\",\n\t\"degree\": \"Registered Nurse (RN)\",\n\t\"experience\": \"{experience}\",\n\t\"skills\": \"{skills}\",\n\t\"languages\": \"{languages}\",\n\t\"number_of_positions\": \"1\",\n\t\"maximum_allowed_submissions\": \"{maximum_allowed_submissions}\",\n\t\"tax_terms\": \"{tax_terms}\",\n\t\"sales_manager\": \"{sales_manager}\",\n\t\"department\": \"\",\n\t\"recruitment_manager\": \"{recruitment_manager}\",\n\t\"account_manager\": \"{account_manager}\",\n\t\"assigned_to\": \"{assigned_to}\",\n\t\"primary_recruiter\": \"{primary_recruiter}\",\n\t\"comments\": \"{comments}\",\n\t\"additional_notifications\": \"{additional_notifications}\",\n\t\"career_portal_published_date\": \"{career_portal_published_date}\",\n\t\"job_description\": \"<p>The Registered Staff Nurse (RN) provides holistic, care to customers and families. The focus is on independent, collaborative and delegated nursing functions as described in standards and competencies specific to the assigned service area. The scope of practice may include the care of neonates, infants, children, young adults, adults, and older adults throughout the life cycle. The RN executes the Standards of Nursing Practice including: assessment; nursing diagnosis; outcome identification; planning; implementation; and evaluation. The RN develops and revises the customer's plan of care, coordinates care among disciplines and delegates activities to other customer care providers as appropriate. The RN participates in shared accountability and decision-making for nursing practices. Through active participation in practice and by implementing personal leadership qualities and skill, the RN defines, implements, and improves practice standards and the environment of care and promotes remarkable customer care.</p><p>Variation of duties may result based on the assigned customer care unit(s). This may include differences in the specific clinical skills required of the RN. Unit based skill lists outline expectations beyond the house wide RN requirements.</p>\\n<p>At North Memorial Health you will be a valuable member of our inclusive and nurturing team that values professional growth, offering big benefits like tuition reimbursement and a supportive culture with a world of education and training opportunities.</p><p>Our health system encompasses two hospital locations in Robbinsdale and Maple Grove as well as a network of 25 clinics which includes 13 primary clinics, 6 specialty clinics, 4 urgent care/urgency centers and emergency care offerings covering five counties. Our Robbinsdale Hospital, established in 1954, is a 385-bed facility recognized as the top Level 1 Trauma center for 25 years, as well as serving as a Level II pediatric trauma center, and Level III NICU. Our Maple Grove Hospital was established in 2009, is a 134-bed facility recognized as a top hospital in the state for Women and Children Care, with a Level III NICU, and is the largest Family Birth Center in the state (~5,000 deliveries per year and over 60,000 babies delivered). Both have been named to the 2022 Fortune/Merative 100 Top Hospitals® list, 2023 Women’s Choice Award Best Hospitals list.</p><div><u><b>Further North Memorial&nbsp;Specifics:</b></u><ul><li><i>Nonbillable Hours: 4 hours&nbsp;</i></li><li><i>Dress Code - Galaxy Blue Scrubs</i></li></ul></div><p><b>**This client has a rebate of 2% of all billings related to this assignment; passthrough expenses are not subject to any rebates. Please see section 14.6.1 of SSP Master Agreement for details.**</b></p>\\n[StateLicense{name='Minnesota', abbrevation='MN', region=Region{name='Zone 3', description=null, code=30, states=[Iowa, Illinois, Indiana, Michigan, Minnesota, Missouri, Mississippi, Ohio, Wisconsin]}}]\\n<ul><li>2+ Years of Experience -&nbsp;<b>Required</b></li><li>Newborn stabilization and resuscitation experience -&nbsp;<b>Required</b></li><li><b></b>BLS (AHA or ARC) -&nbsp;<b>Required</b><br></li><li>NRP -&nbsp;<b>Required</b><b><br></b></li><li>Newborn Population Experience -<b> Required</b><br></li><li>STABLE <i>- Highly Preferred&nbsp;</i><b><br></b></li><li>Level 2/3 NICU Experience&nbsp;-<i><b>&nbsp;</b></i><b><i></i></b><i>Highly Preferred</i></li><li>Epic Experience -<i> Preferred</i></li></ul><b>Profile Requirements:&nbsp;</b><ul><ul><li>Work history</li><li>Skills Checklist, current to 1 year, pertinent to the specialty</li><li>Two Supervisory reference forms, taken in the last two years</li></ul></ul>\\nMust be active!\",\n\t\"post_job_on_career_portal\": \"<p>The Registered Staff Nurse (RN) provides holistic, care to customers and families. The focus is on independent, collaborative and delegated nursing functions as described in standards and competencies specific to the assigned service area. The scope of practice may include the care of neonates, infants, children, young adults, adults, and older adults throughout the life cycle. The RN executes the Standards of Nursing Practice including: assessment; nursing diagnosis; outcome identification; planning; implementation; and evaluation. The RN develops and revises the customer's plan of care, coordinates care among disciplines and delegates activities to other customer care providers as appropriate. The RN participates in shared accountability and decision-making for nursing practices. Through active participation in practice and by implementing personal leadership qualities and skill, the RN defines, implements, and improves practice standards and the environment of care and promotes remarkable customer care.</p><p>Variation of duties may result based on the assigned customer care unit(s). This may include differences in the specific clinical skills required of the RN. Unit based skill lists outline expectations beyond the house wide RN requirements.</p>\\n<p>At North Memorial Health you will be a valuable member of our inclusive and nurturing team that values professional growth, offering big benefits like tuition reimbursement and a supportive culture with a world of education and training opportunities.</p><p>Our health system encompasses two hospital locations in Robbinsdale and Maple Grove as well as a network of 25 clinics which includes 13 primary clinics, 6 specialty clinics, 4 urgent care/urgency centers and emergency care offerings covering five counties. Our Robbinsdale Hospital, established in 1954, is a 385-bed facility recognized as the top Level 1 Trauma center for 25 years, as well as serving as a Level II pediatric trauma center, and Level III NICU. Our Maple Grove Hospital was established in 2009, is a 134-bed facility recognized as a top hospital in the state for Women and Children Care, with a Level III NICU, and is the largest Family Birth Center in the state (~5,000 deliveries per year and over 60,000 babies delivered). Both have been named to the 2022 Fortune/Merative 100 Top Hospitals® list, 2023 Women’s Choice Award Best Hospitals list.</p><div><u><b>Further North Memorial&nbsp;Specifics:</b></u><ul><li><i>Nonbillable Hours: 4 hours&nbsp;</i></li><li><i>Dress Code - Galaxy Blue Scrubs</i></li></ul></div><p><b>**This client has a rebate of 2% of all billings related to this assignment; passthrough expenses are not subject to any rebates. Please see section 14.6.1 of SSP Master Agreement for details.**</b></p>\\n[StateLicense{name='Minnesota', abbrevation='MN', region=Region{name='Zone 3', description=null, code=30, states=[Iowa, Illinois, Indiana, Michigan, Minnesota, Missouri, Mississippi, Ohio, Wisconsin]}}]\\n<ul><li>2+ Years of Experience -&nbsp;<b>Required</b></li><li>Newborn stabilization and resuscitation experience -&nbsp;<b>Required</b></li><li><b></b>BLS (AHA or ARC) -&nbsp;<b>Required</b><br></li><li>NRP -&nbsp;<b>Required</b><b><br></b></li><li>Newborn Population Experience -<b> Required</b><br></li><li>STABLE <i>- Highly Preferred&nbsp;</i><b><br></b></li><li>Level 2/3 NICU Experience&nbsp;-<i><b>&nbsp;</b></i><b><i></i></b><i>Highly Preferred</i></li><li>Epic Experience -<i> Preferred</i></li></ul><b>Profile Requirements:&nbsp;</b><ul><ul><li>Work history</li><li>Skills Checklist, current to 1 year, pertinent to the specialty</li><li>Two Supervisory reference forms, taken in the last two years</li></ul></ul>\\nMust be active!\",\n\t\"job_billrate_summary\": \"Job Bill Rate Summary {id=dc839d8b-0f9e-4d36-b0f8-1234c4766a8b}\",\n\t\"job_payrate_summary\": \"Job Pay Rate Summary {id=dc839d8b-0f9e-4d36-b0f8-1234c4766a8b}\",\n\t\"job_salary_summary\": \"Job Salary Summary {id=dc839d8b-0f9e-4d36-b0f8-1234c4766a8b}\",\n\t\"primary_address\": \"3300 Oakdale Ave. N.\",\n\t\"primary_city\": \"Robbinsdale\",\n\t\"primary_country\": \"{country}\",\n\t\"primary_state\": \"MN\",\n\t\"primary_zip_code\": \"55422\",\n\t\"secondary_address\": \"\",\n\t\"secondary_city\": \"\",\n\t\"secondary_country\": \"{country}\",\n\t\"secondary_state\": \"\",\n\t\"secondary_zip_code\": \"\",\n\t\"primary_job_address\": \"\",\n\t\"primary_job_city\": \"\",\n\t\"primary_job_country\": \"{country}\",\n\t\"primary_job_state\": \"\",\n\t\"primary_job_zip_code\": \"\",\n\t\"secondary_job_address\": \"\",\n\t\"secondary_job_city\": \"\",\n\t\"secondary_job_country\": \"{country}\",\n\t\"secondary_job_state\": \"\",\n\t\"secondary_job_zip_code\": \"\",\n\t\"applicable_for_career_portal\": \"0\",\n\t\"client_special_instruction\": \"\",\n\t\"education\": \"Registered Nurse (RN)\",\n\t\"qualification_experience\": \"{experience}\",\n\t\"applicable_for_mobile_app\": \"0\",\n\t\"user_id\": \"{user_id}\",\n\t\"vendor_email_to\": \"\",\n\t\"vendor_email_cc\": \"\",\n\t\"vendor_email_bcc\": \"\",\n\t\"vendor_email_subject\": \"Temp-RN-ICU-Neonatal(NICU)(Rotating)Robbinsdale, MN\",\n\t\"vendor_email_body\": \"\"\n}");

                Request request = new Request.Builder()
                        .url("https://api.ceipal.com/v1/joborder/")
                        .post(body)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        System.out.println("API Response: " + response.body().string());
                    } else {
                        System.out.println("API Call Failed: " + response.message());
                    }
                }
            } else {
                System.out.println("Authentication Failed: " + authResponse.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract tokens from JSON response
    private static String extractToken(String jsonResponse, String tokenType) {
        // Simple extraction logic based on known response structure
        String token = null;
        String[] parts = jsonResponse.split(",");
        for (String part : parts) {
            if (part.contains(tokenType)) {
                token = part.split(":")[1].replace("\"", "").trim();
                break;
            }
        }
        return token;
    }
}
