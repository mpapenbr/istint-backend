package de.mp.istint.server.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<String> getUUID() {
        UUID ret = UUID.randomUUID();
        return ResponseEntity
                .ok(ret.toString());
    }
}
