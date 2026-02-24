

package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.user.LoginStats;
import com.midas.consulting.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginStatsService {

    private final MongoTemplateProvider multiTenantMongoTemplateFactory;
    private HttpServletRequest request;

    @Autowired
    public LoginStatsService(HttpServletRequest request,MongoTemplateProvider multiTenantMongoTemplateFactory) {
        this.multiTenantMongoTemplateFactory = multiTenantMongoTemplateFactory;
        this.request=request;
    }

    public List<LoginStats> getAllLoginStats() {
//        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate().createMongoTemplate(getTenantIdFromHeader());
        return multiTenantMongoTemplateFactory.getMongoTemplate().findAll(LoginStats.class);
    }
    public Map<String, Object> getAllLoginStats(int page, int size, String sortField, String sortDirection) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();

        Query query = new Query();
        query.skip(page * size);
        query.limit(size);

        // Add sorting
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        query.with(Sort.by(direction, sortField));

        List<LoginStats> loginStats = mongoTemplate.find(query, LoginStats.class);
        long totalCount = mongoTemplate.count(new Query(), LoginStats.class);

        Map<String, Object> response = new HashMap<>();
        response.put("content", loginStats);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalItems", totalCount);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));

        return response;
    }

    public Map<String, Object> getLoginStatsBwDates(
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortField,
            String sortDirection
    ) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();

        Query query = new Query();
        query.addCriteria(Criteria.where("loginTime")
                .gte(startDate.atStartOfDay())
                .lte(endDate.atTime(23, 59, 59)));
        query.skip(page * size);
        query.limit(size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        query.with(Sort.by(direction, sortField));

        List<LoginStats> loginStats = mongoTemplate.find(query, LoginStats.class);

        // Count query - remove pagination for accurate total
        Query countQuery = new Query();
        countQuery.addCriteria(Criteria.where("loginTime")
                .gte(startDate.atStartOfDay())
                .lte(endDate.atTime(23, 59, 59)));
        long totalCount = mongoTemplate.count(countQuery, LoginStats.class);

        Map<String, Object> response = new HashMap<>();
        response.put("content", loginStats);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalItems", totalCount);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));

        return response;
    }


    public Page<LoginStats> getAllLoginStats(Pageable pageable) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();

        Query query = new Query();
        query.with(pageable);

        List<LoginStats> loginStats = mongoTemplate.find(query, LoginStats.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), LoginStats.class);

        return new PageImpl<>(loginStats, pageable, count);
    }

    public Page<LoginStats> getLoginStatsBwDates(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate();

        Query query = new Query();
        query.addCriteria(Criteria.where("loginTime")
                .gte(startDate.atStartOfDay())
                .lte(endDate.atTime(23, 59, 59)));
        query.with(pageable);

        List<LoginStats> loginStats = mongoTemplate.find(query, LoginStats.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), LoginStats.class);

        return new PageImpl<>(loginStats, pageable, count);
    }


    public LoginStats getLoginStatsById(String id) {
//        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate().createMongoTemplate(getTenantIdFromHeader());
        Query query = new Query(Criteria.where("_id").is(id));
        return multiTenantMongoTemplateFactory.getMongoTemplate().findOne(query, LoginStats.class);
    }

    public List<LoginStats> getLoginStatsDate(Date startDate, Date endDate) {
//        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate().createMongoTemplate(getTenantIdFromHeader());
        Query query = new Query(Criteria.where("loginTime").gte(startDate).lte(endDate));
        return multiTenantMongoTemplateFactory.getMongoTemplate().find(query, LoginStats.class);
    }

    public LoginStats saveLoginStats(String userId, String ipInfo) throws Exception {
//        MongoTemplate mongoTemplate = multiTenantMongoTemplateFactory.getMongoTemplate().createMongoTemplate(getTenantIdFromHeader());

        // Fetch the User by userId
        Query userQuery = new Query(Criteria.where("_id").is(userId));
        User user = multiTenantMongoTemplateFactory.getMongoTemplate().findOne(userQuery, User.class);

        if (user == null) {
            throw new MidasCustomException.DuplicateEntityException(
                    "Could not save user login event due to the user not being found");
        }

        // Create and save the LoginStats record
        LoginStats loginStats = new LoginStats()
                .setLoginTime(new Date())
                .setIpAddress(ipInfo)
                .setUser(user);

        return multiTenantMongoTemplateFactory.getMongoTemplate().save(loginStats);
    }

 

    private String getTenantIdFromHeader() {
        // Implement this method to extract the tenant ID from the request header
        Example: return request.getHeader("X-Tenant");
//        return "your-tenant-id"; // Replace with actual logic to get tenant ID
    }
}




