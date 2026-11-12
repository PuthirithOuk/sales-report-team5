package edu.itc.salesreport.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Challenge wiring: opens a compact dashboard in a second Stage that shares
 * the same ReportViewModel instance as the main viewer.
 */
public final class DashboardWindow {
    private static Stage sharedDashboard;

    public static Stage open(ReportViewModel vm) {
        if (sharedDashboard != null && sharedDashboard.isShowing()) {
            return sharedDashboard;
        }

        Stage dash = new Stage();
        sharedDashboard = dash;
        dash.setOnHidden(event -> {
            if (sharedDashboard == dash) {
                sharedDashboard = null;
            }
        });
        dash.setScene(new Scene(new DashboardView(vm).root(), 320, 280));
        dash.setTitle("Angkor Mart · Dashboard");
        dash.setAlwaysOnTop(true);
        dash.show();
        return dash;
    }

    private DashboardWindow() { }
}
