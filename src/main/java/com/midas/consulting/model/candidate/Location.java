package com.midas.consulting.model.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter

@Document(collection = "locations")
public class Location {

    @Id
    private String id;

    private String city;
    private String state;
    private GeoJsonPoint location;  // This is where GeoJsonPoint is mapped


}
