package com.barberbooking.api.availability;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvailabilityConfig {
    //Bean de Clock con zona horaria del negocio y fallback por defecto
    @Bean
    public Clock clock(@Value("${app.business-timezone}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}