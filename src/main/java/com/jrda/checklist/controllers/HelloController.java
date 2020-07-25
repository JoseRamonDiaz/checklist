package com.jrda.checklist.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping("hello")
    public String helloWorld() {
        return "Hello "+ SecurityContextHolder.getContext().getAuthentication().getName()+"!!";
    }
}
