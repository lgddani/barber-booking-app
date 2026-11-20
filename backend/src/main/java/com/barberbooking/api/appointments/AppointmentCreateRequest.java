package com.barberbooking.api.appointments;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentCreateRequest(
        @NotNull UUID barberId,
        @NotNull UUID serviceId,
        @NotNull @FutureOrPresent LocalDate date,
        @NotNull LocalTime startTime
) {
}
