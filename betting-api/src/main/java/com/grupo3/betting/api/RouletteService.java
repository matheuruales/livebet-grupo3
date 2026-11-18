package com.grupo3.betting.api;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
class RouletteService {
    private final RouletteRoundRepository rounds;
    private final RouletteBetRepository bets;
    private final UserAccountRepository users;
    private final SecureRandom random = new SecureRandom();

    RouletteService(RouletteRoundRepository rounds, RouletteBetRepository bets, UserAccountRepository users) {
        this.rounds = rounds; this.bets = bets; this.users = users;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void advanceRound() {
        Instant now = Instant.now();
        RouletteRound round = rounds.findTopByOrderByOpenedAtDesc().orElseGet(() -> rounds.save(new RouletteRound(now)));
        if (round.status() == RouletteRound.Status.BETTING && !now.isBefore(round.closesAt())) {
            round.startSpin(random.nextInt(37));
        }
        if (round.status() == RouletteRound.Status.SPINNING && !now.isBefore(round.resolvesAt())) settle(round);
        if (round.status() == RouletteRound.Status.SETTLED && !now.isBefore(round.nextRoundAt())) rounds.save(new RouletteRound(now));
    }

    @Transactional(readOnly = true)
    RouletteRoundView current() {
        RouletteRound round = rounds.findTopByOrderByOpenedAtDesc().orElse(null);
        if (round == null) return new RouletteRoundView(null, "PREPARING", Instant.now(), null, null, null, null, null, null, 0, List.of());
        Integer visibleResult = round.status() == RouletteRound.Status.BETTING ? null : round.resultNumber();
        List<Integer> recent = rounds.findAll().stream()
                .filter(r -> r.status() == RouletteRound.Status.SETTLED && r.resultNumber() != null)
                .sorted((a,b) -> b.openedAt().compareTo(a.openedAt())).limit(10).map(RouletteRound::resultNumber).toList();
        return new RouletteRoundView(round.id(), round.status().name(), Instant.now(), round.openedAt(), round.closesAt(),
                round.resolvesAt(), round.nextRoundAt(), visibleResult, color(visibleResult), bets.countByRoundId(round.id()), recent);
    }

    @Transactional
    RouletteBetResponse place(String userId, RouletteBetRequest request) {
        RouletteRound round = rounds.findByIdForUpdate(request.roundId())
                .orElseThrow(() -> new IllegalArgumentException("La ronda ya no está disponible"));
        if (round.status() != RouletteRound.Status.BETTING || !Instant.now().isBefore(round.closesAt()))
            throw new IllegalStateException("Las apuestas de esta ronda ya cerraron");
        if (request.amount().remainder(BigDecimal.valueOf(1000)).signum() != 0)
            throw new IllegalArgumentException("El monto debe ser múltiplo de $1.000");
        RouletteRules.Selection selection = RouletteRules.validate(request.selectionType(), request.selectionValue());
        UserAccount user = users.findByIdForUpdate(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.debit(request.amount());
        RouletteBet bet = bets.save(new RouletteBet(round.id(), user.id(), selection.type(), selection.value(), selection.label(), request.amount()));
        return new RouletteBetResponse(bet.id(), round.id(), selection.label(), request.amount(), selection.multiplier(), user.balance());
    }

    @Transactional(readOnly = true)
    List<RouletteBetView> mine(String userId) {
        return bets.findTop30ByUserIdOrderByCreatedAtDesc(userId).stream().map(b ->
                new RouletteBetView(b.id(), b.roundId(), b.selectionLabel(), b.amount(), b.payout(), b.status().name(), b.createdAt())).toList();
    }

    private void settle(RouletteRound round) {
        int result = round.resultNumber();
        for (RouletteBet bet : bets.findByRoundIdAndStatus(round.id(), RouletteBet.Status.PENDING)) {
            boolean won = RouletteRules.wins(bet.selectionType(), bet.selectionValue(), result);
            BigDecimal payout = won ? RouletteRules.payout(bet.amount(), RouletteRules.validate(bet.selectionType(), bet.selectionValue()).multiplier()) : BigDecimal.ZERO;
            if (won) users.findByIdForUpdate(bet.userId()).ifPresent(user -> user.credit(payout));
            bet.settle(won, payout);
        }
        round.settle();
    }

    private String color(Integer number) {
        if (number == null) return null;
        if (number == 0) return "GREEN";
        return RouletteRules.isRed(number) ? "RED" : "BLACK";
    }
}
