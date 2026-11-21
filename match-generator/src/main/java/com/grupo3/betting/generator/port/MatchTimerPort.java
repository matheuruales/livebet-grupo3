package com.grupo3.betting.generator.port;

import com.grupo3.betting.contracts.command.MatchSimulationCommand;

public interface MatchTimerPort { void schedule(MatchSimulationCommand command, String messageId, long delayMs); }
