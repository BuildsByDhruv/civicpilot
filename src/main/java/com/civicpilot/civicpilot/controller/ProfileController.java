package com.civicpilot.civicpilot.controller;

import com.civicpilot.civicpilot.dto.ProfileRequest;
import com.civicpilot.civicpilot.dto.ProfileResponse;
import com.civicpilot.civicpilot.model.UserProfile;
import com.civicpilot.civicpilot.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> create(@RequestBody ProfileRequest request) {
        UserProfile saved = service.create(request.answers());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProfileResponse(saved.getId()));
    }
}