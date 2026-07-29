package com.navrotskyi.trippyapi.controller.trip;

import com.navrotskyi.trippyapi.dto.admin.CurrencyResponse;
import com.navrotskyi.trippyapi.service.admin.AdminDictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dictionaries")
@Tag(name = "Dictionaries", description = "Public dictionaries available for all authenticated users")
public class DictionaryController {

    private final AdminDictionaryService dictionaryService;

    public DictionaryController(AdminDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/currencies")
    @Operation(summary = "Get all currencies", description = "Returns a list of all available currencies.")
    public ResponseEntity<List<CurrencyResponse>> getCurrencies() {
        return ResponseEntity.ok(dictionaryService.getAllCurrencies());
    }
}