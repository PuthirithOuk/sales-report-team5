
package edu.itc.salesreport.ui;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

public final class InputRules {

    private static final Set<String> BRANCHES =
            Set.of("ALL", "PNH", "REP", "BTB");

    private InputRules() {}

    public static Validation<YearMonth> month(
            String text, Clock clock) {

        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>(
                "Enter a month as yyyy-MM, e.g. 2026-09");
        }

        String value = text.strip();

        try {
            YearMonth month = YearMonth.parse(value);

            if (month.isAfter(YearMonth.now(clock))) {
                return new Validation.Invalid<>(
                    month + " is in the future");
            }

            return new Validation.Valid<>(month);

        } catch (DateTimeParseException e) {
            return new Validation.Invalid<>(
                "'" + value +
                "' is not a month in the form yyyy-MM");
        }
    }

    public static Validation<String> branch(String text) {

        String code = text == null
                ? ""
                : text.strip().toUpperCase(Locale.ROOT);

        if (!BRANCHES.contains(code)) {
            return new Validation.Invalid<>(
                "Unknown branch '" + code +
                "': use ALL, PNH, REP or BTB");
        }

        return new Validation.Valid<>(code);
    }
}
