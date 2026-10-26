package edu.itc.salesreport.ingest;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CsvTransactionParserTest {
    @Test
    void rejectsMissingRequiredValue() {
        var parser = new CsvTransactionParser();
        String line = "PNH,2026-09-01,PNH-000001,SKU-1001,,Grocery,2,6.50,0.00,KHQR";

        assertThrows(InvalidRowException.class, () -> parser.parse(line));
    }
}
