package com.example.load_balancer.schedule;

import com.example.load_balancer.balancer.RoundRobinSelector;
import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.service.LoadBalancerService;
import com.example.load_balancer.service.WeightAdjustmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {

    @Mock
    private WeightAdjustmentService weightAdjustmentService;

    @Mock
    private LoadBalancerService loadBalancerService;

    @InjectMocks
    private Scheduler scheduler;

    @Mock
    private RoundRobinSelector roundRobinSelector;

    @Mock
    private InstanceManager instanceManager;

    @Test
    public void testCheckHealStatus(){
        scheduler.checkHealthStatus();
        verify(loadBalancerService).checkInstanceHealthStatus();
    }

    @Test
    public void testAdjustWeightsWhenUpdated(){
        ServerInstance s1 = new ServerInstance("A");

        when(instanceManager.getServerInstances()).thenReturn(List.of(s1));
        when(weightAdjustmentService.adjustAllWeights(Mockito.any())).thenReturn(true);

        scheduler.adjustInstanceWeights();

        verify(weightAdjustmentService).adjustAllWeights(any());
        verify(roundRobinSelector).calculateRoundRobinList();

    }

    @Test
    public void testAdjustWeightsWhenNotUpdated(){
        when(instanceManager.getServerInstances()).thenReturn(List.of());
        when(weightAdjustmentService.adjustAllWeights(Mockito.any())).thenReturn(false);

        scheduler.adjustInstanceWeights();

        verify(weightAdjustmentService).adjustAllWeights(any());
        verify(roundRobinSelector,never()).calculateRoundRobinList();
    }


}