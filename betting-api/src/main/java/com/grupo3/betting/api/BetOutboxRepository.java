package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface BetOutboxRepository extends JpaRepository<BetOutboxEntity, String> {
    List<BetOutboxEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
