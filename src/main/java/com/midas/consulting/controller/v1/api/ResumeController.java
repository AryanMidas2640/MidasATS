package com.midas.consulting.controller.v1.api;

import com.midas.consulting.dto.ResumeMatchResponse;
import com.midas.consulting.service.parsing.ResumeMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@Service
public class ResumeController {

    private final ResumeMatchingService matchingService;

    @PostMapping("/match")
    public ResponseEntity<ResumeMatchResponse> matchResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("requirement") String requirement) {

        return ResponseEntity.ok(
                matchingService.matchResume(file, requirement)
        );
    }
}
