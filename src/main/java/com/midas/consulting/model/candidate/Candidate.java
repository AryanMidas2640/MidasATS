package com.midas.consulting.model.candidate;

import com.midas.consulting.controller.v1.response.microsoft.fileuploads.FileHandle;
import com.midas.consulting.model.hrms.ChangeLog;
import com.textkernel.tx.models.api.parsing.ParseResumeResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

//@Setter
//@NoArgsConstructor
//@Accessors(chain = true)
//@Getter
//@Document(collection = "candidate")
public class Candidate {
    @Id
    private String id;
//    private ResumeParserData resumeParserData;
    private ParseResumeResponse parseResumeResponse;
    private String status;
    @Indexed(unique = true)
    private String email;
    private String phone;
    private String name;
    private Date date_added;
    private Date last_updated;
    private FileHandle fileHandle;
    @DBRef
    private ChangeLog changeLog;
}