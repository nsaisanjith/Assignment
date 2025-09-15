package com.example.load_balancer.model;

import com.example.load_balancer.util.ServerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.load_balancer.util.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServerInstanceTest {

    private ServerInstance instance;

    @BeforeEach
    public void setup(){
        instance = new ServerInstance("A");
    }

    @Test
    public void testInitialState(){
        assertEquals((MAX_WEIGHT+MIN_WEIGHT)/2,instance.getWeight().get());
        assertEquals(ServerStatus.UNKNOWN,instance.getServerStatus());
        assertEquals(0,instance.getRetryCount().get());
    }

    @Test
    public void testComputeWeightWithNoLatency(){
        int before = instance.getWeight().get();
        assertEquals(before,instance.computeAdjustWeight());
    }

    @Test
    public void testIncrementCountServerMarkStatusDOWN(){
        for(int i=0;i < 15;i++){
            instance.incrementRetryCount();
        }
        assertEquals(ServerStatus.DOWN,instance.getServerStatus());
    }

    @Test
    public void testAddLatencyResetRetryCount(){
        for(int i=0; i < 6 ;i++){
            instance.incrementRetryCount();
        }
        instance.addLatency(1L);
        assertEquals(0,instance.getRetryCount().get());
    }

    @Test
    public void testSetServerStatusUnknowResetWeightAndRetry(){
        instance.setWeight(10);
        instance.incrementRetryCount();
        instance.setServerStatus(ServerStatus.UNKNOWN);
        assertEquals((MAX_WEIGHT+MIN_WEIGHT)/2,instance.getWeight().get());
        assertEquals(ServerStatus.UNKNOWN,instance.getServerStatus());
        assertEquals(0,instance.getRetryCount().get());
    }

    @Test
    public void testComputeAdjustWeightFastServerIncreaseWeight(){
        for(int i = 0; i < 20 ; i++){
            instance.addLatency(50L);
        }
        int newWeight = instance.computeAdjustWeight();
        assertTrue(newWeight > instance.getWeight().get());
    }

    @Test
    public void testComputeAdjustWeightSlowServerDecreaseWeight(){
        for(int i = 0; i < 20 ; i++){
            instance.addLatency(1000L);
        }
        int newWeight = instance.computeAdjustWeight();
        assertTrue(newWeight < instance.getWeight().get());
    }

}