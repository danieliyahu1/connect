package com.connect.connector.enums;

public enum Country {
    ISRAEL("Israel"),
    FRANCE("France"),
    GERMANY("Germany"),
    CZECHIA("Czechia"),
    SWITZERLAND("Switzerland"),
    ITALY("Italy"),
    SPAIN("Spain"),
    POLAND("Poland");

    private final String displayValue;

    Country(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() { return displayValue; }
}
