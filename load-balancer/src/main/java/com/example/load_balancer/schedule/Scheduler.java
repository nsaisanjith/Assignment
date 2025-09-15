package com.example.load_balancer.schedule;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.balancer.RoundRobinSelector;
import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.service.LoadBalancerService;
import com.example.load_balancer.service.WeightAdjustmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Scheduler {

    @Autowired
    private WeightAdjustmentService weightAdjustmentService;

    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private RoundRobinSelector roundRobinSelector;

    @Autowired
    private LoadBalancerService loadBalancerService;


    @Scheduled(fixedRate = 2000)
    public void checkHealthStatus(){

        logger.info("checking health status");
        loadBalancerService.checkInstanceHealthStatus();
    }

    @Scheduled(fixedRate = 5000)
    public void adjustInstanceWeights(){

        logger.info("calculating weighted average for each instance");
        List<ServerInstance> serverInstanceList = instanceManager.getServerInstances();
        boolean updated = weightAdjustmentService.adjustAllWeights(serverInstanceList);
        if(updated) {
            roundRobinSelector.calculateRoundRobinList();
        }
    }
}
