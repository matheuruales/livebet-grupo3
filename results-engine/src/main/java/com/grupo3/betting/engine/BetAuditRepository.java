package com.grupo3.betting.engine;

import org.springframework.data.jpa.repository.JpaRepository;

interface BetAuditRepository extends JpaRepository<BetAuditEntity, String> {}
