package com.itlab1024.proguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    private String unUsed;
    /**
     * 主页Mapping
     * @return
     */
    @GetMapping
    public String index() {
        return "这是主页";
    }
}
