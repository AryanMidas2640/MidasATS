package com.midas.consulting.model.hrms;

import com.midas.consulting.model.user.User;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Time;
import java.util.Date;

@Setter
@Accessors(chain = true)
@Document(collection = "timeSheet")
public class TimeSheet {
    @Id
    private String id;
    private Time startTime;
    private Integer hours;
    @DBRef
    @Lazy
    private User user;
    private Float breakHours;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private Date day;
    private Float holidayHours;
    private Float overTimeHours;
    private Float regularHours;

}
