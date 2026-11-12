package edu.itc.salesreport.ui;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.MonthData;
import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.fx.DashboardWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Composition root: the only class that knows every concrete type. */
public class ReportViewerApp extends Application {

    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed()
                .getOrDefault("data", "data"));

        var reader     = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();

        SummarySource source = (month, branch) -> {
            MonthData data = reader.read(dataDir, month);
            List<SaleTransaction> branchRows = data.rows().stream()
                    .filter(t -> t.branch().equals(branch))
                    .toList();
            return aggregator.summarize(branch, branchRows);
        };

        var vm = new ReportViewModel(source, new CallerThreadJobs(),
                Clock.systemDefaultZone());

        stage.setScene(new Scene(new ReportView(vm).root(), 960, 600));
        stage.setTitle("Angkor Mart · Monthly report (MVVM)");
        stage.show();

        // Challenge: open the dashboard in a second window sharing the same VM
        DashboardWindow.open(vm);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
