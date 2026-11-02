package com.barberbooking.api.common;

import com.barberbooking.api.users.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TempLazyDemoRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BarberProfileRepository barberProfileRepository;

    @Override
    public void run(String... args){
        User user = new User();
        user.setEmail("ejemplo4@info.com");
        user.setPasswordHash("temporal123");
        user.setFullName("Carlitos Barbero");
        user.setRole(Role.BARBER);
        userRepository.save(user);

        BarberProfile profile = new BarberProfile();
        profile.setUser(user);
        profile.setBio("Especialista en cortes de cabello clásico");
        barberProfileRepository.save(profile);

        BarberProfile cargado = barberProfileRepository.findByIdWithUser(user.getId()).orElseThrow();

        System.out.println("Nombre del barbero: " + cargado.getUser().getFullName());
    }
}
