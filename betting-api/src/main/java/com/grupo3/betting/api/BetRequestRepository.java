package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

interface BetRequestRepository extends JpaRepository<BetRequestEntity, String> {
    List<BetRequestEntity> findTop50ByUserIdOrderByCreatedAtDesc(String userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BetRequestEntity b where b.id = :id")
    Optional<BetRequestEntity> findByIdForSettlement(@Param("id") String id);

    @Query(value = """
            select request.id as "betId", request.user_id as "userId", audit.winner as winner, audit.payout as payout
            from bet_requests request
            join bets audit on audit.id = request.id
            where request.status = 'ACCEPTED_FOR_PROCESSING'
            """, nativeQuery = true)
    List<PendingSettlement> findPendingSettlements();

    interface PendingSettlement {
        String getBetId();
        String getUserId();
        Boolean getWinner();
        BigDecimal getPayout();
    }
}
