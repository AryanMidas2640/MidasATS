package com.midas.consulting.controller.v1.response.candidate;

import com.midas.consulting.controller.v1.response.microsoft.fileuploads.FileHandle;
import com.midas.consulting.model.candidate.parsed.ResumeParserData;
import com.textkernel.tx.models.api.parsing.ParseResumeResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class CandidateResponse {
    private String id;
    private ResumeParserData resumeParserData;
    private ParseResumeResponse parseResumeResponse;
    private String email;
    private String phone;
    private String status;
    private Date date_added;
    private Date last_updated;
    private FileHandle fileHandle;
}