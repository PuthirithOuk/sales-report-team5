# MVC vs MVVM — Lab-02 / Lab-03 Comparison

| Metric | MVC (Lab-02, `ReportController`) | MVVM (Lab-03, `ReportViewModel`) | How measured |
|---|---|---|---|
| Lines of presentation code | 74 (controller) + 139 (fx.ReportView) | 263 (ViewModel) + 144 (ReportView) + 121 (ports, commands, jobs) | `wc -l` per class |
| Presentation-logic line coverage without toolkit | 0 % (`new ComboBox<>()` needs the toolkit) | 97 % (119 of 123 ViewModel lines; 142 of 146 with JobListener) | JaCoCo 0.8.15 |
| `javafx.scene` imports in the logic class | 0 in `ReportController` (9 in legacy `FatReportController`) | 0 | `grep -c "^import javafx.scene"` |
| Control types the logic class references | 0 in `ReportController` (4 in legacy `FatReportController`) | 0 | code review |
| Test run time of presentation logic | n/a (requires JavaFX toolkit to start) | 0.08 s for 14 tests | Surefire report |
| UI frozen during a load | yes (synchronous load freezes UI thread) | no (async via `BackgroundJobs` port) | manual observation |

## Comparison and Evaluation

1. Separating presentation state into `ReportViewModel` using only `javafx.base` enables the entire presentation logic to be verified without starting the JavaFX toolkit or requiring thread sleeps.
2. Reactive bindings express validation rules (future month guard, empty branch guard) and command enablement declaratively in pure Java.
3. Decoupling actions into the `Command` pattern eliminates procedural conditional logic from the view layer and makes UI states directly observable.
4. The shared ViewModel architecture allows a secondary dashboard window (`DashboardView`) to observe and control the same state; the dashboard wiring is separate from the ViewModel.
5. Conversely, introducing ports, commands, and delegates tripled the number of presentation classes compared to simple MVC controllers.
6. Two-way binding errors and lazy property evaluations can fail silently or cause cyclic updates that are significantly harder to debug than direct procedural calls.
7. MVC remains the preferable choice for small, short-lived utility screens or simple prototypes where testability and multi-window synchronization do not justify the added architectural overhead.
