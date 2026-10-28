package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.ui.mvp.ReportPresenter;
import edu.itc.salesreport.ui.mvp.ReportScreen;
import edu.itc.salesreport.ui.mvp.RowItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Passive JavaFX view: only setters and gesture forwarding. No SalesModel here. */
public final class FxReportScreen implements ReportScreen {
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<RowItem> table = new TableView<>();
    private final BarChart<String, Number> chart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public FxReportScreen() {
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        table.getColumns().setAll(List.of(
                column("Date", RowItem::date),
                column("Receipt", RowItem::receipt),
                column("Product", RowItem::product),
                column("Category", RowItem::category),
                column("Qty", RowItem::quantity),
               column("Revenue", RowItem::revenue),
               column("Discount", RowItem::discount)));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        error.setStyle("-fx-text-fill: firebrick;");

        var filters = new HBox(10,
                new Label("Month"), monthField,
                new Label("Branch"), branchBox);
        filters.setPadding(new Insets(10));
        monthField.setPrefColumnCount(8);
        HBox.setHgrow(monthField, Priority.NEVER);

        var content = new SplitPane(table, chart);
        content.setDividerPositions(0.55);
        var footer = new VBox(4, status, error);
        footer.setPadding(new Insets(8, 10, 10, 10));
        root.setTop(filters);
        root.setCenter(content);
        root.setBottom(footer);
    }

    public Parent root() {
        return root;
    }

    /** Forwards user gestures to the presenter. */
    public void attach(ReportPresenter presenter) {
        monthField.setOnAction(e -> presenter.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> presenter.branchChosen(branchBox.getValue()));
    }

    @Override
    public void showRows(List<RowItem> rows) {
        table.getItems().setAll(rows);
    }

    @Override
    public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
        var series = new XYChart.Series<String, Number>();
        revenue.forEach((category, amount) ->
                series.getData().add(new XYChart.Data<>(category, amount.doubleValue())));
        chart.getData().setAll(series);
    }

    @Override
    public void showStatus(String text) {
        status.setText(text);
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }

    private static TableColumn<RowItem, String> column(String title,
            Function<RowItem, String> value) {
        var column = new TableColumn<RowItem, String>(title);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}