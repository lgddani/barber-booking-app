package com.barberbooking.api.scheduling;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/exceptions")
@RequiredArgsConstructor
public class ScheduleExceptionController {

    private final ScheduleExceptionService scheduleExceptionService;

    // 1. CONSULTA PÚBLICA CON PARÁMETROS DE RANGO OPCIONALES
    @GetMapping
    public List<ScheduleExceptionResponse> list(
            @PathVariable UUID barberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return scheduleExceptionService.list(barberId, from, to);
    }

    // 2. CREACIÓN DE EXCEPCIÓN (201 CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #barberId == authentication.principal.id")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleExceptionResponse create(
            @PathVariable UUID barberId,
            @Valid @RequestBody ScheduleExceptionRequest request
    ) {
        return scheduleExceptionService.create(barberId, request);
    }

    // 3. ELIMINACIÓN FÍSICA DE EXCEPCIÓN (204 NO CONTENT)
    @DeleteMapping("/{exceptionId}")
    @PreAuthorize("hasRole('ADMIN') or #barberId == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID barberId, @PathVariable UUID exceptionId) {
        scheduleExceptionService.delete(barberId, exceptionId);
    }
}