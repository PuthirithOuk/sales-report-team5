package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import java.nio.file.Path;
import java.time.Clock;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoadBenchmarkApp extends Application {
    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed()
                .getOrDefault("data", "data"));
        var engine = new DefaultSalesAggregator();
        SummarySource source = (month, branch) -> engine.aggregate(dataDir, month, branch);

        FrameGapMonitor monitor = new FrameGapMonitor();
        monitor.start();

        ComboBox<String> jobRunner = new ComboBox<>();
        jobRunner.getItems().setAll("Asynchronous", "Blocking");
        jobRunner.setValue("Asynchronous");

        BorderPane reportHost = new BorderPane();
        Runnable installReport = () -> {
            jobRunner.disableProperty().unbind();
            BackgroundJobs jobs = "Blocking".equals(jobRunner.getValue())
                    ? new CallerThreadJobs() : new FxBackgroundJobs();
            var vm = new ReportViewModel(source, jobs, Clock.systemDefaultZone());
            reportHost.setCenter(new ReportView(vm).root());
            jobRunner.disableProperty().bind(vm.loadingProperty());
        };
        installReport.run();
        jobRunner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                installReport.run();
            }
        });

        Button resetGap = new Button("Reset Max Frame Gap");
        resetGap.setOnAction(e -> monitor.reset());
        Button checkGap = new Button("Print Max Frame Gap");
        checkGap.setOnAction(e -> System.out.println("Max UI frame gap during run: " + monitor.getMaxGapMillis() + " ms"));

        HBox controls = new HBox(8, new Label("Job runner"), jobRunner,
                resetGap, checkGap);
        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(reportHost);
        stage.setScene(new Scene(root, 960, 650));
        stage.setTitle("Angkor Mart · Load Benchmark (MVVM)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}