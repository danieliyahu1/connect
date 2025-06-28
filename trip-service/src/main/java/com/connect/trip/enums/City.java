package com.connect.trip.enums;

public enum City {
    // Israel
    TEL_AVIV(Country.ISRAEL, "Tel Aviv"),
    JERUSALEM(Country.ISRAEL, "Jerusalem"),

    // France
    PARIS(Country.FRANCE, "Paris"),
    MARSEILLE(Country.FRANCE, "Marseille"),

    // Germany
    BERLIN(Country.GERMANY, "Berlin"),
    MUNICH(Country.GERMANY, "Munich"),

    // Czechia
    PRAGUE(Country.CZECHIA, "Prague"),
    BRNO(Country.CZECHIA, "Brno"),

    // Switzerland
    ZURICH(Country.SWITZERLAND, "Zurich"),
    GENEVA(Country.SWITZERLAND, "Geneva"),

    // Italy
    ROME(Country.ITALY, "Rome"),
    MILAN(Country.ITALY, "Milan"),

    // Spain
    MADRID(Country.SPAIN, "Madrid"),
    BARCELONA(Country.SPAIN, "Barcelona"),

    // Poland
    WARSAW(Country.POLAND, "Warsaw"),
    KRAKOW(Country.POLAND, "Krakow");

    private final Country country;
    private final String displayValue;

    City(Country country, String displayValue) {
        this.country = country;
        this.displayValue = displayValue;
    }

    public String getDisplayValue() { return displayValue; }

    public Country getCountry() {
        return country;
    }
}