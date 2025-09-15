package com.example.load_balancer.service.impl;

import com.example.load_balancer.balancer.RoundRobinSelector;
import com.example.load_balancer.manager.InstanceManager;
import com.example.load_balancer.model.ServerInstance;
import com.example.load_balancer.util.ServerStatus;
import com.example.load_balancer.util.exception.ForwardingFailedException;
import com.example.load_balancer.util.exception.NoHealthyInstanceException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoadBalancerServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RoundRobinSelector roundRobinSelector;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private InstanceManager instanceManager;

    @InjectMocks
    private LoadBalancerServiceImpl loadBalancerService;


    @Test
    public void testForwardRequestSuccess() {
        ServerInstance instance = new ServerInstance("A");
        when(roundRobinSelector.getNextServerInstance()).thenReturn(instance);

        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getRequestURI()).thenReturn("api");
        when(mockRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        ResponseEntity<String> fakeResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(fakeResponse);

        ResponseEntity<?> response = loadBalancerService.forwardRequest(mockRequest, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(contains("A"), any(), any(), eq(String.class));
    }

    @Test
    public void testForwardRequestNoHealthyInstance() {
        when(roundRobinSelector.getNextServerInstance()).thenReturn(null);

        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getRequestURI()).thenReturn("api");
        when(mockRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        assertThrows(NoHealthyInstanceException.class,
                () -> loadBalancerService.forwardRequest(mockRequest, null));
    }

    @Test
    public void testForwardRequestAllRetriesFail() {
        ServerInstance instance = new ServerInstance("A");
        when(roundRobinSelector.getNextServerInstance()).thenReturn(instance);
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getRequestURI()).thenReturn("api");
        when(mockRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("backend down"));

        assertThrows(ForwardingFailedException.class,
                () -> loadBalancerService.forwardRequest(mockRequest, null));
    }

    @Test
    public void testCheckInstanceHealthStatusAndMarkUp() {
        ServerInstance instance = new ServerInstance("A");
        instance.setServerStatus(ServerStatus.UNKNOWN);

        when(instanceManager.getServerInstances()).thenReturn(List.of(instance));
        ResponseEntity<String> okResponse = new ResponseEntity<>("healthy", HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(okResponse);

        loadBalancerService.checkInstanceHealthStatus();

        assertEquals(ServerStatus.UP, instance.getServerStatus());
    }

}