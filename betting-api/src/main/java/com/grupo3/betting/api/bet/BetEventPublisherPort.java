package com.grupo3.betting.api.bet;

import com.grupo3.betting.contracts.event.BetPlacedEvent;

interface BetEventPublisherPort {
    void publish(BetPlacedEvent event);
}
