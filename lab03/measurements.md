# Lab-03 Performance Measurements

- **Dataset:** `data/2026-08`, 3,720 accepted transaction rows (1,240 per branch, 0 rejected), verified with `CsvMonthReader`.
- **Measurement status:** The prior `465,000`-row dataset is not present in this repository. No frame-gap timing has been recorded for the current dataset, so the earlier `>1,500ms` and `<35ms` figures are not reported as current results.
- **How to measure:** From the repository root, run `mvn javafx:run -Dapp.main=edu.itc.salesreport.ui.LoadBenchmarkApp`. Select **2026-08** and **Asynchronous** or **Blocking**, reset the maximum frame gap, generate the report, and use **Print Max Frame Gap** to record the result for each runner.