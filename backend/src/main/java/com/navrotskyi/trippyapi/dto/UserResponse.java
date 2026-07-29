package com.navrotskyi.trippyapi.dto;

import java.util.UUID;

import com.navrotskyi.trippyapi.domain.Role;

public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private boolean isVerified;
    private String photoUrl;
    private String currencyCode;

    public UserResponse(UUID id, String name, String email, Role role, boolean isVerified, String photoUrl, String currencyCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isVerified = isVerified;
        this.photoUrl = photoUrl;
        this.currencyCode = currencyCode;

    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isVerified() { return isVerified; }
    public String getPhotoUrl() { return photoUrl; }
    public String getCurrencyCode() {return currencyCode; }
}