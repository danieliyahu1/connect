package com.akatsuki.connector.enums.util;

import com.akatsuki.connector.enums.City;
import com.akatsuki.connector.enums.Country;
import com.akatsuki.connector.exception.IllegalEnumException;
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
    void getEnumFromDisplayName_shouldReturnCorrectCityForVariousValidFormats(String inputCityName) throws IllegalEnumException {
        // Arrange & Act
        City city = EnumUtil.getEnumFromDisplayName(City.class, inputCityName);

        // Assert
        assertEquals(City.TEL_AVIV, city, "Input '" + inputCityName + "' should map to TEL_AVIV");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "POLAND",   // All caps
            "poland",   // All lowercase
            "Poland"    // Title case
    })
    void getEnumFromDisplayName_shouldReturnCorrectCountryForVariousValidFormats(String inputCountryName) throws IllegalEnumException {
        // Arrange & Act
        Country country = EnumUtil.getEnumFromDisplayName(Country.class, inputCountryName);

        // Assert - Corrected assertion to Country.POLAND
        assertEquals(Country.POLAND, country, "Input '" + inputCountryName + "' should map to POLAND");
    }

    @Test
    void getEnumFromDisplayName_shouldThrow_whenInputIsUnknown() {
        IllegalEnumException ex = assertThrows(
                IllegalEnumException.class,
                () -> EnumUtil.getEnumFromDisplayName(City.class, "not-a-city")
        );
        assertTrue(ex.getMessage().contains("Unknown enum name"));
    }

    @Test
    void getEnumFromDisplayName_shouldBeCaseInsensitive() throws IllegalEnumException {
        City city = EnumUtil.getEnumFromDisplayName(City.class, "TeL AvIv");
        assertEquals(City.TEL_AVIV, city);
    }

    @Test
    void getEnumFromDisplayName_shouldThrow_whenInputIsNull() {
        assertThrows(NullPointerException.class, () ->
                EnumUtil.getEnumFromDisplayName(City.class, null));
    }

    @Test
    void getEnumFromDisplayName_shouldThrow_whenInputIsEmpty() {
        assertThrows(IllegalEnumException.class, () ->
                EnumUtil.getEnumFromDisplayName(City.class, ""));
    }
}
