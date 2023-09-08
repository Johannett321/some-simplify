package com.johansvartdal.ReactSpringTemplate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/")
    public ResponseEntity<?> testBackend() {
        return ResponseEntity.ok("Backend is working!");
    }

    @GetMapping("/authorization")
    public ResponseEntity<?> testAuthorization() {
        return ResponseEntity.ok("You are authorized!");
    }
}
