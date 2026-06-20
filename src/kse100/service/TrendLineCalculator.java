package com.kse100.service;

import com.kse100.model.DataRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclass implementing a linear regression "best fit" trend line over the original dataset.
 * Extends DataProcessor and measures processing time in nanoseconds.
 */
public class TrendLineCalculator extends DataProcessor {
    private long executionTimeNs = 0;

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

        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += input.get(i).getPrice();
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        double num = 0;
        double den = 0;
        for (int i = 0; i < n; i++) {
            double diffX = i - meanX;
            num += diffX * (input.get(i).getPrice() - meanY);
            den += diffX * diffX;
        }

        double slope = den == 0 ? 0 : num / den;
        double intercept = meanY - slope * meanX;

        for (int i = 0; i < n; i++) {
            double predictedPrice = slope * i + intercept;
            output.add(new DataRecord(input.get(i).getDate(), predictedPrice));
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
