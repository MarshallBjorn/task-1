package com.navrotskyi.trippyapi.service.admin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.admin.UserAdminResponse;
import com.navrotskyi.trippyapi.dto.admin.UserCreateRequest;
import com.navrotskyi.trippyapi.dto.admin.UserUpdateRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.service.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AdminUserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public AdminUserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        FileStorageService fileStorageService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Value("${app.file-server.base-url:/uploads/}")
    private String fileServerBaseUrl;

    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserAdminResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("[ERROR] User with ID: " + id + " was not found."));

        return mapToResponse(user);
    }

    @Transactional
    public UserAdminResponse createUser(UserCreateRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("[ERROR] User with email: " + request.email() + " already exists.");
        }

        User user = new User();
        
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setBlocked(request.isBlocked());
        user.setVerified(request.isVerified());

        User createdUser = userRepository.save(user);

        return mapToResponse(createdUser);
    }

    @Transactional
    public UserAdminResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("[ERROR] User with ID: " + id + " was not found."));

        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(id)) {
                throw new IllegalArgumentException("[ERROR] Email is already taken by another user");
            }
        });

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setBlocked(request.isBlocked());
        user.setVerified(request.isVerified());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("[ERROR] User with ID: " + id + " was not found.");
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public UserAdminResponse uploadUserPhoto(UUID id, MultipartFile file) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("[ERROR] User with ID: " + id + " was not found."));

        String savedFilename = fileStorageService.savePhoto(file);
        
        user.setPhotoUrl(savedFilename);
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private UserAdminResponse mapToResponse(User user) {
        String fullPhotoUrl = (user.getPhotoUrl() != null) ? fileServerBaseUrl + user.getPhotoUrl() : null;

        return new UserAdminResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isVerified(),
                user.isBlocked(),
                fullPhotoUrl
        );
    }
}