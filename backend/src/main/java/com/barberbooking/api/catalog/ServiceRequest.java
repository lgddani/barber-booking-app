package com.barberbooking.api.catalog;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServiceRequest(
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotNull @Positive Integer durationMinutes,
        @NotNull @DecimalMin(value = "0.0") BigDecimal price
) {
}
