package com.example.load_balancer.service.impl;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.RequestPayload;
import com.example.load_balancer.balancer.RoundRobinSelector;
import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.service.LoadBalancerService;
import com.example.load_balancer.util.FieldUpdater;
import com.example.load_balancer.util.ServerStatus;
import com.example.load_balancer.util.exception.ForwardingFailedException;
import com.example.load_balancer.util.exception.NoHealthyInstanceException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.example.load_balancer.util.CommonConstants.*;
import static com.example.load_balancer.util.CommonUtils.getRequestPayload;

@Service
public class LoadBalancerServiceImpl implements LoadBalancerService {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancerServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RoundRobinSelector roundRobinSelector;

    @Autowired
    private InstanceManager instanceManager;

    @Override
    public ResponseEntity<?> forwardRequest(HttpServletRequest request, String body) {

        Exception lastException = null;
        RequestPayload requestPayload = getRequestPayload(request, body);

        int maxTriesPerInstance = MAX_ATTEMPTS;

        long lbStartTime = System.nanoTime();

        for (int instanceAttempt = 0; instanceAttempt < MAX_ATTEMPTS_FOR_INSTANCE; instanceAttempt++) {
            ServerInstance serverInstance = roundRobinSelector.getNextServerInstance();
            if (serverInstance == null) {
                throw new NoHealthyInstanceException("No healthy instance available");
            }

            for (int attempt = 1; attempt <= maxTriesPerInstance; attempt++) {
                try {
                    StopWatch watch = new StopWatch();
                    watch.start();
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            serverInstance.getUrl() + requestPayload.targetSuffixUri(),
                            requestPayload.method(),
                            requestPayload.entity(),
                            String.class
                    );
                    watch.stop();
                    serverInstance.addLatency(watch.getTotalTimeMillis());
                    long lbTotalTime = System.nanoTime()-lbStartTime;
                    long lbOverHead = lbTotalTime- watch.getTotalTimeNanos();

                    if(lbOverHead < 0){
                        lbOverHead=0;
                    }

                    log.info("Request Id={} -> {} status={} [attempt {}|{}]" +
                                    "took {}ms [LB overhead={}ms] | LB total={}ms",
                            request.getRequestId(),
                            serverInstance.getUrl(),
                            responseEntity.getStatusCode().value(),
                            attempt,
                            MAX_ATTEMPTS,
                            TimeUnit.NANOSECONDS.toMillis(watch.getTotalTimeNanos()),
                            TimeUnit.NANOSECONDS.toMillis(lbOverHead),
                            TimeUnit.NANOSECONDS.toMillis(lbTotalTime));

                    return responseEntity;
                } catch (Exception e) {
                    lastException = e;
                    serverInstance.incrementRetryCount();

                    log.warn("Attempt {} failed on instance {}: {}", attempt, serverInstance.getUrl(), e.getMessage());

                    if (attempt == maxTriesPerInstance) {
                        log.error("Exhausted {} retries for instance {}", maxTriesPerInstance, serverInstance.getUrl());
                        break;
                    }

                    long sleep = MAX_TOTAL_DELAY/MAX_ATTEMPTS
                            + ThreadLocalRandom.current().nextLong(50);
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleep);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ex);
                    }
                }
            }
        }

        throw new ForwardingFailedException("All retry attempts exhausted across all instances", lastException);
    }

    @Override
    public void checkInstanceHealthStatus() {

        List<ServerInstance> serverInstanceList = instanceManager.getServerInstances();
        FieldUpdater<ServerStatus> fieldUpdater = new FieldUpdater<>();
        serverInstanceList.forEach(
                serverInstance -> {
                    ServerStatus newStatus;
                    ServerStatus oldStatus = serverInstance.getServerStatus();
                   try{
                       ResponseEntity<String> responseEntity = restTemplate.getForEntity(serverInstance.getUrl()+HEALTH_CHECK_URI_SUFFIX, String.class);
                       if(responseEntity.getStatusCode().is2xxSuccessful()){
                           newStatus = ServerStatus.UP;
                       }else{
                           newStatus = ServerStatus.DOWN;
                       }

                   }catch(Exception e){
                       log.error("ERROR encountered while checking health for server instance {}",serverInstance.getUrl());
                       serverInstance.incrementRetryCount();
                       newStatus = serverInstance.getServerStatus();
                   }
                    fieldUpdater
                            .updateIfChanged(oldStatus,
                                    newStatus,
                                    (prevStatus, currStatus)->{
                                        log.info("Instance {} status updated from {} to {} at:{}", serverInstance.getUrl(), prevStatus ,currStatus, Instant.now());
                                        serverInstance.setServerStatus(currStatus);
                                    });
                }
        );
        if(fieldUpdater.getUpdateCount() > 0){
            roundRobinSelector.calculateRoundRobinList();
        }
    }

}
