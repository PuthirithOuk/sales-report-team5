package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InputRulesTest {
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"), ZoneId.of("Asia/Phnom_Penh"));

    @Test
    void trimmedValidMonth() {
        assertEquals(new Validation.Valid<>(YearMonth.of(2026, 9)),
                InputRules.month("  2026-09 ", clock));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "2026/09", "2026-9", "Sept", "2026-10"})
    void invalidMonths(String text) {
        assertInstanceOf(Validation.Invalid.class, InputRules.month(text, clock));
    }

    @Test
    void futureMonthMessage() {
        assertEquals(new Validation.Invalid<>("2026-10 is in the future"),
                InputRules.month("2026-10", clock));
    }

    @Test
    void branchIsNormalised() {
        assertEquals(new Validation.Valid<>("PNH"), InputRules.branch(" pnh "));
        assertEquals(new Validation.Invalid<>("Unknown branch 'XYZ': use ALL, PNH, REP or BTB"),
                InputRules.branch("xyz"));
    }
}