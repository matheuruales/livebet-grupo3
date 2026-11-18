package com.grupo3.betting.generator;

import com.grupo3.betting.contracts.MatchSimulationCommand;

interface MatchTimerPort { void schedule(MatchSimulationCommand command, String messageId, long delayMs); }
