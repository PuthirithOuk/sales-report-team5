package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import java.nio.file.Path;
import java.time.Clock;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoadBenchmarkApp extends Application {
    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of("data");
        var engine = new DefaultSalesAggregator();
        SummarySource source = (month, branch) -> engine.aggregate(dataDir, month, branch);

        var vm = new ReportViewModel(source, new FxBackgroundJobs(), Clock.systemDefaultZone());
        ReportView view = new ReportView(vm);

        FrameGapMonitor monitor = new FrameGapMonitor();
        monitor.start();

        Button checkGap = new Button("Print Max UI Frame Gap");
        checkGap.setOnAction(e -> System.out.println("Max UI frame gap during run: " + monitor.getMaxGapMillis() + " ms"));

        VBox root = new VBox(10, checkGap, view.root());
        stage.setScene(new Scene(root, 960, 650));
        stage.setTitle("Angkor Mart · Load Benchmark (MVVM)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}