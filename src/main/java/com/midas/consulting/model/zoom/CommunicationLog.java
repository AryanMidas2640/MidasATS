package com.midas.consulting.model.zoom;

import com.midas.consulting.model.user.Role;
import com.midas.consulting.model.user.User;
import com.midas.consulting.model.user.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "communicationLog")
public class CommunicationLog {
        @Id
        private String id;
        @Indexed(direction = IndexDirection.ASCENDING)
        private String userId;
        @Indexed(direction = IndexDirection.ASCENDING)
        private String systemMessage;
        private Date dateCreated;
        private Date dateModified;
}
