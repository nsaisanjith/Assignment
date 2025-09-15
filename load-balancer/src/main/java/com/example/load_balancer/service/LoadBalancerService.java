package com.example.load_balancer.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface LoadBalancerService {

    ResponseEntity<?> forwardRequest(HttpServletRequest request, String body);

    void checkInstanceHealthStatus();
}
