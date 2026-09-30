package com.clinicbooking.clinicbackend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Service
public class RateLimiterService {

    // Separate buckets per key prefix, so login limits don't affect OTP limits, etc.
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int capacity, Duration window) {
        Bucket bucket = buckets.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder().capacity(capacity)
                                .refillIntervally(capacity, window).build())
                        .build());
        return bucket.tryConsume(1);
    }
}