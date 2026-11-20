package com.barberbooking.api.appointments;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID barberId,
        String customerName,
        UUID serviceId,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status,
        BigDecimal priceAtBooking
) {
    public static AppointmentResponse from(Appointment a, String customerName) {
        return new AppointmentResponse(
                a.getId(),
                a.getBarberId(),
                customerName,
                a.getServiceId(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getPriceAtBooking()
        );
    }
}