package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.*;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Plain JUnit: no JavaFX toolkit, no window, no robot, no sleep. */
class ReportViewModelTest {

    // 2026-09-27T03:00:00Z = 2026-09-27T10:00:00+07:00 (Asia/Phnom_Penh)
    // so YearMonth.now(SEP_27) == 2026-09
    static final Clock SEP_27 = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));

    static final YearMonth SEP = YearMonth.of(2026, 9);
    static final YearMonth AUG = YearMonth.of(2026, 8);

    ManualJobs      jobs     = new ManualJobs();
    List<String>    loaded   = new ArrayList<>();
    boolean         failNext;
    ReportViewModel vm;

    @BeforeEach
    void setUp() {
        SummarySource source = (month, branch) -> {
            if (failNext) throw new IOException(branch + "-2026-08-03.csv: unreadable");
            loaded.add(branch);
            return summary(branch, "200.00");
        };
        vm = new ReportViewModel(source, jobs, SEP_27);
    }

    static BranchSummary summary(String branch, String revenue) {
        var rice = new ProductTotal("SKU-1001", "Jasmine Rice 5kg",
                new BigDecimal(revenue), 5);
        return new BranchSummary(branch, new BigDecimal(revenue),
                BigDecimal.ZERO, 10, Map.of(), List.of(rice), Map.of());
    }

    // ── Validation (3 tests) ──────────────────────────────────────────────

    @Test
    void futureMonthIsRejected() {
        vm.monthProperty().set(YearMonth.of(2026, 10));
        assertEquals("Month 2026-10 is in the future.",
                vm.monthErrorProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void currentMonthIsAccepted() {
        vm.monthProperty().set(SEP);
        assertEquals("", vm.monthErrorProperty().get());
        assertTrue(vm.generateCommand().isExecutable(),
                "generate should be executable for current month with branches selected");
    }

    @Test
    void emptyBranchSelectionIsRejected() {
        vm.monthProperty().set(AUG);
        for (String b : ReportViewModel.BRANCHES) {
            vm.branchSelectedProperty(b).set(false);
        }
        assertEquals("Select at least one branch.",
                vm.branchErrorProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    // ── Command enablement (2 tests) ──────────────────────────────────────

    @Test
    void generateStartsLoadingAndDisablesItself() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();

        assertTrue(vm.loadingProperty().get(),   "must be loading");
        assertFalse(vm.generateCommand().isExecutable(), "generate must be disabled while loading");
        assertTrue(vm.cancelCommand().isExecutable(),    "cancel must be enabled while loading");
        assertEquals(1, jobs.pendingCount());
    }

    @Test
    void generateIsIgnoredWhileInvalid() {
        vm.monthProperty().set(null);          // invalid: no month
        vm.generateCommand().execute();        // DelegateCommand guards this
        assertEquals(0, jobs.pendingCount());
        assertFalse(vm.loadingProperty().get());
    }

    // ── Success with totals (1 test) ──────────────────────────────────────

    @Test
    void successfulLoadFillsSummariesAndTotal() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertEquals(1, jobs.pendingCount());

        jobs.runNext();

        assertEquals(3, vm.summaries().size(),
                "all 3 branches loaded");
        assertEquals(List.of("PNH", "REP", "BTB"), loaded);
        // 3 branches × USD 200 = USD 600
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
    }

    // ── Progress (1 test) ─────────────────────────────────────────────────

    @Test
    void progressFollowsTheBranches() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertEquals(0.0, vm.progressProperty().get(), 0.001,
                "progress starts at 0 before the job runs");

        jobs.runNext();

        assertEquals(1.0, vm.progressProperty().get(), 0.001,
                "progress reaches 1.0 after all branches loaded");
    }

    // ── Failure (1 test) ──────────────────────────────────────────────────

    @Test
    void failureKeepsPreviousDataAndShowsError() {
        // first load succeeds
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());

        // second load fails
        failNext = true;
        vm.generateCommand().execute();
        jobs.runNext();

        assertEquals(3, vm.summaries().size(),
                "previous summaries must be preserved");
        assertFalse(vm.loadErrorProperty().get().isEmpty(),
                "loadError must be set");
        assertTrue(vm.loadErrorProperty().get().contains("unreadable"));
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
    }

    // ── Cancellation (1 test) ─────────────────────────────────────────────

    @Test
    void cancelReenablesGenerateAndKeepsOldData() {
        // first load succeeds
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());

        // start second load, then cancel before it runs
        vm.generateCommand().execute();
        assertEquals(1, jobs.pendingCount());
        assertTrue(vm.cancelCommand().isExecutable());

        vm.cancelCommand().execute();

        assertEquals(0, jobs.pendingCount());
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
        assertEquals(3, vm.summaries().size(), "old data must be preserved after cancel");
        assertEquals("Cancelled, previous data kept.", vm.statusProperty().get());
    }

    // ── Product filter (1 test) ───────────────────────────────────────────

    @Test
    void productFilterIsCaseInsensitive() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        // 3 branches × 1 product each = 3 visible
        assertEquals(3, vm.visibleProducts().size());

        vm.productFilterProperty().set("JASMINE");
        assertEquals(3, vm.visibleProducts().size(),
                "uppercase filter still matches");

        vm.productFilterProperty().set("sku-1001");
        assertEquals(3, vm.visibleProducts().size(),
                "SKU filter matches");

        vm.productFilterProperty().set("nonexistent");
        assertEquals(0, vm.visibleProducts().size(),
                "unmatched filter hides all products");

        vm.productFilterProperty().set("");
        assertEquals(3, vm.visibleProducts().size(),
                "empty filter shows all products");
    }

    // ── Worker uses snapshot of inputs (1 test) ───────────────────────────

    @Test
    void workerUsesSnapshotOfInputs() {
        vm.monthProperty().set(AUG);
        vm.branchSelectedProperty("REP").set(false);
        vm.branchSelectedProperty("BTB").set(false);
        vm.generateCommand().execute(); // snapshot: only PNH

        // mutate selection after submit — job must ignore this
        vm.branchSelectedProperty("REP").set(true);
        vm.branchSelectedProperty("BTB").set(true);

        jobs.runNext();

        assertEquals(List.of("PNH"), loaded,
                "worker must use the snapshot taken at generate() time");
    }

    // ── Two listeners on totalRevenueText (Challenge) ─────────────────────

    @Test
    void twoListenersOnTotalRevenueBothReceiveUpdate() {
        List<String> vals1 = new ArrayList<>();
        List<String> vals2 = new ArrayList<>();

        vm.totalRevenueTextProperty().addListener(
                (obs, old, nv) -> vals1.add(nv));
        vm.totalRevenueTextProperty().addListener(
                (obs, old, nv) -> vals2.add(nv));

        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        assertTrue(vals1.contains("USD 600.00"),
                "listener 1 must receive USD 600.00; got: " + vals1);
        assertTrue(vals2.contains("USD 600.00"),
                "listener 2 must receive USD 600.00; got: " + vals2);
    }

    // ── Status messages (1 test) ──────────────────────────────────────────

    @Test
    void statusMessagesFollowLoadLifecycle() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertEquals("Loading...", vm.statusProperty().get());

        vm.cancelCommand().execute();
        assertEquals("Cancelled, previous data kept.", vm.statusProperty().get());

        // Now do a full successful load
        vm.generateCommand().execute();
        jobs.runNext();
        assertTrue(vm.statusProperty().get().startsWith("Loaded 3"),
                "status after success: " + vm.statusProperty().get());
    }
}
