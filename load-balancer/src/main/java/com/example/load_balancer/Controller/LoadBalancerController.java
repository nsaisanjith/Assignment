package com.example.load_balancer.Controller;

import com.example.load_balancer.service.LoadBalancerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadBalancerController {

    @Autowired
    private LoadBalancerService loadBalancerService;

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class);

    @RequestMapping("/**")
    public ResponseEntity<?> forwardRequest(HttpServletRequest request,@RequestBody(required = false) String body){
        logger.info("Request received at load balancer forwarding the request id: {}", request.getRequestId());
        return loadBalancerService.forwardRequest(request,body);
    }

}
