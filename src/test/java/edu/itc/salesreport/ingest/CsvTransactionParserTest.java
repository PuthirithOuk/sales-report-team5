package edu.itc.salesreport.ingest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.itc.salesreport.model.PaymentMethod;

class CsvTransactionParserTest {
    private final CsvTransactionParser parser = new CsvTransactionParser();

    @Test
    void canonicalRowParses() throws Exception {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR");
        assertEquals(new BigDecimal("13.00"), t.revenue());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "XXX,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR",   // unknown branch
        "PNH,2026-13-45,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR",   // bad date
        "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,0,6.50,0.00,KHQR",   // quantity 0
        "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,20.00,KHQR",  // discount > line amount
        "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Toys,2,6.50,0.00,KHQR",      // unknown category
        "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,PAYPAL", // unknown payment
        "PNH,2026-09-01,PNH-000001,SKU-1001,,Grocery,2,6.50,0.00,KHQR"                    // missing value
    })
    void invalidRowsAreRejected(String line) {
        assertThrows(InvalidRowException.class, () -> parser.parse(line));
    }
}