package com.example.taskmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from task management system";
    }

    @GetMapping("/api/hi")
    public String hi() {
        return "Hi from Manoj";
    }
}