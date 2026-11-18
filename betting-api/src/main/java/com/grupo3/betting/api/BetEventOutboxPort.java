package com.grupo3.betting.api;

import com.grupo3.betting.contracts.BetPlacedEvent;

interface BetEventOutboxPort {
    void enqueue(BetPlacedEvent event);
}
