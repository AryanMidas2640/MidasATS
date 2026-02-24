package com.midas.consulting.model.candidate;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document( collection = "activities")
public class Activity {

    @Id
    private String id;
    @Indexed(unique = false, direction = IndexDirection.DESCENDING)
        private String sourceID;
    @Indexed(unique = false, direction = IndexDirection.DESCENDING)
    private Integer providerJobID;
    private String activityNote;
//    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String candidateID;
    @Indexed(unique = false, direction = IndexDirection.DESCENDING)
    private ActivityType
            activityType;
    private Date dateCreated;
    @DBRef
    private User userID;
}

