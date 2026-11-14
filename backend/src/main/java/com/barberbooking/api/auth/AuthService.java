package com.barberbooking.api.auth;

import com.barberbooking.api.common.EmailAlreadyRegisteredException;
import com.barberbooking.api.common.InvalidCredentialsException;
import com.barberbooking.api.security.JwtService;
import com.barberbooking.api.security.UserPrincipal;
import com.barberbooking.api.users.Role;
import com.barberbooking.api.users.User;
import com.barberbooking.api.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole());
    }
}
