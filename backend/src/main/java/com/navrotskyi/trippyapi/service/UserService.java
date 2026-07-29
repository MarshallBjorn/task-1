package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.UpdateUserRequest;
import com.navrotskyi.trippyapi.dto.UserResponse;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.CurrencyRepository;
import com.navrotskyi.trippyapi.repository.TripParticipantRepository;
import com.navrotskyi.trippyapi.domain.Currency;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.navrotskyi.trippyapi.dto.user.InvitationDto;
import java.util.List;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final CurrencyRepository currencyRepository;
    private final TripParticipantRepository tripParticipantRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            FileStorageService fileStorageService, CurrencyRepository currencyRepository,TripParticipantRepository tripParticipantRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.currencyRepository = currencyRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    @Value("${app.file-server.url:/uploads/}")
    private String fileServerBaseUrl;

    public UserResponse uploadProfilePhoto(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String savedFilename = fileStorageService.savePhoto(file);

        user.setPhotoUrl(savedFilename);
        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        if (request.email() != null && !request.email().isBlank()) {
            boolean emailTaken = userRepository.findByEmail(request.email())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();
            if (emailTaken) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            if (request.currentPassword() == null ||
                    !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Wrong password");
            }

            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.currency_code() != null && !request.currency_code().isBlank()) {
            Currency currency = currencyRepository.findById(request.currency_code())
                    .orElseThrow(() -> new RuntimeException("Currency not found"));

            user.setCurrency(currency);
        }

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    private UserResponse mapToResponse(User user) {
        String fullPhotoUrl = (user.getPhotoUrl() != null) ? fileServerBaseUrl + user.getPhotoUrl() : null;
        String userCurrencyCode = (user.getCurrency() != null) ? user.getCurrency().getCode() : null;

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isVerified(),
                fullPhotoUrl,
                userCurrencyCode);
    }

    public List<InvitationDto> getInvitations(String email) {
        return tripParticipantRepository
                .findByUserEmailAndIsAcceptedFalse(email)
                .stream()
                .map(p -> new InvitationDto(
                        p.getEvent().getId(),
                        p.getEvent().getName(),
                        p.getEvent().getOwner().getName(),
                        p.getTripRole().getName(),
                        p.isAccepted()))
                .toList();
    }
}