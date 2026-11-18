package com.grupo3.betting.generator;

import com.grupo3.betting.contracts.MatchCreatedEvent;

interface MatchEventPort { void publish(MatchCreatedEvent event); }
