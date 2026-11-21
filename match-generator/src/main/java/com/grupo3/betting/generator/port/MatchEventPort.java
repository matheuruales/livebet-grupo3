package com.grupo3.betting.generator.port;

import com.grupo3.betting.contracts.event.MatchCreatedEvent;

public interface MatchEventPort { void publish(MatchCreatedEvent event); }
