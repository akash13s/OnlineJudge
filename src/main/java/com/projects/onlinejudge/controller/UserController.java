package com.projects.onlinejudge.controller;

import com.projects.onlinejudge.dto.UserDTO;
import com.projects.onlinejudge.service.impl.UserServiceImpl;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String ORDER_SERVICE ="orderService" ;

    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/register")
    @RateLimiter(name=ORDER_SERVICE, fallbackMethod = "rateLimiterFallback")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        boolean success = userService.addUser(userDTO);
        return new ResponseEntity<>(success? "User registered successfully":
                "Unable to register user", HttpStatus.OK);
    }

    public ResponseEntity<String> rateLimiterFallback(Exception e) {
        return new ResponseEntity<String>("User Controller does not permit further calls", HttpStatus.TOO_MANY_REQUESTS);
    }

}
