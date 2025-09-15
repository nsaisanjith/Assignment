package com.example.load_balancer.manager;

import com.example.load_balancer.model.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.load_balancer.util.ServerStatus.UNKNOWN;
import static com.example.load_balancer.util.ServerStatus.UP;

@Component
public class InstanceManager {

    private final Map<String, ServerInstance> serverInstanceMap = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(InstanceManager.class);


    public void addInstance(String uri){

        ServerInstance existing = serverInstanceMap.putIfAbsent(uri, new ServerInstance(uri));
        if (existing != null) {
            existing.setServerStatus(UNKNOWN);
            log.info("Instance {} already registered, status reset to UNKNOWN",uri);
        }else{
            log.info("Registered new Instance:{}",uri);
        }
    }

    public List<ServerInstance> getHealthyInstances(){
        return serverInstanceMap
                .values()
                .stream()
                .filter(t->UP.equals(t.getServerStatus()))
                .toList();
    }

    public Optional<ServerInstance> getServerInstance(String key){

        return Optional.ofNullable(serverInstanceMap.getOrDefault(key,null));
    }

    public List<ServerInstance> getServerInstances(){
        return serverInstanceMap
                .values()
                .stream()
                .toList();
    }

    // Note:this is only for testing purpose
    public void clearInstances() {
        serverInstanceMap.clear();
    }

}
