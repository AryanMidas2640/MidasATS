package com.midas.consulting.service.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.midas.consulting.controller.v1.request.parsing.textkernal.ResumeParserRequest;
import com.midas.consulting.model.candidate.parsed.Root;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class RchilliResumeParsingService {

    // Set APIURL as the REST API URL for resume parsing provided by RChilli.
    private static final String APIURL = "https://api.us.textkernel.com/tx/v10/parser/resume";
    // Set USERKEY as provided by RChilli.
    private static final String ACCID = "86995237";
    private static final String ServiceKey = "w8pP53576AAijV3qjCiSSnDUQFIgIM+dfC4Cdngw";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Root parseResume(String filePath) {
        File resumeFile = new File(filePath);
        try {
            String lastModifiedSate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileData = Base64.encodeBase64String(Files.readAllBytes(resumeFile.toPath()));
            ResumeParserRequest resumeParserRequest = new ResumeParserRequest();
            resumeParserRequest.setDocumentAsBase64String(fileData);
            resumeParserRequest.setDocumentLastModified(lastModifiedSate);
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json-patch+json");
            RequestBody body = RequestBody.create(mediaType, new Gson().toJson(resumeParserRequest));
            Request request = new Request.Builder()
                    .url(APIURL)
                    .post(body)
                    .addHeader("accept", "text/plain")
                    .addHeader("Tx-AccountId", ACCID)
                    .addHeader("Tx-ServiceKey", ServiceKey)
                    .addHeader("Content-Type", "application/json-patch+json")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string().toString();
            return objectMapper.readValue(jsonResponse, Root.class);
//            return  resumeParserData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
