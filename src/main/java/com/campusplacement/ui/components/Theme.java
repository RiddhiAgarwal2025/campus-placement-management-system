package com.campusplacement.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/** Unified design tokens, Light/Dark mode state management and global Swing defaults. */
public final class Theme {
    private Theme() { }

    private static final String PREF_FILE = "theme.properties";
    public static boolean isDarkMode = false;
    private static final List<Runnable> listeners = new ArrayList<>();

    // Dynamic color tokens accessed by all components
    public static Color BG;
    public static Color SURFACE;
    public static Color TEXT;
    public static Color MUTED;
    public static Color BORDER;
    public static Color PLUM;
    public static Color DEEP_PLUM;
    public static Color LIGHT_PLUM;
    public static Color INK;
    public static Color INK_RAISED;
    public static Color INK_TEXT;
    public static Color INK_MUTED;
    public static Color ROW_ALT;
    public static Color ROW_SELECTED;
    public static Color BRICK;
    public static Color BRICK_BG;
    public static Color OCHRE;
    public static Color OCHRE_BG;
    public static Color STONE_BG;
    public static Color FIELD_BG;

    static {
        loadPreference();
        applyPalette();
    }

    public static void addListener(Runnable r) {
        if (!listeners.contains(r)) {
            listeners.add(r);
        }
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
    }

    public static void toggleDarkMode() {
        setDarkMode(!isDarkMode);
    }

    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
        applyPalette();
        try {
            if (dark) {
                com.formdev.flatlaf.FlatDarkLaf.setup();
            } else {
                com.formdev.flatlaf.FlatLightLaf.setup();
            }
        } catch (Throwable ignored) { }
        installDefaults();
        savePreference();

        try {
            com.formdev.flatlaf.FlatLaf.updateUI();
        } catch (Throwable ignored) { }

        for (Window w : Window.getWindows()) {
            if (w.isDisplayable()) {
                SwingUtilities.updateComponentTreeUI(w);
                w.repaint();
            }
        }
        for (Runnable l : new ArrayList<>(listeners)) {
            try {
                l.run();
            } catch (Throwable ignored) { }
        }
    }

    private static void applyPalette() {
        if (isDarkMode) {
            BG = new Color(0x0B, 0x0F, 0x17);
            SURFACE = new Color(0x15, 0x1E, 0x2E);
            TEXT = new Color(0xF1, 0xF5, 0xF9);
            MUTED = new Color(0x94, 0xA3, 0xB8);
            BORDER = new Color(0x28, 0x35, 0x48);
            PLUM = new Color(0xF1, 0xF5, 0xF9);
            DEEP_PLUM = new Color(0xE2, 0xE8, 0xF0);
            LIGHT_PLUM = new Color(0x1E, 0x29, 0x3B);
            INK = new Color(0x15, 0x1E, 0x2E);
            INK_RAISED = new Color(0x1E, 0x29, 0x3B);
            INK_TEXT = new Color(0xF1, 0xF5, 0xF9);
            INK_MUTED = new Color(0x94, 0xA3, 0xB8);
            ROW_ALT = new Color(0x10, 0x17, 0x24);
            ROW_SELECTED = new Color(0x22, 0x2E, 0x42);
            BRICK = new Color(0xE0, 0x7A, 0x70);    // soft muted terracotta in dark
            BRICK_BG = new Color(0x2E, 0x1D, 0x21); // deep wine/blush in dark
            OCHRE = new Color(0xD9, 0x9B, 0x43);    // soft warm ochre in dark
            OCHRE_BG = new Color(0x2D, 0x25, 0x18); // deep amber in dark
            STONE_BG = new Color(0x20, 0x2A, 0x3B);
            FIELD_BG = new Color(0x0E, 0x14, 0x20); // clean recessed dark input field
        } else {
            BG = new Color(0xF8, 0xFA, 0xFC);
            SURFACE = new Color(0xFF, 0xFF, 0xFF);
            TEXT = new Color(0x0F, 0x17, 0x2A);
            MUTED = new Color(0x64, 0x74, 0x8B);
            BORDER = new Color(0xE2, 0xE8, 0xF0);
            PLUM = new Color(0x0F, 0x17, 0x2A);
            DEEP_PLUM = new Color(0x1E, 0x29, 0x3B);
            LIGHT_PLUM = new Color(0xF1, 0xF5, 0xF9);
            INK = new Color(0x0F, 0x17, 0x2A);
            INK_RAISED = new Color(0x1E, 0x29, 0x3B);
            INK_TEXT = new Color(0xF8, 0xFA, 0xFC);
            INK_MUTED = new Color(0x94, 0xA3, 0xB8);
            ROW_ALT = new Color(0xF8, 0xFA, 0xFC);
            ROW_SELECTED = new Color(0xF1, 0xF5, 0xF9);
            BRICK = new Color(0x8A, 0x3B, 0x32);    // previous version's dignified muted terracotta
            BRICK_BG = new Color(0xF3, 0xE3, 0xE0); // previous version's soft blush
            OCHRE = new Color(0x7A, 0x5A, 0x1E);    // previous version's dignified muted ochre
            OCHRE_BG = new Color(0xF4, 0xEC, 0xDD); // previous version's soft warm ivory
            STONE_BG = new Color(0xEC, 0xE8, 0xE4); // previous version's neutral stone
            FIELD_BG = new Color(0xFF, 0xFF, 0xFF);
        }
    }

    private static void loadPreference() {
        File f = new File(PREF_FILE);
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(in);
                isDarkMode = "true".equalsIgnoreCase(p.getProperty("dark_mode"));
            } catch (Exception ignored) { }
        }
    }

    private static void savePreference() {
        try (FileOutputStream out = new FileOutputStream(PREF_FILE)) {
            Properties p = new Properties();
            p.setProperty("dark_mode", String.valueOf(isDarkMode));
            p.store(out, "Campus Placement Portal Theme");
        } catch (Exception ignored) { }
    }

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
        applyPalette();
        boolean flatLaf = false;
        try {
            flatLaf = isDarkMode ? com.formdev.flatlaf.FlatDarkLaf.setup() : com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) { }
        }
        installDefaults();
    }

    private static void installDefaults() {
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
        put("Table.background", SURFACE);
        put("Table.foreground", TEXT);
        put("Table.selectionBackground", ROW_SELECTED);
        put("Table.selectionForeground", TEXT);
        put("Table.gridColor", BORDER);
        put("TableHeader.background", isDarkMode ? new Color(0x10, 0x17, 0x24) : new Color(0xF8, 0xFA, 0xFC));
        put("TableHeader.foreground", MUTED);
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

        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Button.arc", 6);
        UIManager.put("Component.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.track", SURFACE);
        UIManager.put("ScrollBar.thumb", isDarkMode ? new Color(0x3B, 0x48, 0x5C) : new Color(0xCF, 0xD8, 0xDC));
    }

    private static void put(String key, Object value) {
        UIManager.put(key, value instanceof Color c ? new ColorUIResource(c) : value);
    }
}