//package com.midas.consulting.service.hrms;





//
//import com.midas.consulting.config.database.MultiTenantMongoTemplateFactory;
//import com.midas.consulting.exception.MidasCustomException;
//import com.midas.consulting.model.user.LoginStats;
//import com.midas.consulting.model.user.User;
//import com.midas.consulting.repository.user.LoginStatsRepository;
//import com.midas.consulting.repository.user.UserRepository;
//import com.midas.consulting.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class LoginStatsService {
//
//
////    private LoginStatsRepository loginStatsRepository;
////
////
////    private UserRepository userRepository;
//
//    private UserService userService;
//private  MultiTenantMongoTemplateFactory multiTenantMongoTemplateFactory;
//
//    @Autowired
//    public LoginStatsService(UserService userService, MultiTenantMongoTemplateFactory multiTenantMongoTemplateFactory) {
//        this.userService = userService;
//        this.multiTenantMongoTemplateFactory=multiTenantMongoTemplateFactory;
//    }
//
//    public List<LoginStats> getAllLoginStats() {
//        return loginStatsRepository.findAll();
//    }
//
//    public Optional<LoginStats> getLoginStatsById(String id) {
//        return loginStatsRepository.findById(id);
//    }
//    public List<LoginStats> getLoginStatsDate(Date startDate, Date endDate) {
//        return loginStatsRepository.findLoginBetween(startDate,endDate);
//    }
//
//    public LoginStats saveLoginStats(String userId , String ipInfo) throws Exception {
//        Optional<User> employee = userRepository.findById(userId);
//        if (employee.isPresent()) {
//            User user = employee.get();
//            LoginStats loginStats = new LoginStats().setLoginTime(new Date()).setIpAddress(ipInfo).setUser(user);
//            return loginStatsRepository.save(loginStats);
//        }
//        throw new MidasCustomException.DuplicateEntityException("Could not save user login event due to the user was not found ");
//    }
//
////    public Employee updateDocument(CreateEmployeeDocumentRequest createEmployeeDocumentRequest, UserDto userDto) throws Exception {
////        Optional<EmployeeDocs> employeeDocs = employeeDocumentRepository.findById(createEmployeeDocumentRequest.getId());
////        Optional<Employee> employee = employeeRepository.findById(createEmployeeDocumentRequest.getEmployeeId());
////        if (employeeDocs.isPresent() && employee.isPresent()) {
////            EmployeeDocs employeeToBeBound = employeeDocs.get();
////            employeeToBeBound.setEmployee(employee.get())
////                    .setUser(UserMapper.toUser(userDto))
////                    .setType(createEmployeeDocumentRequest.getDocType())
////                    .setExpiryDate(createEmployeeDocumentRequest.getExpiryDate())
////                    .setDocStructure(createEmployeeDocumentRequest.getUploadDocStructure());
////            employeeDocumentRepository.save(employeeToBeBound);
////        }
////        throw new MidasCustomException.DuplicateEntityException("Could not save the employee document");
////    }
////
////    public void deleteDocument(String id) {
////        employeeDocumentRepository.deleteById(id);
////    }
//
//
//}