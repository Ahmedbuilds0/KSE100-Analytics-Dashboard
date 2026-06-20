package com.kse100.service;

import com.kse100.model.DataRecord;
import java.util.List;

/**
 * Abstract Parent Class representing a generic stock data processor.
 * Demonstrates Abstraction and Inheritance as part of OOP backend requirements.
 *
 * All subclasses must implement both the processing logic AND expose
 * their execution time, making timing a guaranteed part of the contract.
 */
public abstract class DataProcessor {

    /**
     * Processes the input list of DataRecords using a specific mathematical filter.
     * @param input List of validated original DataRecords
     * @return List of processed/smoothed DataRecords
     */
    public abstract List<DataRecord> process(List<DataRecord> input);

    /**
     * Returns the execution time (in nanoseconds) of the most recent process() call.
     * Measured using System.nanoTime() immediately before and after the processing loop.
     * @return Nanoseconds taken by the last process() call, or 0 if not yet called.
     */
    public abstract long getExecutionTimeNs();
}
