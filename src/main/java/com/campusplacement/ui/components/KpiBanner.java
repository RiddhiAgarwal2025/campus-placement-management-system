package com.campusplacement.ui.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 * A horizontal grid banner of StatTiles matching the Figma dashboard KPI bar.
 */
public class KpiBanner extends JPanel {
    public KpiBanner(StatTile... tiles) {
        super(new GridLayout(1, tiles.length, 12, 0));
        setOpaque(false);
        for (StatTile t : tiles) {
            add(t);
        }
        setPreferredSize(new Dimension(100, 88));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
    }
}
