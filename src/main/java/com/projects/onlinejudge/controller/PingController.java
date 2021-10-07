package com.projects.onlinejudge.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @RequestMapping("/ping")
    public static String ping(){
        return "pong";
    }
}
