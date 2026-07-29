package com.navrotskyi.trippyapi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurrencyRequest(
        @NotBlank(message = "[ERR] Currency code cannot be blank.")
        @Size(min = 3, max = 3, message = "[ERR] Currency code has to have exactly 3 characters (e.g. USD, PLN).")
        String code,

        @NotBlank(message = "[ERR] Currency name cannot be blank.")
        String name
) {}