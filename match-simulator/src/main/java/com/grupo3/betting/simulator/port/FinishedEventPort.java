package com.grupo3.betting.simulator.port;

import com.grupo3.betting.contracts.event.MatchFinishedEvent;

public interface FinishedEventPort { void publish(MatchFinishedEvent event); }
