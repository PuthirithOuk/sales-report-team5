package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.mvp.ReportPresenter;
import java.nio.file.Path;
import java.time.Clock;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ReportMvpApp extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Model
        var model = new SalesModel(
                new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator());

        // 2. Passive view
        var screen = new FxReportScreen();

        // 3. Presenter (registers itself as the model's listener)
        var presenter = new ReportPresenter(
                model, screen, Path.of("data"), Clock.systemDefaultZone());

        // 4. Connect the view's gestures to the presenter
        screen.attach(presenter);

        stage.setTitle("Angkor Mart · Monthly sales (MVP)");
        stage.setScene(new Scene(screen.root(), 1200, 720));
        stage.show();

        // 5. Load the initial month through the presenter
        presenter.monthEntered("2026-09");
    }

    public static void main(String[] args) {
        launch(args);
    }
}