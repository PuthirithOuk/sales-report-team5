# Lab 02 quality-attribute scenarios

- **Testability:** A developer verifies branch totals with deterministic fixtures in under one minute without launching the JavaFX window.
- **Modifiability:** A developer adds a new report chart type in under one day without changing CSV parsing.

## Files
| Path | Task | Author |
|---|---|---|
| src/main/java/.../ingest, engine, ui/SalesModel, ui/fx/ReportView, ReportApp | 1–2 MVC viewer | OukPuthirith |
| ui/ReportController, ReportCommand, Validation, InputRules | 3 | Mony |
| ui/fx/legacy/FatReportController, tests, lab02/refactoring.md | 4 | Monika |
| ui/mvp/*, ui/fx/FxReportScreen, ReportMvpApp, ReportPresenterTest | 5 MVP | Hen Chhordavattey |
| lab02/mvc-vs-mvp.md, lab02/adr-002-presentation-pattern.md | 5 | Hen Chhordavattey |

## Lab-01 inputs reused
<commit hash or tag of each Lab-01 input, or "fallback used">

## Change log
| Date | Change | By |
|---|---|---|
| 2026-10-28 | Added MVP variant, presenter tests, comparison and ADR-002 | Hen Chhordavattey |