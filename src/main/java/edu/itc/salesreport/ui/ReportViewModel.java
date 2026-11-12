package edu.itc.salesreport.ui;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/** ViewModel for the MVVM architecture. Only uses javafx.base. */
public final class ReportViewModel {

    public static final List<String> BRANCHES = List.of("PNH", "REP", "BTB");

    private final SummarySource source;
    private final BackgroundJobs jobs;
    private final Clock clock;

    // ---- input state: two-way bound by the view
    private final ObjectProperty<YearMonth> month =
            new SimpleObjectProperty<>(this, "month");
    private final Map<String, BooleanProperty> branchSelected =
            new LinkedHashMap<>();
    private final StringProperty productFilter =
            new SimpleStringProperty(this, "productFilter", "");

    // ---- output state: one-way bound by the view
    private final ObservableList<YearMonth> monthChoices =
            FXCollections.observableArrayList();
    private final ObservableList<BranchSummary> summaries =
            FXCollections.observableArrayList();
    private final ObservableList<ProductTotal> products =
            FXCollections.observableArrayList();
    private final FilteredList<ProductTotal> visibleProducts =
            new FilteredList<>(products);
    private final ReadOnlyBooleanWrapper loading =
            new ReadOnlyBooleanWrapper(this, "loading", false);
    private final ReadOnlyDoubleWrapper progress =
            new ReadOnlyDoubleWrapper(this, "progress", 0);
    private final ReadOnlyStringWrapper status =
            new ReadOnlyStringWrapper(this, "status", "Choose a month.");
    private final ReadOnlyStringWrapper loadError =
            new ReadOnlyStringWrapper(this, "loadError", "");

    // ---- computed state
    private final StringBinding monthError;
    private final StringBinding branchError;
    private final StringBinding validationMessage;
    private final BooleanBinding canGenerate;
    private final ObjectBinding<BigDecimal> totalRevenue;
    private final StringBinding totalRevenueText;

    private final Command generateCommand;
    private final Command cancelCommand;
    private BackgroundJobs.JobHandle running;

    public ReportViewModel(SummarySource source, BackgroundJobs jobs,
                           Clock clock) {
        this.source = Objects.requireNonNull(source);
        this.jobs = Objects.requireNonNull(jobs);
        this.clock = Objects.requireNonNull(clock);
        
        YearMonth now = YearMonth.now(clock);
        for (int i = 0; i < 12; i++) {
            monthChoices.add(now.minusMonths(i));
        }
        
        // 1. Initialize the month so the ComboBox isn't blank
        month.set(now);
        
        for (String b : BRANCHES) {
            branchSelected.put(b, new SimpleBooleanProperty(this, b, true));
        }
        
        // Placeholders for Task 3
        monthError = Bindings.createStringBinding(() -> "", month);
        branchError = Bindings.createStringBinding(() -> "");
        validationMessage = Bindings.createStringBinding(() -> "");
        
        canGenerate = month.isNotNull().and(loading.not());
        
        // Task 2: Sum of summaries' revenue
        totalRevenue = Bindings.createObjectBinding(() -> {
            BigDecimal sum = BigDecimal.ZERO;
            for (BranchSummary s : summaries) {
                sum = sum.add(s.revenue());
            }
            return sum;
        }, summaries);

        // 2. Properly format "USD 1,234.50" as required by the lab
        totalRevenueText = Bindings.createStringBinding(() -> {
            BigDecimal rev = totalRevenue.get();
            if (rev == null) rev = BigDecimal.ZERO;
            return String.format(Locale.US, "USD %,.2f", rev);
        }, totalRevenue);

        // Task 2: Bind visibleProducts' predicate to productFilter
        visibleProducts.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            String filter = productFilter.get();
            if (filter == null || filter.isBlank()) {
                return p -> true;
            }
            String f = filter.toLowerCase(Locale.ROOT);
            return p -> p.productName().toLowerCase(Locale.ROOT).contains(f)
                     || p.sku().toLowerCase(Locale.ROOT).contains(f);
        }, productFilter));

        generateCommand = new DelegateCommand(this::generate, canGenerate);
        cancelCommand = new DelegateCommand(this::cancel,
                loading.getReadOnlyProperty());
    }

    public List<String> selectedBranches() {
        return branchSelected.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();
    }

    private void generate() {
        YearMonth m = month.get();
        List<String> selected = selectedBranches();
        if (m == null || selected.isEmpty()) return;

        loading.set(true);
        status.set("Loading...");
        loadError.set("");
        
        jobs.submit(sink -> {
            List<BranchSummary> results = new ArrayList<>();
            int count = 0;
            for (String b : selected) {
                results.add(source.load(m, b));
                count++;
                sink.update(count, selected.size());
            }
            return results;
        }, new BackgroundJobs.JobListener<List<BranchSummary>>() {
            @Override
            public void progress(double fraction) {
                progress.set(fraction);
            }

            @Override
            public void succeeded(List<BranchSummary> results) {
                summaries.setAll(results);
                products.clear();
                for (BranchSummary r : results) {
                    products.addAll(r.topProducts());
                }
                status.set("Loaded " + results.size() + " branch(es) for " + m + ".");
                loading.set(false);
            }

            @Override
            public void failed(Throwable error) {
                error.printStackTrace();
                loadError.set(error.getMessage());
                status.set("Load failed, previous data kept.");
                loading.set(false);
            }

            @Override
            public void cancelled() {
                status.set("Cancelled, previous data kept.");
                loading.set(false);
            }
        });
    }

    private void cancel() {
        // TODO Task 4: cancel the running job, if any
    }

    // ---- accessors: read-only types for everything the view must not set
    public ObjectProperty<YearMonth> monthProperty() { return month; }
    public BooleanProperty branchSelectedProperty(String branch) {
        return Objects.requireNonNull(branchSelected.get(branch), branch);
    }
    public StringProperty productFilterProperty() { return productFilter; }
    public ObservableList<YearMonth> monthChoices() {
        return FXCollections.unmodifiableObservableList(monthChoices);
    }
    public ObservableList<BranchSummary> summaries() {
        return FXCollections.unmodifiableObservableList(summaries);
    }
    public ObservableList<ProductTotal> visibleProducts() {
        return visibleProducts;
    }
    public ReadOnlyBooleanProperty loadingProperty() {
        return loading.getReadOnlyProperty();
    }
    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }
    public ReadOnlyStringProperty statusProperty() {
        return status.getReadOnlyProperty();
    }
    public ReadOnlyStringProperty loadErrorProperty() {
        return loadError.getReadOnlyProperty();
    }
    public StringExpression monthErrorProperty() { return monthError; }
    public StringExpression branchErrorProperty() { return branchError; }
    public StringExpression validationMessageProperty() {
        return validationMessage;
    }
    public ObservableBooleanValue canGenerateProperty() { return canGenerate; }
    public ObservableObjectValue<BigDecimal> totalRevenueProperty() {
        return totalRevenue;
    }
    public StringExpression totalRevenueTextProperty() {
        return totalRevenueText;
    }
    public Command generateCommand() { return generateCommand; }
    public Command cancelCommand() { return cancelCommand; }
}