package com.example.load_balancer.service;

import com.example.load_balancer.model.RegisterRequest;

public interface InstanceRegistryService {

    void registerInstance(RegisterRequest registerRequest);
}
