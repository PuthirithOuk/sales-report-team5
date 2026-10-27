package edu.itc.salesreport.ui.fx.legacy;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Smell on purpose: I/O, parsing, filtering and totals in a handler. */
public class FatReportController {
    private final TextField monthField;
    private final ComboBox<String> branchBox;
    private final Label status;

    public FatReportController(TextField monthField,
            ComboBox<String> branchBox, Label status) {
        this.monthField = monthField;
        this.branchBox = branchBox;
        this.status = status;
    }

    public void onLoad() {
        try {
            var dir = Path.of("data", monthField.getText());
            BigDecimal revenue = BigDecimal.ZERO;
            for (Path f : Files.list(dir).toList()) {
                var lines = Files.readAllLines(f);
                for (String line : lines.subList(1, lines.size())) {
                    String[] c = line.split(",");
                    if (!branchBox.getValue().equals("ALL")
                            && !c[0].equals(branchBox.getValue())) continue;
                    revenue = revenue.add(new BigDecimal(c[7])
                            .multiply(new BigDecimal(c[6]))
                            .subtract(new BigDecimal(c[8])));
                }
            }
            status.setText("Revenue: " + revenue);
        } catch (Exception e) {
            status.setText("Error");
        }
    }
}