package com.grupo3.betting.api.roulette;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

interface RouletteRoundRepository extends JpaRepository<RouletteRound, String> {
    Optional<RouletteRound> findTopByOrderByOpenedAtDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RouletteRound r where r.id = :id")
    Optional<RouletteRound> findByIdForUpdate(@Param("id") String id);
}
