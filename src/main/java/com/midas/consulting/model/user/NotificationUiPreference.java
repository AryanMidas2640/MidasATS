

package com.midas.consulting.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "notificationUiPreference")
public class NotificationUiPreference {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private String city;
    private boolean isActive;
    private String state;
    private String zip;
    private String specialty;
    private String shift;
    private Float rate;
    private String facility;
    private String status;
}