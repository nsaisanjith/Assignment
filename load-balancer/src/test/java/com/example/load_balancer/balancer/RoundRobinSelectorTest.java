package com.example.load_balancer.balancer;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.util.ServerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class RoundRobinSelectorTest {

    private InstanceManager instanceManager;
    private RoundRobinSelector roundRobinSelector;

    @BeforeEach
    public void setup(){
        instanceManager = new InstanceManager();
        roundRobinSelector = new RoundRobinSelector();
        ReflectionTestUtils.setField(roundRobinSelector,"instanceManager",instanceManager);
    }

    @Test
    public void testEmptyList(){
        roundRobinSelector.calculateRoundRobinList();
        assertNull(roundRobinSelector.getNextServerInstance());
    }

    @Test
    public void testSingleHealthyInstance(){
        instanceManager.addInstance("A");
        ServerInstance s1 = instanceManager.getServerInstance("A").get();
        s1.setServerStatus(ServerStatus.UP);
        roundRobinSelector.calculateRoundRobinList();
        for(int i=0; i < 5; i++){
            assertEquals("A",roundRobinSelector.getNextServerInstance().getUrl());
        }
    }

    @Test
    public void testRoundRobinDistribution(){
        instanceManager.addInstance("A");
        instanceManager.addInstance("B");

        ServerInstance s1 = instanceManager.getServerInstance("A").get();
        ServerInstance s2 = instanceManager.getServerInstance("B").get();

        s1.setServerStatus(ServerStatus.UP);
        s2.setServerStatus(ServerStatus.UP);

        s1.setWeight(4);
        s2.setWeight(1);

        roundRobinSelector.calculateRoundRobinList();

        String[] expected = {"A","A","A","A","B"};

        for(int i=0; i < 5;i++){
            assertEquals(expected[i],roundRobinSelector.getNextServerInstance().getUrl());
        }
    }

    @Test
    public void testSkipUnhealthyInstance(){
        instanceManager.addInstance("A");
        instanceManager.addInstance("B");

        ServerInstance s1 = instanceManager.getServerInstance("A").get();
        ServerInstance s2 = instanceManager.getServerInstance("B").get();

        s1.setServerStatus(ServerStatus.DOWN);
        s2.setServerStatus(ServerStatus.UP);

        s1.setWeight(4);
        s2.setWeight(1);

        roundRobinSelector.calculateRoundRobinList();

        for(int i=0; i < 5;i++){
            assertEquals("B",roundRobinSelector.getNextServerInstance().getUrl());
        }
    }

    @Test
    public void testSkipInstanceWithDownStatus(){
        instanceManager.addInstance("A");
        instanceManager.addInstance("B");

        ServerInstance s1 = instanceManager.getServerInstance("A").get();
        ServerInstance s2 = instanceManager.getServerInstance("B").get();

        s1.setServerStatus(ServerStatus.UP);
        s2.setServerStatus(ServerStatus.UP);

        s1.setWeight(4);
        s2.setWeight(1);

        roundRobinSelector.calculateRoundRobinList();

        s1.setServerStatus(ServerStatus.DOWN);

        for(int i=0; i < 5;i++){
            assertEquals("B",roundRobinSelector.getNextServerInstance().getUrl());
        }
    }

}