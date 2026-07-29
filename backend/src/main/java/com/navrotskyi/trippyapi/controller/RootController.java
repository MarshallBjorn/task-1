package com.navrotskyi.trippyapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Root", description = "Root API Endpoints")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Check API status", description = "Returns a simple JSON indicating that the application is running.")
    public Map<String, String> index() {
        return Map.of("status", "ok", "message", "Trippy hotreload WORKS!");
    }
}
