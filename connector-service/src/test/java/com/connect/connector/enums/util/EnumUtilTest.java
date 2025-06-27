package com.connect.connector.enums.util;

import com.connect.connector.enums.City;
import com.connect.connector.enums.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EnumUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "TEL_AVIV",   // Underscore format
            "tel-aviv",   // Lowercase with dash
            "tel aviv",   // Lowercase with space
            "Tel Aviv",   // Mixed case with space
            "tEl_aViV",   // Mixed case with underscore
            "TEL AVIV"
    })
    void fromDisplayName_shouldReturnCorrectCityForVariousValidFormats(String inputCityName) {
        // Arrange & Act
        City city = EnumUtil.fromDisplayName(City.class, inputCityName);

        // Assert
        assertEquals(City.TEL_AVIV, city, "Input '" + inputCityName + "' should map to TEL_AVIV");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "POLAND",   // All caps
            "poland",   // All lowercase
            "Poland"    // Title case
    })
    void fromDisplayName_shouldReturnCorrectCountryForVariousValidFormats(String inputCountryName) {
        // Arrange & Act
        Country country = EnumUtil.fromDisplayName(Country.class, inputCountryName);

        // Assert - Corrected assertion to Country.POLAND
        assertEquals(Country.POLAND, country, "Input '" + inputCountryName + "' should map to POLAND");
    }

    @Test
    void fromDisplayName_shouldThrow_whenInputIsUnknown() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> EnumUtil.fromDisplayName(City.class, "not-a-city")
        );
        assertTrue(ex.getMessage().contains("Unknown enum name"));
    }

    @Test
    void fromDisplayName_shouldBeCaseInsensitive() {
        City city = EnumUtil.fromDisplayName(City.class, "TeL AvIv");
        assertEquals(City.TEL_AVIV, city);
    }

    @Test
    void fromDisplayName_shouldThrow_whenInputIsNull() {
        assertThrows(NullPointerException.class, () ->
                EnumUtil.fromDisplayName(City.class, null));
    }

    @Test
    void fromDisplayName_shouldThrow_whenInputIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                EnumUtil.fromDisplayName(City.class, ""));
    }
}
