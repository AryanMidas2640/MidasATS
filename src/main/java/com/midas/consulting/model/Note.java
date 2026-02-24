package com.midas.consulting.model;

import com.midas.consulting.model.user.User;
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
import java.util.List;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "notes")
public class Note {
    @Id
    private String id;
    @DBRef
    private User createdBy;
    private String noteType;
    private String note;
    private String candidateId;
    @Indexed(unique = false, direction = IndexDirection.ASCENDING)
    private String linkToId;
    private Date dateCreated;
    private List<Note> linkedNotes;
}
