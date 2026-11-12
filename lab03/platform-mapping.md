# Platform Mapping — ReportViewModel → Jetpack / WPF

## Member-by-member mapping

| `ReportViewModel` member | JavaFX type | Android Jetpack equivalent | WPF / .NET equivalent |
|---|---|---|---|
| `monthProperty()` | `ObjectProperty<YearMonth>` | `MutableStateFlow<YearMonth>` in `ViewModel` | `INotifyPropertyChanged` property + two-way `Binding` |
| `branchSelectedProperty(String)` | `BooleanProperty` (one per branch) | `MutableStateFlow<Boolean>` per branch | `bool` property with `INotifyPropertyChanged` |
| `productFilterProperty()` | `StringProperty` | `MutableStateFlow<String>` | `string` property with `INotifyPropertyChanged` + `TextBox.Text` two-way binding |
| `monthChoices()` | `ObservableList<YearMonth>` | `StateFlow<List<YearMonth>>` (collected in Compose) | `ObservableCollection<YearMonth>` |
| `summaries()` | `ObservableList<BranchSummary>` | `StateFlow<List<BranchSummary>>` | `ObservableCollection<BranchSummary>` |
| `visibleProducts()` | `FilteredList<ProductTotal>` | `StateFlow<List<ProductTotal>>` derived via `combine` | `CollectionViewSource` with a filter predicate |
| `loadingProperty()` | `ReadOnlyBooleanProperty` | `StateFlow<Boolean>` | `bool` + `INotifyPropertyChanged` |
| `progressProperty()` | `ReadOnlyDoubleProperty` | `StateFlow<Float>` (0f–1f) | `double` + `INotifyPropertyChanged` |
| `statusProperty()` | `ReadOnlyStringProperty` | `StateFlow<String>` | `string` + `INotifyPropertyChanged` |
| `loadErrorProperty()` | `ReadOnlyStringProperty` | `StateFlow<String>` | `string` + `INotifyPropertyChanged` |
| `monthErrorProperty()` | `StringExpression` (derived binding) | `val monthError = monthFlow.map { ... }` as `StateFlow` | `IMultiValueConverter` or computed property |
| `branchErrorProperty()` | `StringExpression` | Derived `StateFlow` via `combine` | Computed property |
| `validationMessageProperty()` | `StringExpression` | Derived `StateFlow` via `combine(monthError, branchError)` | Computed property |
| `generateCommand()` | `Command` (custom interface) | `fun generate()` guarded by `if (!_loading.value)` in a `viewModelScope` coroutine | `ICommand` (`RelayCommand`) with `CanExecute` predicate |
| `cancelCommand()` | `Command` | `job?.cancel()` where `job` is a `viewModelScope` `Job` | `ICommand` calling `CancellationTokenSource.Cancel()` |
| `BackgroundJobs` port | Interface injected at construction | `viewModelScope.launch { … }` (scope is the port) | `async/await` + `Task.Run` or `IScheduler` from Reactive Extensions |

## What would not carry over (5 sentences)

**Two-way binding in Jetpack Compose** does not exist as a first-class primitive: Compose uses
unidirectional data flow where the UI reads `State<T>` and calls a lambda to update it, so
`bidirectionalBind` from JavaFX must be replaced by explicit `onValueChange` callbacks paired
with separate read-only state flows.

**JavaFX `FilteredList`** is a live view of an `ObservableList` that refilters automatically
on predicate change; Jetpack has no equivalent and the ViewModel must instead emit a new
`StateFlow<List<T>>` derived via `combine`, which means the full list is re-computed on every
filter keystroke rather than incrementally updated.

**ViewModel lifetime across screen rotation** is handled transparently by Android's
`androidx.lifecycle.ViewModel`, but the ViewModel must not hold `Context` references longer
than a single Activity — the JavaFX ViewModel holds a `Clock` injected at construction, which
maps cleanly, but any resource tied to the UI lifecycle (e.g. a file handle) would need to be
released in `onCleared()`.

**WPF `ICommand.CanExecute`** is polled by the framework on each user gesture rather than
being an observable binding, so the WPF port must call `CommandManager.InvalidateRequerySuggested()`
after any state change that affects executability; the JavaFX `DelegateCommand` pushes
enablement changes automatically through the `ObservableBooleanValue`, requiring no explicit
invalidation.

**Progress reporting** in `BackgroundJobs.ProgressSink` maps naturally to Jetpack's
`kotlinx.coroutines.flow.Flow<Float>` emitted from inside `withContext(Dispatchers.IO)`, but
WPF requires `IProgress<T>` or `Dispatcher.Invoke` to marshal progress values back to the UI
thread, since WPF controls are single-threaded and there is no equivalent of JavaFX's
`Platform.runLater` being called automatically by the framework.
