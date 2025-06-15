package com.connect.connector.enums;


public enum City {
    // Israel
    TEL_AVIV(Country.ISRAEL),
    JERUSALEM(Country.ISRAEL),

    // France
    PARIS(Country.FRANCE),
    MARSEILLE(Country.FRANCE),

    // Germany
    BERLIN(Country.GERMANY),
    MUNICH(Country.GERMANY),

    // Czechia
    PRAGUE(Country.CZECHIA),
    BRNO(Country.CZECHIA),

    // Switzerland
    ZURICH(Country.SWITZERLAND),
    GENEVA(Country.SWITZERLAND),

    // Italy
    ROME(Country.ITALY),
    MILAN(Country.ITALY),

    // Spain
    MADRID(Country.SPAIN),
    BARCELONA(Country.SPAIN),

    // Poland
    WARSAW(Country.POLAND),
    KRAKOW(Country.POLAND);

    private final Country country;

    City(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }
}