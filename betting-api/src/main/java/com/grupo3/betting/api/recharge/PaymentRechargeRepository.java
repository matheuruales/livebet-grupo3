package com.grupo3.betting.api.recharge;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface PaymentRechargeRepository extends JpaRepository<PaymentRecharge, String> {
    Optional<PaymentRecharge> findByReferenceAndUserId(String reference, String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PaymentRecharge r where r.reference = :reference")
    Optional<PaymentRecharge> findByReferenceForUpdate(@Param("reference") String reference);
}
