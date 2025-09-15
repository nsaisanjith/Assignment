package com.example.load_balancer.service.impl;

import com.example.load_balancer.model.ServerInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeightAdjustmentServiceImplTest {

    private WeightAdjustmentServiceImpl weightAdjustmentService;

    @Mock
    private ServerInstance server;

    @BeforeEach
    public void setUp() {
        weightAdjustmentService = new WeightAdjustmentServiceImpl();
    }

    @Test
    public void testNoWeightChange() {
        when(server.getWeight()).thenReturn(new AtomicInteger(5));
        when(server.computeAdjustWeight()).thenReturn(5);

        boolean updated = weightAdjustmentService.adjustAllWeights(List.of(server));

        assertFalse(updated);
        verify(server, never()).setWeight(anyInt());
    }

    @Test
    public void testWeightChange() {
        when(server.getWeight()).thenReturn(new AtomicInteger(5));
        when(server.computeAdjustWeight()).thenReturn(7);

        boolean updated = weightAdjustmentService.adjustAllWeights(List.of(server));

        assertTrue(updated);
        verify(server).setWeight(7);
    }

}