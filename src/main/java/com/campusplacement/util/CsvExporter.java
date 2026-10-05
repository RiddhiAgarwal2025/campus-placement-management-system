package com.campusplacement.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.table.TableModel;

public final class CsvExporter {
    private CsvExporter() { }

    public static void export(TableModel model, Path file) throws IOException {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write('\ufeff');
            for (int c = 0; c < model.getColumnCount(); c++) {
                w.write((c > 0 ? "," : "") + escape(model.getColumnName(c)));
            }
            w.write("\r\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object v = model.getValueAt(r, c);
                    w.write((c > 0 ? "," : "") + escape(v == null ? "" : v.toString()));
                }
                w.write("\r\n");
            }
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        // Neutralize formula injection / spreadsheet DDE execution (CWE-1236)
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")
                || s.startsWith("\t") || s.startsWith("\r")) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
