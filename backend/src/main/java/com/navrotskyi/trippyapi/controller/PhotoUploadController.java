package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.service.FileStorageService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/photos")
public class PhotoUploadController {

    private final FileStorageService fileStorageService;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public PhotoUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        Bucket bucket = cache.computeIfAbsent(ipAddress, k -> createNewBucket());

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Zbyt wiele zapytań. Maksymalnie 10 zdjęć na minutę."));
        }

        String savedFilename = fileStorageService.savePhoto(file);
        String fileUrl = "http://localhost:8888/uploads/" + savedFilename;

        return ResponseEntity.ok(Map.of(
                "fileName", savedFilename,
                "url", fileUrl
        ));
    }
}