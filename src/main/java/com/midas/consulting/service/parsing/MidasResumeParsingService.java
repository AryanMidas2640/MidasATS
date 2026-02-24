
package com.midas.consulting.service.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.textkernel.tx.models.api.parsing.ParseResumeResponse;
import com.midas.consulting.controller.v1.response.parsing.midas.ResumeParsingRespone;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class MidasResumeParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(MidasResumeParsingService.class);

    public ResumeParsingRespone parseResume(String filePath) {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .build();

            // Create the file object
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // Create the request body with the file
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/pdf"), file);
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            // Build the request
            Request request = new Request.Builder()

                    .url("http://150.241.246.11:8000/api/upload")
                    .post(requestBody)
                    .addHeader("Content-Type", "multipart/form-data")
                    .addHeader("User-Agent", "insomnia/9.2.0")
                    .build();

            // Execute the request and get the response
            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Parse the response body
                return objectMapper.readValue(response.body().string(), ResumeParsingRespone.class);
            } else {
                // Handle unsuccessful response
                System.err.println("Request failed: " + response.code() + " - " + response.message());
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public String parseResumeStr(String filePath) {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .build();

            // Create the file object
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            // Create the request body with the file
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/pdf"), file);
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            // Build the request
            Request request = new Request.Builder()

                    .url("http://150.241.246.11:8000/api/upload")
                    .post(requestBody)
                    .addHeader("Content-Type", "multipart/form-data")
                    .addHeader("User-Agent", "insomnia/9.2.0")
                    .build();

            // Execute the request and get the response
            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Parse the response body
                return response.body().string();//objectMapper.readValue(response.body().string(), ResumeParsingRespone.class);
            } else {
                // Handle unsuccessful response
                System.err.println("Request failed: " + response.code() + " - " + response.message());
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public String parseResumeString(String fullText) {
        Response response = null;

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .build();

            // Build the JSON request body
            JSONObject json = new JSONObject();
            json.put("text", fullText);
            json.put("parser", "tika");
            json.put("output", "dict");
            json.put("debug", false);
//            json.put("skills", "string");

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    json.toString()
                        );

            Request request = new Request.Builder()
                    .url("http://150.241.246.11:8000/api/parse-text")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("Request failed: {} - {} - {}", response.code(), response.message(), errorBody);
                return errorBody;
            }
        } catch (Exception ex) {
            logger.error("Exception occurred while parsing resume: {}", ex.getMessage(), ex);
            return ex.getMessage();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

//    public String parseResumeFullText(String filePath) {
//        Response response = null;
//        ResumeParsingRespone resumeParsingResponse = new ResumeParsingRespone();
//        try {
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .connectTimeout(5, TimeUnit.MINUTES)
//                    .readTimeout(5, TimeUnit.MINUTES)
//                    .writeTimeout(5, TimeUnit.MINUTES)
//                    .build();
//
//            // Create the file object
////            File file = new File(filePath);
////            if (!file.exists()) {
////                throw new IllegalArgumentException("File not found: " + filePath);
////            }
//
//            // Create the request body with the file
//            RequestBody fileBody = RequestBody.create(MediaType.parse("application/pdf"), file);
//            MultipartBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("file", file.getName(), fileBody)
//                    // .addFormDataPart("userId", "12345") // Add additional fields if required
//                    .build();
//
//            // Build the request
//            Request request = new Request.Builder()
//                    .url("http://150.241.246.11:8000/api/upload")
//                    .post(requestBody)
//                    // .addHeader("User-Agent", "insomnia/9.2.0") // Optional
//                    .build();
//
//            // Execute the request and get the response
//            response = client.newCall(request).execute();
//
//            if (response.isSuccessful()) {
//                // Parse the response body
//                String responseBody = response.body() != null ? response.body().string() : "";
//                return responseBody;
//            } else {
//                // Handle unsuccessful response
//                String errorBody = response.body() != null ? response.body().string() : "No response body";
//                logger.error("Request failed: {} - {} - {}", response.code(), response.message(), errorBody);
//                resumeParsingResponse.setMessage("Could not parse: " + response.message());
//                return errorBody;
//            }
//        } catch (IOException ex) {
//            logger.error("IOException occurred while parsing resume: {}", ex.getMessage(), ex);
//            resumeParsingResponse.setMessage("Could not parse: " + ex.getMessage());
//            return ex.getMessage();
//        } catch (IllegalArgumentException ex) {
//            logger.error("IllegalArgumentException: {}", ex.getMessage(), ex);
//            resumeParsingResponse.setMessage("Could not parse: " + ex.getMessage());
//            return ex.getMessage();
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//    }
}

//package com.midas.consulting.service.parsing;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.textkernel.tx.models.api.parsing.ParseResumeResponse;
//import okhttp3.*;
//import org.springframework.stereotype.Service;
//
//@Service
//public class MidasResumeParsingService {
//
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public ParseResumeResponse parseResume(String filePath) {
//        Response response= null;
//        try {
//
//            OkHttpClient client = new OkHttpClient();
//
//            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=---011000010111000001101001");
//            RequestBody body = RequestBody.create(mediaType, "-----011000010111000001101001\r\nContent-Disposition: form-data; name=\"file\"; filename=\"Open Cats -(Only Server) AWS Pricing Calculator.pdf\"\r\nContent-Type: application/pdf\r\n\r\n\r\n-----011000010111000001101001--\r\n");
//            Request request = new Request.Builder()
//                    .url("http://34.230.215.187:8000/api/upload")
//                    .post(body)
//                    .addHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001")
//                    .addHeader("User-Agent", "insomnia/9.2.0")
//                    .build();
//             response = client.newCall(request).execute();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//        return response;
//    }
//}
