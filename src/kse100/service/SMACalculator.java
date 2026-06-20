package com.kse100.service;

import com.kse100.model.DataRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclass implementing a configurable centered Simple Moving Average (SMA).
 * The "radius" controls how many points on each side of the current index
 * are averaged together (window size = 2*radius + 1), so the dashboard can
 * expose this as an adjustable slider.
 * Extends DataProcessor and measures processing time in nanoseconds.
 */
public class SMACalculator extends DataProcessor {
    private long executionTimeNs = 0;
    private final int radius;

    /** Default constructor uses radius = 1 (the original 3-point centered window). */
    public SMACalculator() {
        this(1);
    }

    /**
     * @param radius number of points to include on each side of the current index.
     *               Effective window size = 2*radius + 1. Values below 0 are clamped to 0.
     */
    public SMACalculator(int radius) {
        this.radius = Math.max(0, radius);
    }

    @Override
    public List<DataRecord> process(List<DataRecord> input) {
        List<DataRecord> output = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            executionTimeNs = 0;
            return output;
        }

        int n = input.size();

        // PERFORMANCE TIMER START
        long startTime = System.nanoTime();

        for (int i = 0; i < n; i++) {
            int lo = Math.max(0, i - radius);
            int hi = Math.min(n - 1, i + radius);

            double sum = 0;
            for (int j = lo; j <= hi; j++) {
                sum += input.get(j).getPrice();
            }
            double smaValue = sum / (hi - lo + 1);
            output.add(new DataRecord(input.get(i).getDate(), smaValue));
        }

        long endTime = System.nanoTime();
        // PERFORMANCE TIMER END

        executionTimeNs = endTime - startTime;
        return output;
    }

    public int getRadius() {
        return radius;
    }

    /** Effective window size (2*radius + 1). */
    public int getWindowSize() {
        return 2 * radius + 1;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }
}
