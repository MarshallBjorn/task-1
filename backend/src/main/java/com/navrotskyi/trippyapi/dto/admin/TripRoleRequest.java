package com.navrotskyi.trippyapi.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record TripRoleRequest(
        @NotBlank(message = "[ERR] Trip role name cannot be blank.")
        String name
) {}