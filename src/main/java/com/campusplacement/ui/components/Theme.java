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

    public static final Color BG = new Color(0xF5F2ED);
    public static final Color SURFACE = new Color(0xFFFDFC);
    public static final Color TEXT = new Color(0x17151A);
    public static final Color MUTED = new Color(0x756D73);
    public static final Color BORDER = new Color(0xDDD7D2);
    public static final Color PLUM = new Color(0x633B59);
    public static final Color DEEP_PLUM = new Color(0x48283F);
    public static final Color LIGHT_PLUM = new Color(0xE9DFE7);

    public static final Color INK = new Color(0x17151A);
    public static final Color INK_RAISED = new Color(0x26222A);
    public static final Color INK_TEXT = new Color(0xEDE7E1);
    public static final Color INK_MUTED = new Color(0x9C939A);
    public static final Color ROW_ALT = new Color(0xFAF7F3);
    public static final Color ROW_SELECTED = new Color(0xEFE6EC);
    public static final Color BRICK = new Color(0x8A3B32);
    public static final Color BRICK_BG = new Color(0xF3E3E0);
    public static final Color OCHRE = new Color(0x7A5A1E);
    public static final Color OCHRE_BG = new Color(0xF4ECDD);
    public static final Color STONE_BG = new Color(0xECE8E4);

    public static final String SERIF = pick("Georgia", "Palatino Linotype", "Book Antiqua", "Cambria", "Charter",
            "DejaVu Serif", "Serif");
    public static final String SANS = pick("Segoe UI", "Helvetica Neue", "Inter", "Noto Sans", "Ubuntu", "DejaVu Sans",
            "SansSerif");

    public static Font serif(int size) { return new Font(SERIF, Font.PLAIN, size); }

    public static Font serifBold(int size) { return new Font(SERIF, Font.BOLD, size); }

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
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to whatever look and feel is active
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
        put("CheckBox.background", SURFACE);
        put("CheckBox.foreground", TEXT);
        put("ScrollPane.background", SURFACE);
        put("Viewport.background", SURFACE);
        put("ScrollBar.thumb", new Color(0xCFC7C2));
        put("ScrollBar.track", SURFACE);
        put("ScrollBar.width", 10);
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
        UIManager.put("ScrollBarUI", ThemeUI.ScrollBar.class.getName());
        UIManager.put("ComboBoxUI", ThemeUI.Combo.class.getName());
    }

    private static void put(String key, Object value) {
        UIManager.put(key, value instanceof Color c ? new ColorUIResource(c) : value);
    }
}
