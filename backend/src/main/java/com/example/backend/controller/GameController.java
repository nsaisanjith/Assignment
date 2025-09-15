package com.example.backend.controller;

import com.example.backend.DTO.GameRequestDTO;
import com.example.backend.DTO.GameResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
@RequestMapping("/api/game")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final AtomicInteger counter = new AtomicInteger();

    @Value("${server.port}")
    private String port;

    @PostMapping
    public ResponseEntity<GameResponseDTO> handleGameRequest(@RequestBody GameRequestDTO request){
        logger.info("Received game request Id: {}",request.gamerId());
        String instance = "Instance-"+port;
        int delay;
        if("8043".equals(port)){
            delay = ThreadLocalRandom.current().nextInt(50,180);
        }else if("8044".equals(port)){
            delay = ThreadLocalRandom.current().nextInt(50,500);
        }else{
            delay = ThreadLocalRandom.current().nextInt(600,1000);
        }
        try{
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        GameResponseDTO gameResponseDTO = new GameResponseDTO(
                request.game(),
                request.gamerId(),
                request.points(),
                instance
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gameResponseDTO);

    }
}
