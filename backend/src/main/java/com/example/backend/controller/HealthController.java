package com.example.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping
    public ResponseEntity<?> handleHealthRequest(){
        logger.info("Received health status request");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);

    }
}
