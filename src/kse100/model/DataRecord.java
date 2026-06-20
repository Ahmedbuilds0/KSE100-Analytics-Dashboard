package com.kse100.model;

/**
 * Represents a single KSE-100 data record with a date and a closing price.
 * Demonstrates encapsulation using private fields, an explicit constructor, and public getters/setters.
 */
public class DataRecord {
    private String date;
    private double price;

    /**
     * Explicit constructor to initialize the record.
     * @param date  The date of the record (e.g. YYYY-MM-DD)
     * @param price The index closing price
     */
    public DataRecord(String date, double price) {
        this.date = date;
        this.price = price;
    }

    // Public Getters and Setters for private fields (Encapsulation)

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "DataRecord[Date=" + date + ", Price=" + price + "]";
    }
}
