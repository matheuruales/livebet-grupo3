package com.grupo3.betting.simulator;

import com.grupo3.betting.contracts.MatchFinishedEvent;

interface FinishedEventPort { void publish(MatchFinishedEvent event); }
