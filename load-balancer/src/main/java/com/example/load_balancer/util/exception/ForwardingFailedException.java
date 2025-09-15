package com.example.load_balancer.util.exception;

public class ForwardingFailedException extends RuntimeException {
    public ForwardingFailedException(String message,Exception e) {
        super(message,e);
    }
}
