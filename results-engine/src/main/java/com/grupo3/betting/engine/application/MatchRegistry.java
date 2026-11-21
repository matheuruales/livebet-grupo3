package com.grupo3.betting.engine.application;

import com.grupo3.betting.contracts.notification.DashboardNotification;
import com.grupo3.betting.contracts.event.MatchCreatedEvent;
import com.grupo3.betting.domain.match.MatchAggregate;
import com.grupo3.betting.domain.match.MatchId;
import com.grupo3.betting.domain.match.MatchStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
class MatchRegistry {
    private final Map<String, MatchAggregate> matches = new ConcurrentHashMap<>();
    private final Map<String, Instant> starts = new ConcurrentHashMap<>();
    private final Map<String, Instant> ends = new ConcurrentHashMap<>();
    private final Map<String, Integer> homeScores = new ConcurrentHashMap<>();
    private final Map<String, Integer> awayScores = new ConcurrentHashMap<>();

    MatchAggregate create(MatchCreatedEvent event) {
        starts.putIfAbsent(event.matchId(), event.occurredAt());
        ends.putIfAbsent(event.matchId(), event.endsAt() == null ? event.occurredAt().plusSeconds(60) : event.endsAt());
        return matches.computeIfAbsent(event.matchId(), id -> MatchAggregate.create(new MatchId(id), event.teams()));
    }

    MatchAggregate require(String matchId) {
        MatchAggregate match = matches.get(matchId);
        if (match == null) throw new IllegalStateException("No existe el partido " + matchId);
        return match;
    }

    void score(String matchId, Integer homeScore, Integer awayScore) {
        if (homeScore != null) {
            homeScores.put(matchId, homeScore);
            awayScores.put(matchId, awayScore);
        }
    }

    DashboardNotification dashboard(MatchAggregate match, Instant occurredAt) {
        String id = match.id().value();
        return new DashboardNotification(id, match.status().name(), match.teams(), match.winner(),
                homeScores.get(id), awayScores.get(id), match.bets().size(), match.totalBet(),
                starts.get(id), ends.get(id), occurredAt);
    }

    Map<String, MatchAggregate> snapshot() { return Map.copyOf(matches); }

    List<OverdueMatch> overdue(Instant now) {
        return matches.values().stream()
                .filter(match -> match.status() == MatchStatus.OPEN_FOR_BETS)
                .filter(match -> {
                    Instant end = ends.get(match.id().value());
                    return end != null && !now.isBefore(end.plusSeconds(3));
                })
                .map(match -> new OverdueMatch(match.id().value(), match.teams()))
                .toList();
    }

    record OverdueMatch(String matchId, List<String> teams) {}
}
