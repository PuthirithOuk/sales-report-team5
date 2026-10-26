package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SalesModelTest {
    @TempDir
    Path tempDir;

    private SalesModel model;
    private final List<SalesEvent> events = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        model = new SalesModel(
                new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator());
        TestData.writeSample(tempDir);
        model.addListener(events::add);
    }

    @Test
    void loadFiresMonthLoadedAndSummarizesAllBranches() throws Exception {
        model.load(tempDir, YearMonth.of(2026, 9));

        assertEquals(List.of(new SalesEvent.MonthLoaded(
                YearMonth.of(2026, 9), 3, 1)), events);
        assertEquals(new BigDecimal("52.00"), model.summary().revenue());
        assertEquals(2, model.summary().receipts());
        assertEquals(new BigDecimal("26.00"), model.summary().averageBasket());
        assertEquals(1, model.rejectedRows().size());
        assertEquals(YearMonth.of(2026, 9), model.month().orElseThrow());
    }

    @Test
    void selectingSameBranchTwiceFiresOnceAndFiltersSummary() throws Exception {
        model.load(tempDir, YearMonth.of(2026, 9));
        events.clear();

        model.selectBranch("PNH");
        model.selectBranch("PNH");

        assertEquals(List.of(new SalesEvent.BranchSelected("PNH")), events);
        assertEquals(2, model.visibleTransactions().size());
        assertEquals(new BigDecimal("31.00"), model.summary().revenue());
        assertEquals(1, model.summary().receipts());
        assertEquals(new BigDecimal("31.00"), model.summary().averageBasket());
    }

    @Test
    void failedLoadFiresNothingAndKeepsPreviouslyLoadedData() throws Exception {
        model.load(tempDir, YearMonth.of(2026, 9));
        events.clear();

        assertThrows(NoSuchFileException.class,
                () -> model.load(tempDir, YearMonth.of(2026, 7)));

        assertEquals(List.of(), events);
        assertEquals(3, model.visibleTransactions().size());
        assertEquals(YearMonth.of(2026, 9), model.month().orElseThrow());
    }

    @Test
    void removedListenerIsNotCalled() throws Exception {
        SalesModelListener removedListener = events::add;
        model.addListener(removedListener);
        model.removeListener(removedListener);

        model.load(tempDir, YearMonth.of(2026, 9));

        assertEquals(1, events.size());
        assertEquals(new SalesEvent.MonthLoaded(
                YearMonth.of(2026, 9), 3, 1), events.get(0));
    }
}
