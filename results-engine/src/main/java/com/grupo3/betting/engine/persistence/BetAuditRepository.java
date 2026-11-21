package com.grupo3.betting.engine.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BetAuditRepository extends JpaRepository<BetAuditEntity, String> {}
