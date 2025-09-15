package com.example.load_balancer.model;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public record RequestPayload(String targetSuffixUri, HttpMethod method, HttpEntity<String> entity) {}
