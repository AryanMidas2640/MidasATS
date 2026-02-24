package com.midas.consulting.model.hrms;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
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
@Document(collection = "employeeDocs")
public class EmployeeDocs {
    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String type;
    @DBRef
    private Employee employee;
    private  Date expiryDate;
    private Object docStructure;

    private String docDesc;
    @Indexed(unique = false, direction = IndexDirection.DESCENDING)
    private String docName;

    @DBRef
    private User user;
}
