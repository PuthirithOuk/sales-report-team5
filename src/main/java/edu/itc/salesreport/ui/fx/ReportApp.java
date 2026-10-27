
package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.ReportController;

import java.nio.file.Path;
import java.time.Clock;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ReportApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // 1. Create the Model
        var model = new SalesModel(
                new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator()
        );

        // 2. Create the View
        var view = new ReportView(model);

        // 3. Create the Controller
        var controller = new ReportController(
                model,
                Path.of("data"),
                Clock.systemDefaultZone(),
                view
        );

        // 4. Connect View with Controller
        view.bind(controller);

        // 5. Set up JavaFX window
        stage.setTitle("Angkor Mart · Monthly sales (MVC)");
        stage.setScene(new Scene(view.root(), 1200, 720));
        stage.show();

        // 6. Load initial month through Controller
        controller.monthEntered("2026-09");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
