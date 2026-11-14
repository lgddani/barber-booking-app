package com.barberbooking.api.barbers;

import com.barberbooking.api.common.EmailAlreadyRegisteredException;
import com.barberbooking.api.common.ResourceNotFoundException;
import com.barberbooking.api.users.BarberProfile;
import com.barberbooking.api.users.BarberProfileRepository;
import com.barberbooking.api.users.Role;
import com.barberbooking.api.users.User;
import com.barberbooking.api.users.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final UserRepository userRepository;
    private final BarberProfileRepository barberProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public List<BarberResponse> listActive() {
        return barberProfileRepository.findAllActiveWithUser().stream().map(this::toResponse).toList();
    }

    public List<BarberResponse> listAll() {
        return barberProfileRepository.findAllWithUser().stream().map(this::toResponse).toList();
    }

    public BarberResponse get(UUID id) {
        return toResponse(findProfileOrThrow(id));
    }

    //Creación atómica en dos tablas (users + barber_profiles)
    @Transactional
    public BarberResponse create(BarberCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        //1.Primero guardamos el User para generar su UUID
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(Role.BARBER);
        userRepository.save(user);

        //2Luego el BarberProfile toma prestado ese ID gracias a @MapsId
        BarberProfile profile = new BarberProfile();
        profile.setUser(user);
        profile.setBio(request.bio());
        barberProfileRepository.save(profile);

        return toResponse(profile);
    }

    @Transactional
    public BarberResponse update(UUID id, BarberUpdateRequest request) {
        BarberProfile profile = findProfileOrThrow(id);
        User user = profile.getUser();
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        profile.setBio(request.bio());
        return toResponse(profile);
    }

    //Doble bloqueo (oculta perfil + revoca acceso al sistema)
    @Transactional
    public void deactivate(UUID id) {
        BarberProfile profile = findProfileOrThrow(id);
        profile.setActive(false);
        profile.getUser().setEnabled(false);
    }

    @Transactional
    public void activate(UUID id) {
        BarberProfile profile = findProfileOrThrow(id);
        profile.setActive(true);
        profile.getUser().setEnabled(true);
    }

    private BarberProfile findProfileOrThrow(UUID id) {
        return barberProfileRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado: " + id));
    }

    private BarberResponse toResponse(BarberProfile profile) {
        User user = profile.getUser();
        return new BarberResponse(
                profile.getUserId(),
                user.getFullName(),
                user.getPhone(),
                profile.getBio(),
                profile.isActive()
        );
    }
}
