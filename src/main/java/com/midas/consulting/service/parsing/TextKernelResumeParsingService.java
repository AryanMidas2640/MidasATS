package com.midas.consulting.service.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textkernel.tx.DataCenter;
import com.textkernel.tx.TxClient;
import com.textkernel.tx.exceptions.TxException;
import com.textkernel.tx.models.api.parsing.ParseOptions;
import com.textkernel.tx.models.api.parsing.ParseRequest;
import com.textkernel.tx.models.api.parsing.ParseResumeResponse;
import org.springframework.stereotype.Service;

@Service
public class TextKernelResumeParsingService {

    // Set APIURL as the REST API URL for resume parsing provided by RChilli.
    private static final String APIURL = "https://api.us.textkernel.com/tx/v10/parser/resume";
    // Set USERKEY as provided by RChilli.
    private static final String ACCID = "86995237";
    private static final String ServiceKey = "w8pP53576AAijV3qjCiSSnDUQFIgIM+dfC4Cdngw";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParseResumeResponse parseResume(String filePath) {
        ParseResumeResponse response= new ParseResumeResponse();
        try {
            TxClient client = new TxClient(ACCID,ServiceKey , DataCenter.US);
            com.textkernel.tx.models.Document doc = new  com.textkernel.tx.models.Document(filePath);
            ParseRequest request = new ParseRequest(doc, new ParseOptions());
            try {
                response = client.parseResume(request);
                //if we get here, it was 200-OK and all operations succeeded
                //now we can use the response from Textkernel to ouput some of the data from the resume
                System.out.println("Name: " + response.Value.ResumeData.ContactInformation.CandidateName.FormattedName);
                System.out.println("Experience: " + response.Value.ResumeData.EmploymentHistory.ExperienceSummary.Description);
            }
            catch (TxException e) {
                //this was an outright failure, always try/catch for TxExceptions when using the TxClient
                System.out.println("Error: " + e.TxErrorCode + ", Message: " + e.getMessage());
            }
//            return  resumeParserData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return response;
    }
}
