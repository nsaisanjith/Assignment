package com.example.load_balancer.service.impl;

import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.service.WeightAdjustmentService;
import com.example.load_balancer.util.FieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WeightAdjustmentServiceImpl implements WeightAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(WeightAdjustmentServiceImpl.class);

    @Override
    public boolean adjustAllWeights(List<ServerInstance> serverInstanceList) {

        FieldUpdater<Integer> fieldUpdater = new FieldUpdater<>();
        serverInstanceList.forEach(serverInstance -> {
            int oldWeight = serverInstance.getWeight().get();
            int newWeight;
            try{
                newWeight = serverInstance.computeAdjustWeight();
            } catch (Exception e) {
                log.warn("Unabled to adjust weight for instance:{}",serverInstance.getUrl());
                newWeight = serverInstance.getWeight().get();
            }
            log.debug("Instance {} new weight {}",serverInstance.getUrl(),newWeight);
            fieldUpdater.updateIfChanged(oldWeight
                    ,newWeight
                    ,(prevWeight,currWeight)->{
                        log.info("Instance {} weight updated from {} to {} at:{}", serverInstance.getUrl(), prevWeight ,currWeight, Instant.now());
                        serverInstance.setWeight(currWeight);
                    });
        });
        log.debug("Weight adjustment cycle completed. {} updates applied.",
                fieldUpdater.getUpdateCount());

        return fieldUpdater.getUpdateCount() > 0;
    }
}
