package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.ui.ReportViewModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Challenge wiring: opens a compact dashboard in a second Stage that shares
 * the same ReportViewModel instance as the main viewer.
 */
public final class DashboardWindow {

    public static Stage open(ReportViewModel vm) {
        Stage dash = new Stage();
        dash.setScene(new Scene(new DashboardView(vm).root(), 320, 280));
        dash.setTitle("Angkor Mart · Dashboard");
        dash.setAlwaysOnTop(true);
        dash.show();
        return dash;
    }

    private DashboardWindow() { }
}
