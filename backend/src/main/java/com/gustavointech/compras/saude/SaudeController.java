package com.gustavointech.compras.saude;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudeController {

    @GetMapping("/api/saude")
    public String saude() {
        return "ok";
    }
}