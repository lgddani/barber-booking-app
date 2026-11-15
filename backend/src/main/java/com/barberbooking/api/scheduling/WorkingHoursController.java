package com.barberbooking.api.scheduling;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/working-hours")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    // 1. ENDPOINT DE CONSULTA PÚBLICA
    @GetMapping
    public List<WorkingHoursItem> get(@PathVariable UUID barberId) {
        return workingHoursService.get(barberId);
    }

    // 2. ENDPOINT DE REEMPLAZO COMPLETO PROTEGIDO
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or #barberId == authentication.principal.id")
    public List<WorkingHoursItem> replace(
            @PathVariable UUID barberId,
            @Valid @RequestBody List<WorkingHoursItem> items
    ) {
        return workingHoursService.replace(barberId, items);
    }
}