package com.example.load_balancer.balancer;

import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.ServerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.load_balancer.util.ServerStatus.UP;

@Component
public class RoundRobinSelector {

    @Autowired
    private InstanceManager instanceManager;

    private final AtomicInteger circularQueueIndex = new AtomicInteger(0);

    private List<String> roundRobinList = new ArrayList<>();

    private final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    private final Lock readLock = READ_WRITE_LOCK.readLock();

    private final Lock writeLock = READ_WRITE_LOCK.writeLock();

    public ServerInstance getNextServerInstance() {
        if (roundRobinList.isEmpty()) {
            return null;
        }
        try {
            readLock.lock();
            int startIndex = circularQueueIndex.get();
            int size = roundRobinList.size();

            for (int i = 0; i < size; i++) {
                int index = (startIndex + i) % size;
                Optional<ServerInstance> candidate =
                        instanceManager.getServerInstance(roundRobinList.get(index));

                if (candidate.isPresent() && UP.equals(candidate.get().getServerStatus())) {
                    circularQueueIndex.incrementAndGet();
                    return candidate.get();
                }
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }

    public void calculateRoundRobinList(){

        List<ServerInstance> serverInstanceList = instanceManager.getHealthyInstances();
        try{
            writeLock.lock();
            roundRobinList = serverInstanceList
                    .stream()
                    .flatMap(t-> Collections.nCopies(t.getWeight().get(),t.getUrl()).stream())
                    .toList();
            int size = roundRobinList.size();;
            circularQueueIndex.getAndSet(size==0 ? 0 : circularQueueIndex.get()% size);
        }finally {
            writeLock.unlock();
        }
    }
}
