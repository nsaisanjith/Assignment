package com.example.load_balancer.util;

public class CommonConstants {

    public static final int MIN_WEIGHT = 1;
    public static final int MAX_WEIGHT = 10;
    public  static final int MAX_CAPACITY = 100;
    public static final int MAX_RETRY_COUNT = 15;
    public static final int ALLOWED_RESPONSE_LATENCY = 300;
    public static final String HEALTH_CHECK_URI_SUFFIX = "/api/health";
    public static final int MAX_ATTEMPTS = 15;
    public static final int MAX_ATTEMPTS_FOR_INSTANCE = 3;
    public static final long MAX_TOTAL_DELAY = 100;
    public static final double EWMA_ALPHA = 0.7 ;
    public static final int MAX_DELTA_PER_RUN = 2;
}
