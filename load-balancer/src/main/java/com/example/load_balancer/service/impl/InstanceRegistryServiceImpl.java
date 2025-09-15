package com.example.load_balancer.service.impl;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.RegisterRequest;
import com.example.load_balancer.service.InstanceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceRegistryServiceImpl implements InstanceRegistryService {

    @Autowired
    private InstanceManager instanceManager;

    @Override
    public void registerInstance(RegisterRequest registerRequest) {

        instanceManager.addInstance(registerRequest.url());
    }
}
