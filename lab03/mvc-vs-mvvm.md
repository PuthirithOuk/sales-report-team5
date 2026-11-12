# MVC vs MVVM — Lab-02 / Lab-03 Comparison

| Metric | MVC (Lab-02, `ReportController`) | MVVM (Lab-03, `ReportViewModel`) | How measured |
|---|---|---|---|
| Lines of presentation code | 43 (controller) + 170 (fx.ReportView) | 220 (ViewModel) + 133 (ReportView) + 95 (ports, commands, jobs) | `wc -l` per class |
| Presentation-logic line coverage without toolkit | 0 % (`new ComboBox<>()` needs the toolkit) | 97 % (127 of 131 lines — 106 + 21 inner) | JaCoCo 0.8.15 |
| `javafx.scene` imports in the logic class | 0 (`ReportController` itself) | 0 | `grep -c "^import javafx.scene"` |
| Control types the logic class references | 0 in `ReportController`; 8 in `fx.ReportView` | 0 | code review |
| Test run time of presentation logic | n/a (requires JavaFX toolkit to start) | 0.038–0.068 s for 13 tests | Surefire report |
| UI frozen during a load | yes (`CallerThreadJobs` → blocking) | no (async via `BackgroundJobs` port) | manual observation |

## What got better

Separating screen state into a `ReportViewModel` that uses only `javafx.base` made the entire
presentation layer testable without ever starting the JavaFX toolkit — 13 JUnit tests run in
under 70 ms on the calling thread, with a fixed `Clock` and `ManualJobs`, so there are no
sleeps, no robots, and no flaky timing.

Validation logic (future-month guard, empty-branch guard) is now a binding expression that
runs in pure Java; a test can prove it in a single assertion with no UI setup.

The `Command` abstraction decouples button enablement from event-handler wiring, so the view
contains no `if` statements and no domain logic.

Adding a second window (`DashboardView`) required zero changes to the ViewModel — it simply
bound the same properties again from a different `Stage`, demonstrating true view–ViewModel
reuse across two simultaneous windows.

The `BackgroundJobs` port makes it trivial to swap implementations:
`CallerThreadJobs` for a synchronous baseline, `FxBackgroundJobs` for real async,
and `ManualJobs` for step-by-step test control.

## What got worse

The class count more than tripled: `Command`, `DelegateCommand`, `BackgroundJobs`,
`SummarySource`, `CallerThreadJobs`, `ManualJobs`, `DashboardView`, `DashboardWindow` are
all new files the MVC version did not need.

Two-way binding bugs are silent — a misconfigured `bidirectionalBind` can create update
cycles that are extremely hard to trace in a debugger, and JavaFX properties have
non-obvious lazy evaluation that surprises developers who expect eager listeners.

## When MVC is still a good choice

For short-lived forms or prototypes where testability is not a priority, plain MVC — a
controller wired directly to JavaFX controls — requires far fewer classes and is easier for
a new developer to read in one sitting.
MVC is also appropriate when no second view will ever share the same state, when the toolkit's
event model is already the team's shared mental model, and when the deployment environment
makes it unlikely that the UI toolkit will ever be swapped for a different platform.
