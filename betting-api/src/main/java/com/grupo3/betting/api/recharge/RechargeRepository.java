package com.grupo3.betting.api.recharge;

import org.springframework.data.jpa.repository.JpaRepository;

interface RechargeRepository extends JpaRepository<RechargeTransaction, String> {}
