# ADR-002: Presentation pattern for Lab-03

## Status
Accepted

## Context
Lab-02 built the report screen twice: MVC (`ReportView` + `ReportController`) and MVP with a passive view (`FxReportScreen` + `ReportPresenter`). In MVC the view reads `SalesModel` and formats text itself, so display text cannot be tested without JavaFX. In MVP the presenter formats everything and the screen only has setters. Measurements are in `mvc-vs-mvp.md`.

Quality-attribute scenario (Testability): "A developer verifies branch totals with deterministic fixtures in under one minute without launching the JavaFX window."

## Decision
The team keeps **MVP with a passive view** for Lab-03.

## Consequences
- Good: `ReportPresenterTest` checks the exact status text and a formatted row with a `FakeScreen` and no toolkit. The MVC view had no such tests.
- Good: a web or Swing screen needs only the four `ReportScreen` methods; the presenter and its tests are reused.
- Bad: a new column touches more files (Discount: 4 files against 1 in MVC) and adds two types, `ReportScreen` and `RowItem`.
- Bad: display formatting now differs from the MVC viewer (thousands separators), so the two must not be compared as text.