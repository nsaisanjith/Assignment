package com.example.load_balancer.service;

import com.example.load_balancer.model.ServerInstance;

import java.util.List;

public interface WeightAdjustmentService {

    boolean adjustAllWeights(List<ServerInstance> serverInstanceList);
}
