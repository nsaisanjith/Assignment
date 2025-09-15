package com.example.load_balancer.util.exception;

public class NoHealthyInstanceException extends RuntimeException {
    public NoHealthyInstanceException(String message) {
        super(message);
    }
}
