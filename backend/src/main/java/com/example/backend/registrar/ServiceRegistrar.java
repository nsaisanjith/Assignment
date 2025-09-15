package com.example.backend.registrar;

import com.example.backend.DTO.RegisterRequestDTO;
import com.example.backend.controller.GameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServiceRegistrar implements ApplicationRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistrar.class);

    @Value("${loadbalancer.url}")
    private String loadBalancerUrl;

    @Value("${server.port}")
    private int port;

    @Override
    public void run(ApplicationArguments args) {
        String myUrl = "http://localhost:" + port;
        RegisterRequestDTO request = new RegisterRequestDTO(myUrl);

        try {
            restTemplate.postForEntity(
                    loadBalancerUrl,
                    request,
                    String.class
            );
            logger.info("Registered with Load Balancer at {}",loadBalancerUrl);
        } catch (Exception e) {
            logger.error("Failed to register with load balancer:{}", e.getMessage());
        }
    }
}