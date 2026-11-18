package com.grupo3.betting.domain;

public interface PayoutStrategy { Money payout(Bet bet, String winner); }
