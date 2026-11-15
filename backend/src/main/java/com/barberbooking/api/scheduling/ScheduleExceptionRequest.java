package com.barberbooking.api.scheduling;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleExceptionRequest(
        @NotNull @FutureOrPresent LocalDate date,
        @NotNull ExceptionType type,
        LocalTime startTime,
        LocalTime endTime
) {}
