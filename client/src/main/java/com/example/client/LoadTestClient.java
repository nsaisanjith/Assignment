package com.example.client;

import com.example.client.DTO.GameRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class LoadTestClient {

    private static final Logger log = LoggerFactory.getLogger(LoadTestClient.class);

    private static final String BASE_URL = "http://localhost:8081";   // LB address
    private static final String TARGET_URI = "/api/game";
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_SEC = 10;
    private static final int WRITE_TIMEOUT_SEC = 10;
    private static final int RESPONSE_TIMEOUT_SEC = 5;
    private static final int RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(100);

    private final WebClient webClient;
    private final AtomicInteger counter = new AtomicInteger(0);

    public LoadTestClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SEC))
                .option(CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(READ_TIMEOUT_SEC, SECONDS))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(WRITE_TIMEOUT_SEC, SECONDS))
                );

        this.webClient = builder
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Scheduled(fixedRate = 100)
    public void sendRequest() {
        int requestId = counter.incrementAndGet();

        GameRequestDTO request = new GameRequestDTO(
                "Game-" + (requestId % 3),
                UUID.randomUUID().toString(),
                requestId * 10
        );

        webClient.post()
                .uri(TARGET_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> {
                                    log.info("Request {} -> status={} body={}",
                                            requestId, response.statusCode().value(), body);
                                    return body;
                                })
                )
                .retryWhen(
                        Retry.backoff(RETRY_ATTEMPTS, RETRY_BACKOFF)
                                .filter(ex -> ex instanceof PrematureCloseException)
                                .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                )
                .onErrorResume(PrematureCloseException.class, e -> {
                    log.warn("Request {} failed after retries: connection closed prematurely", requestId);
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Request {} failed with unexpected error: {}", requestId, e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
}