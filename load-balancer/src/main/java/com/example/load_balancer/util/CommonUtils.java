package com.example.load_balancer.util;

import com.example.load_balancer.model.RequestPayload;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.Optional;

public class CommonUtils {

    public static RequestPayload getRequestPayload(HttpServletRequest request, String body){

        String method = request.getMethod();
        String uri = request.getRequestURI();;
        String query = request.getQueryString();

        String targetSuffixUri = uri + Optional.ofNullable(query)
                .map(q-> "?"+q)
                .orElse("");

        HttpHeaders httpHeaders = Collections.list(request.getHeaderNames())
                .stream()
                .collect(HttpHeaders::new,
                        (map,name)->map.add(name,request.getHeader(name)),
                        HttpHeaders::putAll
                );

        HttpEntity<String> entity = new HttpEntity<>(body,httpHeaders);
        return new RequestPayload(targetSuffixUri, HttpMethod.valueOf(method),entity);

    }
}
