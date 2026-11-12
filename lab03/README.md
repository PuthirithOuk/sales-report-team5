# Lab-03 — MVVM with Unit Tests

## (a) Files introduced in this lab

| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java` | MVVM ViewModel: validation, commands, loading state, product filter | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportView.java` | JavaFX view — binds controls to ViewModel properties | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | Composition root: wires source -> VM -> view + dashboard | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/BackgroundJobs.java` | Port (interface): `submit(Work, JobListener) -> JobHandle` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` | Synchronous `BackgroundJobs` implementation (UI-thread baseline) | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/Command.java` | Interface: `executableProperty()` + `execute()` + `isExecutable()` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/DelegateCommand.java` | Concrete `Command` guarded by an `ObservableBooleanValue` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/SummarySource.java` | Port (FI): `load(YearMonth, String) -> BranchSummary` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/fx/DashboardView.java` | Challenge: compact 2nd-window view on the shared ViewModel | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/fx/DashboardWindow.java` | Challenge: opens `DashboardView` in an always-on-top `Stage` | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Test double: jobs queue until `runNext()` is called | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | 13 JUnit tests (no toolkit, no sleep, fixed Clock) | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architecture guard: ViewModel may not import javafx.scene/stage/application/concurrent | Team 5 |
| `lab03/mvc-vs-mvvm.md` | Metric comparison between Lab-02 MVC and Lab-03 MVVM | Team 5 |
| `lab03/platform-mapping.md` | Challenge: mapping ViewModel members to Android Jetpack & WPF | Team 5 |
| `lab03/screenshots/dashboard.png` | Screenshot showing viewer and dashboard sharing the ViewModel | Team 5 |
| `lab03/README.md` | This documentation file | Team 5 |
| `lab03/viewmodel-design.md` | ViewModel member inventory, class diagram, and state diagram | Team 5 |

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
| 2026-11-12 | Challenge: `DashboardView` and `DashboardWindow` in `ui/fx/`, two-listener test in `ReportViewModelTest`, `dashboard.png` screenshot, and `platform-mapping.md` | Team 5 | No |

## (d) ViewModel Reuse Verification

Running:
```bash
git diff lab-03-task5 -- src/main/java/edu/itc/salesreport/ui/ReportViewModel.java
```
produces zero output. The challenge was implemented completely via `DashboardView` and `DashboardWindow` without altering `ReportViewModel.java`.
