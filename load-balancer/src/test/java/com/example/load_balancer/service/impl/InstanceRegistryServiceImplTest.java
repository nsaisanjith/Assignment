package com.example.load_balancer.service.impl;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.RegisterRequest;
import com.example.load_balancer.service.InstanceRegistryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstanceRegistryServiceImplTest {

    @Mock
    private InstanceManager instanceManager;

    @InjectMocks
    private InstanceRegistryServiceImpl service;


    @Test
    public void testRegisterInstance(){

        RegisterRequest req = new RegisterRequest("A");
        service.registerInstance(req);
        verify(instanceManager).addInstance("A");

    }
}