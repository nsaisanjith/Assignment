package com.example.load_balancer.model;

import com.example.load_balancer.util.ServerStatus;

import java.time.Instant;

public record StatusChange(ServerStatus prevStatus, ServerStatus newStatus, Instant  timestamp) {}
