package com.barberbooking.api.users;

import com.barberbooking.api.common.ResourceNotFoundException;
import com.barberbooking.api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.user().getFullName(),
                principal.getRole()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole()))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserResponse get(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}

