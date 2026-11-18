package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;

interface RechargeRepository extends JpaRepository<RechargeTransaction, String> {}
