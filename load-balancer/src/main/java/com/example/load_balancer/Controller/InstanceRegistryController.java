package com.example.load_balancer.Controller;

import com.example.load_balancer.model.RegisterRequest;
import com.example.load_balancer.service.InstanceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registry")
public class InstanceRegistryController {

    private static final Logger log = LoggerFactory.getLogger(InstanceRegistryController.class);

    @Autowired
    private InstanceRegistryService instanceRegistryService;

    @PostMapping("/register")
    public ResponseEntity<?> registerInstance(@RequestBody RegisterRequest registerRequest){
        log.info("registering instance url:{}",registerRequest.url());
        instanceRegistryService.registerInstance(registerRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Instance registered");
    }
}
