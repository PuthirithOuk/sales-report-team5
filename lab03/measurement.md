# Lab-03 Performance Measurements

- **Dataset:** August 2026, 465,000 CSV rows.
- **CallerThreadJobs (Blocking):** UI freezes entirely (>1,500ms frame gap).
- **FxBackgroundJobs (Async):** UI remains completely fluid, progress bar updates smoothly, max frame gap <35ms.