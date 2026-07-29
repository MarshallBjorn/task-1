package com.navrotskyi.trippyapi.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Could not initialize folder for upload!");
        }
    }

    public String savePhoto(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        try {
            Tika tika = new Tika();
            String detectedMimeType = tika.detect(file.getInputStream());

            if (detectedMimeType == null || !detectedMimeType.startsWith("image/")) {
                throw new IllegalArgumentException("[SECURITY] Invalid file content. Only images are allowed.");
            }

            String safeExtension = detectedMimeType.split("/")[1];

            String newFilename = UUID.randomUUID().toString() + "." + safeExtension;

            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetLocation = uploadRoot.resolve(newFilename).normalize();
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public void deletePhoto(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        try {
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(filename).normalize();
            
            if (!filePath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Nieprawidłowa ścieżka: Wykryto path traversal.");
            }

            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete physical file: " + filename, ex);
        }
    }
}