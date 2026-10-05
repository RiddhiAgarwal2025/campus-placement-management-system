package com.campusplacement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExporterTest {

    @Test
    @DisplayName("Export CSV properly quotes commas and sanitizes spreadsheet formulas")
    void testExportFormulasAndQuotes(@TempDir Path tempDir) throws IOException {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Formula");
        model.addColumn("Text");
        model.addColumn("Number");

        model.addRow(new Object[]{"=SUM(A1:A10)", "Hello, World", 100});
        model.addRow(new Object[]{"@cmd|' /C calc'!A0", "Simple", 200});
        model.addRow(new Object[]{"+1+2", "Line 1\nLine 2", 300});
        model.addRow(new Object[]{"-5+10", "Safe", 400});

        Path csvFile = tempDir.resolve("test_export.csv");
        CsvExporter.export(model, csvFile);

        assertThat(csvFile).exists();
        String content = Files.readString(csvFile);

        // Header check
        assertThat(content).contains("Formula,Text,Number");

        // Formula neutralization check (prefixed with single quote)
        assertThat(content).contains("'=SUM(A1:A10)");
        assertThat(content).contains("'@cmd|' /C calc'!A0");
        assertThat(content).contains("'+1+2");
        assertThat(content).contains("'-5+10");

        // Quoted text with comma and newline check
        assertThat(content).contains("\"Hello, World\"");
        assertThat(content).contains("\"Line 1\nLine 2\"");
    }

    @Test
    @DisplayName("Export CSV preserves legitimate negative numbers without prepending single quote")
    void testLegitimateNegativeNumbersNotMangled(@TempDir Path tempDir) throws IOException {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Metric");
        model.addColumn("Value");

        model.addRow(new Object[]{"Temperature", -5});
        model.addRow(new Object[]{"Delta", "-12.50"});
        model.addRow(new Object[]{"Loss", -100});

        Path csvFile = tempDir.resolve("test_negative.csv");
        CsvExporter.export(model, csvFile);

        assertThat(csvFile).exists();
        String content = Files.readString(csvFile);

        // Verify that negative numbers are not prepended with single quotes
        assertThat(content).contains("Temperature,-5");
        assertThat(content).contains("Delta,-12.50");
        assertThat(content).contains("Loss,-100");
        assertThat(content).doesNotContain("'-5");
        assertThat(content).doesNotContain("'-12.50");
        assertThat(content).doesNotContain("'-100");
    }
}
