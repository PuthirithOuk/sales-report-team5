package edu.itc.salesreport.ui.fx;


import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import java.util.List;
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

public final class ReportView implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<SaleTransaction> table = new TableView<>();
    private final BarChart<String, Number> chart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    
        public void bind(ReportController controller) {

        monthField.setOnAction(e ->
                controller.monthEntered(monthField.getText()));

        branchBox.setOnAction(e ->
                controller.branchChosen(branchBox.getValue()));
        }


    public ReportView(SalesModel model) {
        this.model = model;
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue(SalesModel.ALL);

        table.getColumns().setAll(List.of(
                column("Date", transaction -> transaction.date().toString()),
                column("Receipt", SaleTransaction::receiptNo),
                column("Product", SaleTransaction::productName),
                column("Category", SaleTransaction::category),
                column("Qty", transaction ->
                        Integer.toString(transaction.quantity())),
                column("Revenue", transaction ->
                        transaction.revenue().toPlainString())));
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

        model.addListener(this);
    }

    public Parent root() {
        return root;
    }

    @Override
    public void modelChanged(SalesEvent event) {
        branchBox.setValue(model.branch());
        table.getItems().setAll(model.visibleTransactions());

        var summary = model.summary();
        var series = new XYChart.Series<String, Number>();
        summary.revenueByCategory().forEach((category, revenue) ->
                series.getData().add(new XYChart.Data<>(
                        category, revenue.doubleValue())));
        chart.getData().setAll(series);

        status.setText(switch (event) {
            case SalesEvent.MonthLoaded(var month, var rows, var rejected) ->
                    "%s: %d rows, %d rejected · revenue USD %s · receipts %d"
                            .formatted(month, rows, rejected,
                                    summary.revenue().toPlainString(),
                                    summary.receipts());
            case SalesEvent.BranchSelected(var branch) ->
                    "%s: revenue USD %s · receipts %d"
                            .formatted(branch, summary.revenue().toPlainString(),
                                    summary.receipts());
        });
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }

    @Override
    public void clearError() {
        error.setText("");
    }

    private static TableColumn<SaleTransaction, String> column(String title,
            Function<SaleTransaction, String> value) {
        var column = new TableColumn<SaleTransaction, String>(title);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
