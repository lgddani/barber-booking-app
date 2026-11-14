package com.barberbooking.api.barbers;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @GetMapping
    @PreAuthorize("!#includeInactive or hasRole('ADMIN')")
    public List<BarberResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return includeInactive ? barberService.listAll() : barberService.listActive();
    }

    @GetMapping("/{id}")
    public BarberResponse get(@PathVariable UUID id) {
        return barberService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BarberResponse create(@Valid @RequestBody BarberCreateRequest request) {
        return barberService.create(request);
    }

    //Ownership check directo gracias a @MapsId
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public BarberResponse update(@PathVariable UUID id, @Valid @RequestBody BarberUpdateRequest request) {
        return barberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        barberService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable UUID id) {
        barberService.activate(id);
    }
}