package com.midas.consulting.controller.v1.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.midas.consulting.model.CompanyDivision;
import com.midas.consulting.service.CompanyDivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/divisions")
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")

public class CompanyDivisionController {
    private final CompanyDivisionService companyDivisionService;

    @Autowired
    public CompanyDivisionController(CompanyDivisionService companyDivisionService) {
        this.companyDivisionService = companyDivisionService;
    }
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @PostMapping
    public ResponseEntity<CompanyDivision> createDivision(@RequestBody CompanyDivision division) {
        return ResponseEntity.ok(companyDivisionService.createDivision(division));
    }
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @GetMapping
    public ResponseEntity<List<CompanyDivision>> getAllDivisions() {
        return ResponseEntity.ok(companyDivisionService.getAllDivisions());
    }
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDivision> getDivisionById(@PathVariable String id) {
        return companyDivisionService.getDivisionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDivision> updateDivision(@PathVariable String id, @RequestBody CompanyDivision division) {
        return ResponseEntity.ok(companyDivisionService.updateDivision(id, division));
    }
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDivision(@PathVariable String id) {
        companyDivisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }
}
