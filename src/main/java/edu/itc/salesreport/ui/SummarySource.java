package edu.itc.salesreport.ui;

import java.time.YearMonth;

import edu.itc.salesreport.model.BranchSummary;

/** What the ViewModel needs from the model: one branch summary at a time. */
@FunctionalInterface
public interface SummarySource {
    BranchSummary load(YearMonth month, String branch) throws Exception;
}