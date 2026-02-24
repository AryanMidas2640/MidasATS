package com.midas.consulting.model.hrms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.midas.consulting.model.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "organisation")
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organisation {
    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String name;
    @DBRef
    @Lazy
    private User user;
    private String website;
}
