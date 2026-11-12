# Lab-03 — MVVM with Unit Tests

## (a) Files introduced in this lab

| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java` | MVVM ViewModel: validation, commands, loading state, product filter | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportView.java` | JavaFX view — binds controls to ViewModel properties | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | Composition root: wires source -> VM -> view + dashboard | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/Command.java` | Command interface | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/DelegateCommand.java` | Command guarded by an observable condition | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/SummarySource.java` | Port for loading one branch summary | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/BackgroundJobs.java` | Port (interface): `submit(Work, JobListener) -> JobHandle` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` | Synchronous `BackgroundJobs` implementation (UI-thread baseline) | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/FxBackgroundJobs.java` | JavaFX Task/executor implementation of `BackgroundJobs` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/FrameGapMonitor.java` | Measures UI frame gaps during loading | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/LoadBenchmarkApp.java` | Benchmark viewer for synchronous and asynchronous loading | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/DashboardView.java` | Challenge: compact second-window view on the shared ViewModel | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/DashboardWindow.java` | Challenge: opens `DashboardView` in an always-on-top `Stage` | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Test double: jobs queue until `runNext()` is called | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | 14 JUnit tests (no toolkit, no sleep, fixed Clock) | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architecture guard: ViewModel may not import javafx.scene/stage/application/concurrent | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/FxBackgroundJobsTest.java` | Regression test for JavaFX job cancellation checks | Team 5 |
| `lab03/README.md` | Lab file inventory and change log | Team 5 |
| `lab03/viewmodel-design.md` | ViewModel inventory, class diagram, and state diagram | Team 5 |
| `lab03/measurements.md` | Task 4 UI responsiveness measurements | Team 5 |
| `lab03/mvc-vs-mvvm.md` | Metric comparison between Lab-02 MVC and Lab-03 MVVM | Team 5 |
| `lab03/platform-mapping.md` | Challenge: mapping ViewModel members to Android Jetpack & WPF | Team 5 |
| `lab03/screenshots/viewer.png` | Main report viewer screenshot | Team 5 |
| `lab03/screenshots/dashboard.png` | Screenshot showing viewer and dashboard sharing the ViewModel | Team 5 |

## (b) Inputs reused from Lab-02

| Artefact | Tag / commit |
|---|---|
| `model/` package (`BranchSummary`, `ProductTotal`, `SaleTransaction`, `PaymentMethod`) | `lab-02-final` (commit `67ea473`) |
| `ingest/` package (`CsvMonthReader`, `CsvTransactionParser`, `MonthData`, etc.) | `lab-02-final` |
| `engine/` package (`DefaultSalesAggregator`, `SalesAggregator`) | `lab-02-final` |
| `SalesModel`, `SalesEvent`, `SalesModelListener` | `lab-02-final` (retained, not changed) |
| `ReportController`, `InputRules`, `Validation` | `lab-02-final` (retained, not changed) |

## (c) Change log

| Date | What changed | Who | Model package changed? |
|---|---|---|---|
| 2026-11-12 | Task 1: ReportViewModel skeleton, ports (`BackgroundJobs`, `Command`, `SummarySource`), and design document | Team 5 | No |
| 2026-11-12 | Task 2: Bound view (`ReportView`), synchronous jobs (`CallerThreadJobs`), and app composition root (`ReportViewerApp`) | Team 5 | No |
| 2026-11-12 | Task 3: Validation rules (future-month guard, empty-branch guard), red border binding, and state diagram | Team 5 | No |
| 2026-11-12 | Task 4: Cancellation mechanism (`JobHandle`), progress tracking, and input snapshotting before async load | Team 5 | No |
| 2026-11-12 | Task 5: `ManualJobs` test double, `ReportViewModelTest` (13 tests), `ViewModelImportsTest`, JaCoCo line coverage 96%, comparison in `mvc-vs-mvvm.md`, and `README.md` | Team 5 | No |
| 2026-11-12 | Challenge: `DashboardView` and `DashboardWindow` in `ui/`, two-listener test in `ReportViewModelTest`, viewer/dashboard screenshots, and `platform-mapping.md` | Team 5 | No |
| 2026-11-13 | Fixed JavaFX cancellation checks and empty error messages; added regression tests, selectable benchmark runners, and corrected the file inventory | Team 5 | No |

## (d) ViewModel Reuse Verification

The dashboard challenge was originally implemented without changing the
ViewModel. The later 2026-11-13 runtime fix adds a fallback error label when
an exception has no message, so the current file now differs from that baseline.
The dashboard still shares the same ViewModel instance as the main window.

To inspect the subsequent ViewModel changes, run:
```bash
git diff lab-03-task5 -- src/main/java/edu/itc/salesreport/ui/ReportViewModel.java
```
