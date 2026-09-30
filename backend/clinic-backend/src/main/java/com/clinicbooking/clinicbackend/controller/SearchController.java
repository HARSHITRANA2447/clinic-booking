package com.clinicbooking.clinicbackend.controller;

import com.clinicbooking.clinicbackend.dto.DoctorSearchResponse;
import com.clinicbooking.clinicbackend.exception.RateLimitException;
import com.clinicbooking.clinicbackend.security.RateLimiterService;
import com.clinicbooking.clinicbackend.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;
    private final RateLimiterService rateLimiter;

    @GetMapping
    public List<DoctorSearchResponse> search(
            HttpServletRequest httpReq,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng,
            @RequestParam(defaultValue = "10")
            @DecimalMin("0.5") @DecimalMax("50") double radiusKm,
            @RequestParam(required = false) String spec) {

        String ip = httpReq.getRemoteAddr();

        if (!rateLimiter.tryConsume(
                "search:" + ip,
                60,
                Duration.ofMinutes(1))) {

            throw new RateLimitException(
                    "Too many requests. Please slow down.");
        }

        return searchService.search(lat, lng, radiusKm, spec);
    }
}