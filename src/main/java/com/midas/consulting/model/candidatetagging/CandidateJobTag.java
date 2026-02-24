package com.midas.consulting.model.candidatetagging;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "candidateJobTags")
public class CandidateJobTag {

    @Id
    private String id;
    private String candidateId;
    private String jobId;
    @DBRef
    private User taggedBy;
    private LocalDateTime dateTime;
    private String tagStatus;

    public CandidateJobTag(String id, String candidateId, String jobId, String s, LocalDateTime dateTime, String tagStatus) {
    }
}