# Lab-03 — MVVM with Unit Tests

## (a) Files introduced in this lab

| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java` | MVVM ViewModel: validation, commands, loading state, product filter | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportView.java` | JavaFX view — binds controls to ViewModel properties | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | Composition root: wires source → VM → view + dashboard | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/BackgroundJobs.java` | Port (interface): `submit(Work, JobListener) → JobHandle` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` | Synchronous `BackgroundJobs` implementation (UI-thread baseline) | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/Command.java` | Interface: `executableProperty()` + `execute()` + `isExecutable()` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/DelegateCommand.java` | Concrete `Command` guarded by an `ObservableBooleanValue` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/SummarySource.java` | Port (FI): `load(YearMonth, String) → BranchSummary` | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/fx/DashboardView.java` | Challenge: compact 2nd-window view on the shared ViewModel | Team 5 |
| `src/main/java/edu/itc/salesreport/ui/fx/DashboardWindow.java` | Challenge: opens `DashboardView` in an always-on-top `Stage` | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Test double: jobs queue until `runNext()` is called | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | 13 JUnit tests (no toolkit, no sleep, fixed Clock) | Team 5 |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architecture guard: ViewModel may not import javafx.scene/stage/application/concurrent | Team 5 |
| `lab03/mvc-vs-mvvm.md` | Metric comparison between Lab-02 MVC and Lab-03 MVVM | Team 5 |
| `lab03/README.md` | This file | Team 5 |
| `lab03/viewmodel-design.md` | ViewModel member inventory and class diagram | Team 5 |

## (b) Inputs reused from Lab-02

| Artefact | Tag / commit |
|---|---|
| `model/` package (`BranchSummary`, `ProductTotal`, `SaleTransaction`, `PaymentMethod`) | `lab-02-final` (commit `67ea473`) |
| `ingest/` package (`CsvMonthReader`, `CsvTransactionParser`, `MonthData`, …) | `lab-02-final` |
| `engine/` package (`DefaultSalesAggregator`, `SalesAggregator`) | `lab-02-final` |
| `SalesModel`, `SalesEvent`, `SalesModelListener` | `lab-02-final` (retained, not changed) |
| `ReportController`, `InputRules`, `Validation` | `lab-02-final` (retained, not changed) |

## (c) Change log

| Date | What changed | Who | Model package changed? |
|---|---|---|---|
| 2026-11-12 | Added Task 5: fixed validation bindings (future-month, empty-branch), fixed `cancel()` to actually call `running.cancel()`, snapshotted inputs before async dispatch | Team 5 | No |
| 2026-11-12 | Added `ManualJobs` test double, `ReportViewModelTest` (13 tests), `ViewModelImportsTest` | Team 5 | No |
| 2026-11-12 | Added JaCoCo 0.8.15 to `pom.xml`; ViewModel line coverage = 97 % (127/131) | Team 5 | No |
| 2026-11-12 | Challenge: `DashboardView` and `DashboardWindow` in `ui/fx/`; updated `ReportViewerApp` to open dashboard | Team 5 | No |
| 2026-11-12 | Created `lab03/mvc-vs-mvvm.md` and `lab03/README.md` | Team 5 | No |

> **Note on `UiIndependenceTest`:** This pre-existing test (`src/test/java/edu/itc/salesreport/UiIndependenceTest.java`) scans the flat `ui/` package and forbids all `import javafx.*` — including `javafx.beans`. It was already failing before Lab-03 because `Command.java`, `DelegateCommand.java`, `ReportView.java`, `ReportViewModel.java`, and `ReportViewerApp.java` all use `javafx.beans` or `javafx.scene`. Our challenge view classes (`DashboardView`, `DashboardWindow`) were placed in `ui/fx/` (which the test does not scan) to avoid adding new violations. The root cause of the original failure is a package-structure mismatch that predates Lab-03 and is out of scope for this lab.
