# Lab-03 · ViewModel design

## 1. Controller inventory (from Lab-02)

The Lab-02 viewer put screen logic in `ui.fx.ReportView` (view) and
`ui.ReportController` (controller). Every row below is something the
old controller or view read, wrote, or decided.

| # | Code (Lab-02) | Concern | New owner |
|---|---|---|---|
| 1 | `monthField.getText()` → `YearMonth.parse` | Parse month text | ViewModel (`month` property, `YearMonthConverter` in view) |
| 2 | `branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB")` | Branch list | ViewModel (`BRANCHES`) |
| 3 | `model.selectBranch(branch)` | Which branch is shown | ViewModel (`branchSelected` map) |
| 4 | `if (month.isAfter(YearMonth.now()))` | Future-month rule | ViewModel (`monthError` binding, `Clock` injected) |
| 5 | `if (selectedBranches.isEmpty())` | Empty-branch rule | ViewModel (`branchError` binding) |
| 6 | `errors.showError(msg)` / `clearError()` | Error surface | ViewModel (`loadError`) + View (`showError` style) |
| 7 | `model.load(dataDir, month)` | Read files | Model, behind `SummarySource` |
| 8 | `summary.revenue().add(...)` | Sum branch revenues | ViewModel (`totalRevenue` binding) |
| 9 | `String.format("USD %,.2f", …)` | Format total | ViewModel (`totalRevenueText`, `Locale.US`) |
| 10 | `table.getItems().setAll(...)` | Table content | View (bound to `summaries()`) |
| 11 | `chart.getData().setAll(series)` | Chart content | View (bound to `summaries()` / `totalRevenue`) |
| 12 | `status.setText("Loaded …")` | Status line | ViewModel (`status` property) |
| 13 | `button.setDisable(...)` | Enablement | ViewModel (`canGenerate` binding) + View (bind) |
| 14 | `productFilter` matching | Case-insensitive filter | ViewModel (`visibleProducts` predicate) |

That is more than the minimum 8 rows.

## 2. ReportViewModel members

### Input (two-way bound by the view)
| Member | Type | View may write? |
|---|---|---|
| `month` | `ObjectProperty<YearMonth>` | yes |
| `branchSelected("PNH"/"REP"/"BTB")` | `BooleanProperty` | yes |
| `productFilter` | `StringProperty` | yes |

### Output (one-way bound by the view)
| Member | Type |
|---|---|
| `monthChoices()` | `ObservableList<YearMonth>` (unmodifiable) |
| `summaries()` | `ObservableList<BranchSummary>` (unmodifiable) |
| `visibleProducts()` | `ObservableList<ProductTotal>` |
| `loading` | `ReadOnlyBooleanProperty` |
| `progress` | `ReadOnlyDoubleProperty` |
| `status` | `ReadOnlyStringProperty` |
| `loadError` | `ReadOnlyStringProperty` |

### Computed (read-only, derived bindings)
| Member | Type |
|---|---|
| `monthError` | `StringExpression` |
| `branchError` | `StringExpression` |
| `validationMessage` | `StringExpression` |
| `canGenerate` | `ObservableBooleanValue` |
| `totalRevenue` | `ObservableObjectValue<BigDecimal>` |
| `totalRevenueText` | `StringExpression` |

### Commands
| Member | Guarded by |
|---|---|
| `generateCommand` | `canGenerate` |
| `cancelCommand` | `loading` |

### Ports (constructor parameters)
| Port | Implemented in production by |
|---|---|
| `SummarySource` | lambda over `CsvMonthReader` + `DefaultSalesAggregator` |
| `BackgroundJobs` | `CallerThreadJobs` (Task 2), `FxBackgroundJobs` (Task 4) |
| `Clock` | `Clock.systemDefaultZone()` (Task 2), fixed in tests |

## 3. Class diagram

```mermaid
classDiagram
    class ReportView {
        +ReportView(ReportViewModel vm)
        +root() Parent
    }
    class ReportViewModel {
        +ObjectProperty~YearMonth~ monthProperty()
        +BooleanProperty branchSelectedProperty(String)
        +StringProperty productFilterProperty()
        +ObservableList~YearMonth~ monthChoices()
        +ObservableList~BranchSummary~ summaries()
        +ObservableList~ProductTotal~ visibleProducts()
        +ReadOnlyBooleanProperty loadingProperty()
        +ReadOnlyDoubleProperty progressProperty()
        +ReadOnlyStringProperty statusProperty()
        +ReadOnlyStringProperty loadErrorProperty()
        +StringExpression monthErrorProperty()
        +StringExpression branchErrorProperty()
        +StringExpression validationMessageProperty()
        +ObservableBooleanValue canGenerateProperty()
        +ObservableObjectValue~BigDecimal~ totalRevenueProperty()
        +StringExpression totalRevenueTextProperty()
        +Command generateCommand()
        +Command cancelCommand()
    }
    class Command {
        <<interface>>
        +executableProperty() ObservableBooleanValue
        +execute() void
    }
    class DelegateCommand {
        +DelegateCommand(Runnable, ObservableBooleanValue)
    }
    class SummarySource {
        <<interface>>
        +load(YearMonth, String) BranchSummary
    }
    class BackgroundJobs {
        <<interface>>
        +submit(Work, JobListener) JobHandle
    }
    class CallerThreadJobs
    class FxBackgroundJobs
    class ManualJobs {
        <<test double>>
        +runNext() void
        +pendingCount() int
    }
    class BranchSummary {
        <<record>>
    }
    class ProductTotal {
        <<record>>
    }

    ReportView --> ReportViewModel : binds
    ReportViewModel --> Command
    ReportViewModel --> SummarySource
    ReportViewModel --> BackgroundJobs
    ReportViewModel ..> BranchSummary
    ReportViewModel ..> ProductTotal
    Command <|.. DelegateCommand
    BackgroundJobs <|.. CallerThreadJobs
    BackgroundJobs <|.. FxBackgroundJobs
    BackgroundJobs <|.. ManualJobs
    stateDiagram-v2
    [*] --> Incomplete
    Incomplete --> Invalid : input changed
    Incomplete --> Ready : month chosen and branch ticked
    Invalid --> Ready : input fixed
    Ready --> Loading : generate
    Loading --> Loaded : succeeded
    Loading --> Failed : failed
    Loading --> Ready : cancelled
    Loaded --> Loading : generate again
    Failed --> Loading : generate again