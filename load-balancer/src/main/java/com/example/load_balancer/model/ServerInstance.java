package com.example.load_balancer.model;

import com.example.load_balancer.util.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.load_balancer.util.CommonConstants.*;
import static com.example.load_balancer.util.ServerStatus.*;

public class ServerInstance {

    private static final Logger log = LoggerFactory.getLogger(ServerInstance.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final String url;

    private final AtomicInteger weight;

    private volatile ServerStatus serverStatus;

    private final Deque<Long> latencies;

    private final AtomicInteger retryCount;

    public ServerInstance(String url) {
        this.url = url;
        weight = new AtomicInteger((MAX_WEIGHT+MIN_WEIGHT)/2);
        serverStatus = UNKNOWN;
        latencies = new ArrayDeque<>();
        retryCount = new AtomicInteger(0);
    }

    public AtomicInteger getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight.set(weight);
    }

    public int computeAdjustWeight(){
        List<Long> sortedList;
        try {
            lock.lock();
            sortedList = new ArrayList<>(this.latencies);
        }finally {
            lock.unlock();
        }
        int size = sortedList.size();
        if(size==0){
            return weight.get();
        }
        Collections.sort(sortedList);
        long p95 = sortedList.get((int) (Math.ceil(size*0.95)-1));
        p95 = Math.max(1L,p95);

        double scale = (double) ALLOWED_RESPONSE_LATENCY / (double) p95;

        double desiredDouble = MIN_WEIGHT + (MAX_WEIGHT - MIN_WEIGHT) * Math.min(1.0,scale);

        int desired = (int) Math.round(desiredDouble);

        desired = Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, desired));

        double smoothed = weight.get() * (1.0 - EWMA_ALPHA) + desired * EWMA_ALPHA;
        int newWeight = (int) Math.round(smoothed);

        int delta = newWeight - weight.get();
        if (delta > MAX_DELTA_PER_RUN) delta = MAX_DELTA_PER_RUN;
        if (delta < -MAX_DELTA_PER_RUN) delta = -MAX_DELTA_PER_RUN;

        newWeight = weight.get() + delta;
        newWeight = Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, newWeight));

        return newWeight;
    }

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        if(UNKNOWN.equals(serverStatus)){
            retryCount.set(0);
            setWeight((MAX_WEIGHT+MIN_WEIGHT)/2);
            log.info("Instance {} reset to UNKNOWN with mid weight {}",url,weight.get());
        }
        this.serverStatus = serverStatus;
    }

    public AtomicInteger getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        int current = retryCount.incrementAndGet();
        if (current == MAX_RETRY_COUNT) {
            setServerStatus(DOWN);
            log.warn("Instance {} marked down after {} retries", url, current);
        }
    }

    public void addLatency(Long latency){
        /*
         This is to ensure if the previous requests had failed and incremented retry count,
         set it back to zero as the current request was successful
         */
        retryCount.set(0);
        try {
            lock.lock();
            if (latencies.size() == MAX_CAPACITY) {
                latencies.pollFirst();
            }
            latencies.add(latency);
        }finally {
            lock.unlock();
        }
    }

    public String getUrl() {
        return url;
    }

}
