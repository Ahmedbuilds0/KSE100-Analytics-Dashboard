package com.kse100.service;

import com.kse100.model.DataRecord;
import com.kse100.model.Dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading KSE-100 data from the local text file.
 * Implements a strict try-catch data validation layer and a previous-value recovery strategy.
 */
public class DataLoader {

    /**
     * Reads a dataset from a text file and returns a validated/repaired Dataset.
     * @param filePath Absolute or relative path to the text file
     * @return Dataset containing records list and metadata
     */
    public Dataset loadData(String filePath) {
        List<DataRecord> records = new ArrayList<>();
        int totalCount = 0;
        int repairedCount = 0;

        String previousDate = "2023-01-01"; // Fallback initial date
        double previousPrice = 40350.25;      // Initial fallback price (first valid price in dataset)

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue; // Skip blank lines
                }

                totalCount++;

                // Clean Python list brackets/quotes if present
                String cleanLine = trimmed;
                if (cleanLine.startsWith("[")) {
                    cleanLine = cleanLine.substring(1);
                }
                if (cleanLine.endsWith("]")) {
                    cleanLine = cleanLine.substring(0, cleanLine.length() - 1);
                }
                cleanLine = cleanLine.replace("'", "").replace("\"", "").trim();

                String date = previousDate;
                double price = previousPrice;
                boolean isCorrupt = false;

                // TRY-CATCH VALIDATION START
                try {
                    if (cleanLine.contains(",")) {
                        String[] parts = cleanLine.split(",", -1);
                        if (parts.length >= 2) {
                            String rawDate = parts[0].trim();
                            String rawPrice = parts[1].trim();

                            if (rawDate.isEmpty()) {
                                throw new IllegalArgumentException("Empty date field");
                            }
                            date = rawDate;

                            if (rawPrice.isEmpty()) {
                                throw new IllegalArgumentException("Empty price field");
                            }
                            price = Double.parseDouble(rawPrice);
                        } else {
                            throw new IllegalArgumentException("Incorrect number of comma-separated tokens");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing comma separator");
                    }

                    // On successful parse, update previous valid values
                    previousDate = date;
                    previousPrice = price;

                } catch (Exception e) {
                    isCorrupt = true;
                    repairedCount++;
                    // Data Recovery Rule: Repair with previous valid price, and log the validation error
                    price = previousPrice;
                    System.err.println("VALIDATION LAYER WARNING: Repaired corrupt row '" + trimmed 
                            + "' at line index " + totalCount + ". Swapped price with previous valid: " 
                            + price + ". Error context: " + e.getMessage());
                }
                // TRY-CATCH VALIDATION END

                // Add to record list (using either parsed values or repaired fallback values)
                records.add(new DataRecord(date, price));
            }
        } catch (IOException e) {
            System.err.println("CRITICAL FILE ERROR: Failed to read from " + filePath + ". Msg: " + e.getMessage());
        }

        String startDate = records.isEmpty() ? "N/A" : records.get(0).getDate();
        String endDate = records.isEmpty() ? "N/A" : records.get(records.size() - 1).getDate();

        return new Dataset(records, totalCount, repairedCount, startDate, endDate);
    }
}
