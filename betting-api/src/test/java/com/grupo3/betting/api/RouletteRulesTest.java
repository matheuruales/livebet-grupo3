package com.grupo3.betting.api;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class RouletteRulesTest {
    @Test void validatesEuropeanPayouts() {
        assertEquals(36, RouletteRules.validate("NUMBER", "17").multiplier());
        assertEquals(3, RouletteRules.validate("DOZEN", "SECOND").multiplier());
        assertEquals(2, RouletteRules.validate("COLOR", "RED").multiplier());
        assertEquals(new BigDecimal("36000"), RouletteRules.payout(new BigDecimal("1000"), 36));
    }

    @Test void zeroOnlyWinsStraightBets() {
        assertTrue(RouletteRules.wins("NUMBER", "0", 0));
        assertFalse(RouletteRules.wins("COLOR", "RED", 0));
        assertFalse(RouletteRules.wins("PARITY", "EVEN", 0));
        assertFalse(RouletteRules.wins("RANGE", "LOW", 0));
    }

    @Test void resolvesColorsAndDozens() {
        assertTrue(RouletteRules.wins("COLOR", "RED", 32));
        assertTrue(RouletteRules.wins("COLOR", "BLACK", 15));
        assertTrue(RouletteRules.wins("DOZEN", "THIRD", 36));
    }
}
