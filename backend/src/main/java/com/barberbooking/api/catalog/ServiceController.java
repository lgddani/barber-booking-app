package com.barberbooking.api.catalog;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("!#includeInactive or hasRole('ADMIN')")
    public List<ServiceResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return catalogService.list(includeInactive);
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable UUID id) {
        return catalogService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request) {
        return catalogService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        return catalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        catalogService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable UUID id) {
        catalogService.activate(id);
    }
}