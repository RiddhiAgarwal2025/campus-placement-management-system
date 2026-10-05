package com.campusplacement.util;

import com.campusplacement.service.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorsTest {

    @Test
    @DisplayName("Required string returns trimmed value when valid")
    void testRequiredValid() {
        assertThat(Validators.required("Name", "  John Doe  ", 50)).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Required string throws ServiceException on empty or null")
    void testRequiredEmptyOrNull() {
        assertThatThrownBy(() -> Validators.required("Name", null, 50))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Name is required.");

        assertThatThrownBy(() -> Validators.required("Name", "   ", 50))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Name is required.");
    }

    @Test
    @DisplayName("Required string throws ServiceException when exceeding max length")
    void testRequiredExceedsLength() {
        assertThatThrownBy(() -> Validators.required("Code", "123456", 5))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Code must be at most 5 characters.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"john@example.com", "mohak.gupta@univ.ac.in", "student+test@gmail.com"})
    @DisplayName("Email accepts valid formats and converts to lowercase")
    void testEmailValid(String email) {
        assertThat(Validators.email(email, true)).isEqualTo(email.toLowerCase());
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "missing@domain", "@nodomain.com", "space in@address.com"})
    @DisplayName("Email rejects invalid syntax")
    void testEmailInvalid(String email) {
        assertThatThrownBy(() -> Validators.email(email, true))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Enter a valid email address");
    }

    @Test
    @DisplayName("Decimal validation parses and sets scale to 2")
    void testDecimalValid() {
        BigDecimal val = Validators.decimal("CGPA", "8.7", 0, 10, true);
        assertThat(val).isEqualByComparingTo("8.70");
        assertThat(val.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Decimal validation rejects values out of bounds")
    void testDecimalOutOfBounds() {
        assertThatThrownBy(() -> Validators.decimal("CGPA", "10.5", 0, 10, true))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("CGPA must be between 0 and 10.");

        assertThatThrownBy(() -> Validators.decimal("Package", "-1.0", 0, 100, false))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("Integer validation parses correctly within range")
    void testIntegerValid() {
        int year = Validators.integer("Graduation Year", "2025", 2000, 2100);
        assertThat(year).isEqualTo(2025);
    }

    @Test
    @DisplayName("Integer validation throws ServiceException on invalid format or range")
    void testIntegerInvalid() {
        assertThatThrownBy(() -> Validators.integer("Backlogs", "abc", 0, 20))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backlogs must be a whole number.");

        assertThatThrownBy(() -> Validators.integer("Backlogs", "25", 0, 20))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backlogs must be between 0 and 20.");
    }

    @Test
    @DisplayName("Date parses ISO 8601 date string")
    void testDateValid() {
        LocalDate date = Validators.date("Deadline", "2026-10-15");
        assertThat(date).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    @DisplayName("Date throws ServiceException on non-ISO format")
    void testDateInvalid() {
        assertThatThrownBy(() -> Validators.date("Deadline", "15/10/2026"))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Deadline must be a date in the form YYYY-MM-DD.");
    }
}
