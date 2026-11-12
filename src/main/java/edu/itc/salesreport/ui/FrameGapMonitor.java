package edu.itc.salesreport.ui;

import javafx.animation.AnimationTimer;

/** Task 4: Monitors UI thread freeze / frame gaps during heavy background loading. */
public final class FrameGapMonitor extends AnimationTimer {
    private long lastNanoTime = -1;
    private long maxGapMillis = 0;

    @Override
    public void handle(long now) {
        if (lastNanoTime != -1) {
            long elapsedMillis = (now - lastNanoTime) / 1_000_000;
            if (elapsedMillis > maxGapMillis) {
                maxGapMillis = elapsedMillis;
                if (maxGapMillis > 100) {
                    System.out.println("[UI Freeze Detected] Frame gap: " + maxGapMillis + " ms");
                }
            }
        }
        lastNanoTime = now;
    }

    public long getMaxGapMillis() {
        return maxGapMillis;
    }

    public void reset() {
        lastNanoTime = -1;
        maxGapMillis = 0;
    }
}