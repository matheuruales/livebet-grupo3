package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface BetRequestRepository extends JpaRepository<BetRequestEntity, String> {
    List<BetRequestEntity> findTop50ByUserIdOrderByCreatedAtDesc(String userId);
}
