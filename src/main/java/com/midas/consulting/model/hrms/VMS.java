package com.midas.consulting.model.hrms;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;import org.springframework.data.annotation.Id;

import java.util.Set;
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "vms")
public class VMS {
    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String name;
    private String url;
    @DBRef
    @Lazy
    private User user;
}