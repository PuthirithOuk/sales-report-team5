# MVC vs MVP (Lab-02)

| Criterion | MVC (ReportView + ReportController) | MVP (FxReportScreen + ReportPresenter) |
|---|---|---|
| Lines of code (view / controller-presenter) | 139 / 74 (+7 `ErrorDisplay`) | 113 / 74 (+12 `ReportScreen`, +4 `RowItem`) |
| Tests that run without JavaFX | 4 (`ReportControllerTest`), none can check display text | 2 (`ReportPresenterTest`), both check exact display text |
| Where money and status text are formatted | In `ReportView.modelChanged` (`toPlainString()`, no grouping), so only visible by running the window | In `ReportPresenter` (`money()` and the status string); the screen only calls setters |
| Files changed to add a "Discount" column | 1: `ReportView` | 4: `RowItem`, `ReportPresenter`, `FxReportScreen`, `ReportPresenterTest` |
| Does the view hold a model reference? | Yes: `ReportView` has a `SalesModel` field and reads `model.summary()` and `model.visibleTransactions()` | No: `FxReportScreen` imports only `ReportPresenter`, `ReportScreen` and `RowItem` |
| Effort for a second screen (web, Swing) | The new view must reimplement the formatting and the model reads | Implement the 4 `ReportScreen` methods; the presenter and its tests are reused |

**Reading the numbers.** MVP's Discount change touched four files against MVC's one, but three of the four are toolkit-free and covered by `ReportPresenterTest`. The MVC change is a single line in a class that nothing can test without launching JavaFX. The two variants also format differently. MVC prints `26721.90` and MVP prints `26,721.90`, so "same figures" holds only up to digit grouping. Note this in the file as evidence of where formatting lives.