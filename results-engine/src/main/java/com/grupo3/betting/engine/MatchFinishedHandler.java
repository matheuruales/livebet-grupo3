package com.grupo3.betting.engine;

import com.grupo3.betting.contracts.EmailNotification;
import com.grupo3.betting.contracts.MatchFinishedEvent;
import com.grupo3.betting.domain.FixedOddsPayoutStrategy;
import com.grupo3.betting.domain.MatchAggregate;
import com.grupo3.betting.domain.MatchStatus;
import com.grupo3.betting.domain.PayoutStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
class MatchFinishedHandler {
    private static final Logger log = LoggerFactory.getLogger(MatchFinishedHandler.class);
    private final MatchRegistry registry;
    private final BetContactRegistry contacts;
    private final MatchAuditRepository matches;
    private final NotificationPort notifications;
    private final PayoutStrategy payouts = new FixedOddsPayoutStrategy(BigDecimal.valueOf(2));

    MatchFinishedHandler(MatchRegistry registry, BetContactRegistry contacts, MatchAuditRepository matches,
                         NotificationPort notifications) {
        this.registry = registry;
        this.contacts = contacts;
        this.matches = matches;
        this.notifications = notifications;
    }

    void handle(MatchFinishedEvent event, boolean firstProcessing) {
        MatchAggregate match = registry.require(event.matchId());
        if (match.status() == MatchStatus.SETTLED) return;
        match.finish(event.winner());
        match.settle(payouts);
        registry.score(event.matchId(), event.homeScore(), event.awayScore());
        if (firstProcessing) persistAndNotify(event, match);
        notifications.dashboard("notify.dashboard.finished", registry.dashboard(match, event.occurredAt()));
        log.info("Partido {} liquidado, marcador={}-{}, ganador={}, apuestas={}", match.id().value(),
                event.homeScore(), event.awayScore(), match.winner(), match.bets().size());
    }

    private void persistAndNotify(MatchFinishedEvent event, MatchAggregate match) {
        var audit = new MatchAuditEntity(match.id().value(), match.teams().get(0), match.teams().get(1),
                match.status().name(), match.winner(), event.homeScore(), event.awayScore(), match.totalBet(), Instant.now());
        for (var settlement : match.settlements()) {
            var bet = settlement.bet();
            audit.add(BetAuditEntity.settled(bet, settlement.winner(), settlement.payout().amount()));
            notifications.email(settlement.winner() ? "notify.email.win" : "notify.email.loss",
                    new EmailNotification(UUID.randomUUID().toString(), contacts.emailFor(bet.id().value()),
                            bet.userId().value(), bet.id().value(), match.id().value(),
                            settlement.winner() ? "WIN" : "LOSS", match.teams().get(0), match.teams().get(1),
                            bet.team(), match.winner(), event.homeScore(), event.awayScore(), bet.stake().amount(),
                            settlement.payout().amount(), event.occurredAt()));
        }
        matches.save(audit);
    }
}
