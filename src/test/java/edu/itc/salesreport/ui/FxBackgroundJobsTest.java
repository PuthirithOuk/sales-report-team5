package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;

import javafx.concurrent.Task;
import org.junit.jupiter.api.Test;

class FxBackgroundJobsTest {

    @Test
    void progressSinkChecksTaskCancellation() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                return null;
            }
        };

        BackgroundJobs.ProgressSink sink =
                FxBackgroundJobs.progressSink(task, (done, total) -> { });

        assertFalse(sink.isCancelled());
    }
}
