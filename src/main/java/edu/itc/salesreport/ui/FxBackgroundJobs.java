package edu.itc.salesreport.ui;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.concurrent.Task;

/** Task 4: Background jobs implementation using JavaFX Task and an executor. */
public final class FxBackgroundJobs implements BackgroundJobs {

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Report-Worker");
        t.setDaemon(true);
        return t;
    });

    @Override
    public <T> JobHandle submit(Work<T> work, JobListener<T> listener) {
        Objects.requireNonNull(work);
        Objects.requireNonNull(listener);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                ProgressSink sink = progressSink(this,
                        (done, total) -> updateProgress(done, total));
                return work.run(sink);
            }
        };

        task.progressProperty().addListener((obs, oldVal, newVal) -> {
            double prog = newVal.doubleValue();
            Platform.runLater(() -> listener.progress(Double.isNaN(prog) ? 0.0 : prog));
        });

        task.setOnSucceeded(e -> Platform.runLater(() -> listener.succeeded(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> listener.failed(task.getException())));
        task.setOnCancelled(e -> Platform.runLater(listener::cancelled));

        executor.submit(task);

        return new JobHandle() {
            @Override
            public void cancel() {
                task.cancel(true);
            }
        };
    }

    static ProgressSink progressSink(Task<?> task,
                                     BiConsumer<Long, Long> progressUpdater) {
        return new ProgressSink() {
            @Override
            public void update(long done, long total) {
                progressUpdater.accept(done, total);
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }
}