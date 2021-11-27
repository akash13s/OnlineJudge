package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.SubmissionResponseDTO;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
public class PingController {
    private static final String ORDER_SERVICE ="orderService" ;

    @RequestMapping("/ping")
    @RateLimiter(name=ORDER_SERVICE, fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<?>  ping(){
        System.out.println("Hittting ping");
        return new ResponseEntity<String>("pong", HttpStatus.OK);
    }

    public ResponseEntity<String> rateLimiterFallback(Exception e) {
        return new ResponseEntity<String>("Ping Controller does not permit further calls", HttpStatus.TOO_MANY_REQUESTS);
    }
}
