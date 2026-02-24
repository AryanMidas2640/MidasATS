package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.RoutePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class RoutePermissionService {

  private MongoTemplateProvider multiTenantMongoTemplateFactory;
  @Autowired
public  RoutePermissionService(MongoTemplateProvider multiTenantMongoTemplateFactory) {
    this.multiTenantMongoTemplateFactory = multiTenantMongoTemplateFactory;
}


    private String getTenantIdFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Tenant");
        }
        throw new IllegalStateException("Tenant ID not found in the request header");
    }


    public RoutePermission saveRoutePermission(RoutePermission routePermission) {
        return multiTenantMongoTemplateFactory.getMongoTemplate().save(routePermission);
    }

    public List<RoutePermission> getAllRoutePermissions() {
        return multiTenantMongoTemplateFactory.getMongoTemplate().findAll(RoutePermission.class);
    }

    public RoutePermission getRoutePermissionById(String id) {
        return multiTenantMongoTemplateFactory.getMongoTemplate().findById(id, RoutePermission.class);
    }

    public RoutePermission updateRoutePermission(String id, RoutePermission routePermission) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();
        Query query = new Query(Criteria.where("id").is(id));

        if (mongoTemplate.exists(query, RoutePermission.class, "routes")) { // Specify the collection name "routes"
            routePermission.setId(id);
            return mongoTemplate.save(routePermission);
        }
        return null;
    }

    public void deleteRoutePermissionById(String id) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();
        Query query = new Query(Criteria.where("id").is(id));

        mongoTemplate.remove(query, RoutePermission.class, "routes"); // Specify the collection name "routes"
    }

}
