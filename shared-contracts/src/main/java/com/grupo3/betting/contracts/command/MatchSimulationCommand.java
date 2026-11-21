package com.grupo3.betting.contracts.command;

import java.util.List;

public record MatchSimulationCommand(String matchId, List<String> teams) {
    public MatchSimulationCommand {
        if (matchId == null || matchId.isBlank()) throw new IllegalArgumentException("matchId es obligatorio");
        if (teams == null || teams.size() != 2) throw new IllegalArgumentException("Se requieren dos equipos");
        teams = List.copyOf(teams);
    }
}
