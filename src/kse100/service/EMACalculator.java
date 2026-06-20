package com.kse100.service;

import com.kse100.model.DataRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclass implementing the Exponential Moving Average (EMA) algorithm.
 * Extends the DataProcessor class and measures processing time in nanoseconds.
 */
public class EMACalculator extends DataProcessor {
    private long executionTimeNs = 0;
    private final double alpha;

    /** Default constructor uses alpha = 0.2. */
    public EMACalculator() {
        this(0.2);
    }

    /**
     * @param alpha smoothing factor between 0.0 and 1.0.
     */
    public EMACalculator(double alpha) {
        this.alpha = Math.max(0.0, Math.min(1.0, alpha));
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

        // Initial EMA setup: EMA_0 = Price_0
        double previousEma = input.get(0).getPrice();
        output.add(new DataRecord(input.get(0).getDate(), previousEma));

        for (int i = 1; i < n; i++) {
            double currentPrice = input.get(i).getPrice();
            double emaValue = currentPrice * alpha + previousEma * (1.0 - alpha);
            output.add(new DataRecord(input.get(i).getDate(), emaValue));
            previousEma = emaValue;
        }

        long endTime = System.nanoTime();
        // PERFORMANCE TIMER END

        executionTimeNs = endTime - startTime;
        return output;
    }

    /**
     * Gets the execution time of the last process() call in nanoseconds.
     * @return Execution speed in ns
     */
    public long getExecutionTimeNs() {
        return executionTimeNs;
    }
}
