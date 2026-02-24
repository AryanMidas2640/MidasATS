package com.midas.consulting.service.candidate;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.candidate.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final MongoTemplateProvider mongoTemplate;
@Autowired
    public LocationService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Location> findLocationsNear(double longitude, double latitude, double radiusInMiles) {
        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInMiles, Metrics.MILES);

        Query query = new Query();
        query.addCriteria(Criteria.where("location").near(point).maxDistance(distance.getNormalizedValue()));

        return mongoTemplate.getMongoTemplate().find(query, Location.class);
    }

    public List<Location> findLocationsByZipOrStateAndRadius(String zipOrState, double radiusInMiles) {
        Point coordinates = getCoordinatesByZipOrState(zipOrState);
        if (coordinates == null) {
            throw new IllegalArgumentException("Invalid ZIP code or state provided.");
        }

        // Convert miles to meters (MongoDB uses meters for geospatial queries)
        double radiusInMeters = radiusInMiles * 1609.34;

        // Create the query with $nearSphere criteria
        Query query = new Query();
        query.addCriteria(Criteria.where("location.coordinates").nearSphere(coordinates).maxDistance(radiusInMeters));

        // Execute the query using MongoTemplate
        List<Location>     l= mongoTemplate.getMongoTemplate().find(query, Location.class);
       return  l;
    }

    public List<Location> getLocationsNearby(double longitude, double latitude, double maxDistanceInMeters) {
        GeoJsonPoint geoJsonPoint = new GeoJsonPoint(longitude, latitude);

        Query query = new Query();
        query.addCriteria(Criteria.where("location").nearSphere(geoJsonPoint).maxDistance(maxDistanceInMeters / 6371.0)); // 6371 is the Earth radius in km

        return mongoTemplate.getMongoTemplate().find(query, Location.class, "locationsTree");
    }
    private Point getCoordinatesByZipOrState(String zipOrState) {
        Query query = new Query();

        if (zipOrState.matches("\\d{5}")) {
            query.addCriteria(Criteria.where("zipCode").is(zipOrState));
        } else {
            query.addCriteria(Criteria.where("city").is(zipOrState));
        }

        Location location = mongoTemplate.getMongoTemplate().findOne(query, Location.class);

        return location != null ?location.getLocation() : null;
    }
}
