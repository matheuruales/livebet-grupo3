package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface RouletteBetRepository extends JpaRepository<RouletteBet, String> {
    List<RouletteBet> findByRoundIdAndStatus(String roundId, RouletteBet.Status status);
    List<RouletteBet> findTop30ByUserIdOrderByCreatedAtDesc(String userId);
    long countByRoundId(String roundId);
}
