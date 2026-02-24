package com.midas.consulting.controller.v1.api;

import com.midas.consulting.service.candidate.TextExtractionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/extract")
public class ExtractionController {

    @Autowired
    private  TextExtractionService textExtractionService;



//    @GetMapping("/extractDetailsNamesBlank")
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public List<String> extractDetailsNamesBlank() {
//        return textExtractionService.extractDetailsPhoneBlank();
//    }


    @GetMapping("/extractDetailsPhoneBlank")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<String> extractDetailsPhoneBlank() {
        return textExtractionService.extractDetailsPhoneBlank();
    }
}
