package edu.itc.salesreport.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/**
 * Challenge: a compact always-on-top dashboard bound to the SAME
 * ReportViewModel instance as the main viewer.  Zero ViewModel changes needed.
 */
public final class DashboardView {

    private final VBox root = new VBox(8);

    public DashboardView(ReportViewModel vm) {

        // --- month label ---
        Label month = new Label();
        month.textProperty().bind(Bindings.createStringBinding(
                () -> "Month: " + vm.monthProperty().get(),
                vm.monthProperty()));

        // --- chain revenue in large type ---
        Label total = new Label();
        total.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        total.textProperty().bind(vm.totalRevenueTextProperty());

        // --- branch count ---
        Label branches = new Label();
        branches.textProperty().bind(Bindings.createStringBinding(
                () -> vm.summaries().size() + " branch(es) loaded",
                vm.summaries()));

        // --- spinner visible only while loading ---
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.progressProperty().bind(vm.progressProperty());
        spinner.visibleProperty().bind(vm.loadingProperty());

        // --- refresh button disabled while loading ---
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> vm.generateCommand().execute());
        refresh.disableProperty().bind(
                Bindings.not(vm.generateCommand().executableProperty()));

        root.getChildren().addAll(month, total, branches, spinner, refresh);
        root.setPadding(new Insets(12));
    }

    public Parent root() { return root; }
}
