package com.midas.consulting.model.hrms;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Setter@Getter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "changeLog")
public class ChangeLog {
    @Id
    private  String id;
    private Object oldObject;
    @DBRef
    private User user;
    @Indexed
    private Date changeDateTime;
private String userNotes;
}
