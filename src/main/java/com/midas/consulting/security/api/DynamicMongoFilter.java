//package com.midas.consulting.security.api;
//
//import com.midas.consulting.config.database.MongoTemplateProvider;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.web.filter.OncePerRequestFilter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//public class DynamicMongoFilter extends OncePerRequestFilter {
//
//    @Autowired
//    public MongoTemplateProvider mongoTemplateProvider;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // Get the current MongoTemplate
//        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//        // Perform any MongoDB operations using the mongoTemplate
//        // Example: Check a condition or log something
//        System.out.println("Using MongoTemplate: " + mongoTemplate.toString());
//
//        // Continue with the request
//        filterChain.doFilter(request, response);
//    }
//}
