package com.grupo3.betting.api.bet;

import com.grupo3.betting.contracts.event.BetPlacedEvent;

interface BetEventOutboxPort {
    void enqueue(BetPlacedEvent event);
}
