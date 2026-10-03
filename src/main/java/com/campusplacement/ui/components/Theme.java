package com.campusplacement.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/** Black + ivory + plum design tokens and global Swing defaults. */
public final class Theme {
    private Theme() { }

    public static final Color BG = new Color(0xF8, 0xFA, 0xFC);
    public static final Color SURFACE = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TEXT = new Color(0x0F, 0x17, 0x2A);
    public static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    public static final Color PLUM = new Color(0x0F, 0x17, 0x2A);
    public static final Color DEEP_PLUM = new Color(0x1E, 0x29, 0x3B);
    public static final Color LIGHT_PLUM = new Color(0xF1, 0xF5, 0xF9);

    public static final Color INK = new Color(0x0F, 0x17, 0x2A);
    public static final Color INK_RAISED = new Color(0x1E, 0x29, 0x3B);
    public static final Color INK_TEXT = new Color(0xF8, 0xFA, 0xFC);
    public static final Color INK_MUTED = new Color(0x94, 0xA3, 0xB8);
    public static final Color ROW_ALT = new Color(0xF8, 0xFA, 0xFC);
    public static final Color ROW_SELECTED = new Color(0xF1, 0xF5, 0xF9);
    public static final Color BRICK = new Color(0xDC, 0x26, 0x26);
    public static final Color BRICK_BG = new Color(0xFE, 0xF2, 0xF2);
    public static final Color OCHRE = new Color(0xD9, 0x77, 0x06);
    public static final Color OCHRE_BG = new Color(0xFF, 0xFB, 0xEB);
    public static final Color STONE_BG = new Color(0xF1, 0xF5, 0xF9);

    public static final String SANS = pick("Inter", "Segoe UI", "Helvetica Neue", "Arial", "SansSerif");

    public static Font serif(int size) { return new Font(SANS, Font.PLAIN, size); }

    public static Font serifBold(int size) { return new Font(SANS, Font.BOLD, size); }

    public static Font sans(int size) { return new Font(SANS, Font.PLAIN, size); }

    public static Font sansBold(int size) { return new Font(SANS, Font.BOLD, size); }

    private static String pick(String... families) {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : families) {
            if (available.contains(f)) {
                return f;
            }
        }
        return families[families.length - 1];
    }

    /** Installs consistent defaults for components that are not custom painted. */
    public static void install() {
        boolean flatLaf = false;
        try {
            flatLaf = com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) { }
        }

        FontUIResource body = new FontUIResource(sans(13));
        for (Object key : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (key.toString().endsWith(".font")) {
                UIManager.put(key, body);
            }
        }

        put("Panel.background", BG);
        put("OptionPane.background", SURFACE);
        put("OptionPane.messageForeground", TEXT);
        put("Label.foreground", TEXT);
        put("TextField.background", SURFACE);
        put("TextField.foreground", TEXT);
        put("TextField.caretForeground", PLUM);
        put("TextField.selectionBackground", LIGHT_PLUM);
        put("TextField.selectionForeground", TEXT);
        put("PasswordField.background", SURFACE);
        put("PasswordField.selectionBackground", LIGHT_PLUM);
        put("TextArea.background", SURFACE);
        put("TextArea.selectionBackground", LIGHT_PLUM);
        put("ComboBox.background", SURFACE);
        put("ComboBox.foreground", TEXT);
        put("ComboBox.selectionBackground", LIGHT_PLUM);
        put("ComboBox.selectionForeground", TEXT);
        put("ComboBox.buttonBackground", SURFACE);
        put("ComboBox.disabledBackground", BG);
        put("List.selectionBackground", LIGHT_PLUM);
        put("List.selectionForeground", TEXT);
        put("Table.selectionBackground", ROW_SELECTED);
        put("Table.selectionForeground", TEXT);
        put("Table.gridColor", BORDER);
        put("CheckBox.background", SURFACE);
        put("CheckBox.foreground", TEXT);
        put("ScrollPane.background", SURFACE);
        put("Viewport.background", SURFACE);
        put("ToolTip.background", INK);
        put("ToolTip.foreground", INK_TEXT);
        put("TabbedPane.selected", SURFACE);
        put("TabbedPane.background", BG);
        put("TabbedPane.contentAreaColor", SURFACE);
        put("TabbedPane.focus", SURFACE);
        put("TabbedPane.selectHighlight", BORDER);
        put("TabbedPane.borderHightlightColor", BORDER);
        put("TabbedPane.darkShadow", BORDER);
        put("TabbedPane.shadow", BORDER);
        put("TabbedPane.light", BORDER);
        put("TabbedPane.highlight", BORDER);
        put("SplitPane.background", BG);
        put("SplitPaneDivider.draggingColor", BORDER);
        put("Button.focus", SURFACE);
        put("CheckBox.focus", SURFACE);
        put("ComboBox.border", BorderFactory.createLineBorder(BORDER));
        put("ToolTip.border", BorderFactory.createEmptyBorder(6, 8, 6, 8));
        put("ScrollPane.border", BorderFactory.createEmptyBorder());

        if (flatLaf) {
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.track", SURFACE);
            UIManager.put("ScrollBar.thumb", new Color(0xCF, 0xD8, 0xDC));
        } else {
            UIManager.put("ScrollBar.thumb", new Color(0xCF, 0xD8, 0xDC));
            UIManager.put("ScrollBar.track", SURFACE);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBarUI", ThemeUI.ScrollBar.class.getName());
            UIManager.put("ComboBoxUI", ThemeUI.Combo.class.getName());
        }
    }

    private static void put(String key, Object value) {
        UIManager.put(key, value instanceof Color c ? new ColorUIResource(c) : value);
    }
}
