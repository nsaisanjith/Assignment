package com.example.load_balancer.manager;

import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.util.ServerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class InstanceManagerTest {

    private InstanceManager instanceManager;

    @BeforeEach
    public void setup(){
        instanceManager = new InstanceManager();
        instanceManager.clearInstances();
    }

    @Test
    public void testGetWithNoServerInstance(){
        assertFalse(instanceManager.getServerInstance("A").isPresent());
    }

    @Test
    public void testGetAllInstances(){
        instanceManager.addInstance("F");
        instanceManager.addInstance("G");

        List<ServerInstance> serverInstanceList = instanceManager.getServerInstances();
        assertEquals(2,serverInstanceList.size());
    }

    @Test
    public void testAddNewInstance(){
        instanceManager.addInstance("A");

        Optional<ServerInstance> serverInstance = instanceManager.getServerInstance("A");

        assertTrue(serverInstance.isPresent());
        assertEquals(ServerStatus.UNKNOWN,serverInstance.get().getServerStatus());
    }

    @Test
    public void testAddExistingInstanceResetStatus(){
        instanceManager.addInstance("B");
        ServerInstance serverInstance = instanceManager.getServerInstance("B").get();

        serverInstance.setServerStatus(ServerStatus.UP);

        instanceManager.addInstance("B");
        assertEquals(ServerStatus.UNKNOWN,serverInstance.getServerStatus());
    }

    @Test
    public void testGetHealthyInstances(){
        instanceManager.addInstance("C");
        instanceManager.addInstance("D");

        ServerInstance s1 = instanceManager.getServerInstance("C").get();
        ServerInstance s2 = instanceManager.getServerInstance("D").get();

        s1.setServerStatus(ServerStatus.UP);
        s2.setServerStatus(ServerStatus.DOWN);
        List<ServerInstance> serverInstanceList = instanceManager.getHealthyInstances();
        assertEquals(1,serverInstanceList.size());
        assertEquals("C",serverInstanceList.getFirst().getUrl());
    }

    @Test
    public void testGetServerInstance(){
        instanceManager.addInstance("E");
        Optional<ServerInstance> serverInstance = instanceManager.getServerInstance("E");

        assertTrue(serverInstance.isPresent());
        assertEquals("E",serverInstance.get().getUrl());
    }


}