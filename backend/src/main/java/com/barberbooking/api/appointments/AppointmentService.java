package com.barberbooking.api.appointments;

import com.barberbooking.api.availability.AvailabilityService;
import com.barberbooking.api.common.BusinessRuleException;
import com.barberbooking.api.common.ResourceNotFoundException;
import com.barberbooking.api.common.SlotNotAvailableException;
import com.barberbooking.api.users.BarberProfileRepository;
import com.barberbooking.api.users.User;
import com.barberbooking.api.users.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final List<AppointmentStatus> INACTIVE_STATUSES =
            List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

    private final AppointmentRepository appointmentRepository;
    private final com.barberbooking.api.catalog.ServiceRepository serviceRepository;
    private final BarberProfileRepository barberProfileRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final Clock clock;

    @Transactional
    public AppointmentResponse create(UUID customerId, AppointmentCreateRequest request) {
        // 1. VALIDACIÓN DE EXISTENCIA DE ENTIDADES
        if (!barberProfileRepository.existsById(request.barberId())) {
            throw new ResourceNotFoundException("Barbero no encontrado: " + request.barberId());
        }
        var service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + request.serviceId()));

        // 2. PRIMERA LÍNEA DE DEFENSA: Motor de Disponibilidad (99.9% de los casos)
        boolean slotOffered = availabilityService.getAvailableSlots(request.barberId(), request.date(), request.serviceId())
                .stream()
                .anyMatch(slot -> slot.time().equals(request.startTime()) && slot.available());
        if (!slotOffered) {
            throw new SlotNotAvailableException();
        }

        // 3. CÁLCULO DE INSTANTES Y VALIDACIÓN AMIGABLE DEL CLIENTE
        ZoneId zone = clock.getZone();
        Instant startAt = request.date().atTime(request.startTime()).atZone(zone).toInstant();
        Instant endAt = startAt.plus(service.getDurationMinutes(), ChronoUnit.MINUTES);

        boolean customerBusy = !appointmentRepository
                .findActiveForCustomerInRange(customerId, startAt, endAt, INACTIVE_STATUSES)
                .isEmpty();
        if (customerBusy) {
            throw new BusinessRuleException("Ya tienes otra cita reservada en ese horario");
        }

        // 4. CONSTRUCCIÓN DE LA ENTIDAD (Congelando precio y duración reales)
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customerId);
        appointment.setBarberId(request.barberId());
        appointment.setServiceId(request.serviceId());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPriceAtBooking(service.getPrice());

        // 5. SEGUNDA LÍNEA DE DEFENSA (Definitiva): Ejecución inmediata contra PostgreSQL
        try {
            appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new SlotNotAvailableException();
        }

        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.from(appointment, customerNameOf(appointment.getCustomerId()));
    }

    private String customerNameOf(UUID customerId) {
        return userRepository.findById(customerId).map(User::getFullName).orElse("Cliente");
    }
}