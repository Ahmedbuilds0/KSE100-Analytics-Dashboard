package com.kse100.model;

import java.util.List;

/**
 * Encapsulates the list of DataRecords along with loading metadata.
 * Demonstrates OOP encapsulation by exposing read-only data statistics to the controller.
 */
public class Dataset {
    private final List<DataRecord> records;
    private final int totalCount;
    private final int repairedCount;
    private final String startDate;
    private final String endDate;

    /**
     * Constructs a Dataset containing the records and metadata.
     * @param records       List of successfully validated/repaired DataRecords
     * @param totalCount    Total records processed (including repaired ones)
     * @param repairedCount Total number of repaired/corrupted records detected
     * @param startDate     The first date in the dataset
     * @param endDate       The last date in the dataset
     */
    public Dataset(List<DataRecord> records, int totalCount, int repairedCount, String startDate, String endDate) {
        this.records = records;
        this.totalCount = totalCount;
        this.repairedCount = repairedCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public List<DataRecord> getRecords() {
        return records;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getRepairedCount() {
        return repairedCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
