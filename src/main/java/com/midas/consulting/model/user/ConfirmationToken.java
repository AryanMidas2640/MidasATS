package com.midas.consulting.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "confirmationToken")
public class ConfirmationToken {
    @Id
    private String tokenId;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String confirmationToken;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
}
