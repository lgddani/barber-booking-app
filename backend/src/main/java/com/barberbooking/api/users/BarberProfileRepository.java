package com.barberbooking.api.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BarberProfileRepository extends JpaRepository<BarberProfile, UUID> {
    @Query("SELECT bp FROM BarberProfile bp JOIN FETCH bp.user WHERE bp.userId = :userId")
    Optional<BarberProfile> findByIdWithUser(@Param("userId") UUID userId);
}
