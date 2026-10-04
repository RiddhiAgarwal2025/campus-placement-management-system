package com.campusplacement.ui.officer;

import com.campusplacement.model.ReportDefinition;
import com.campusplacement.model.TableData;
import com.campusplacement.service.ReportService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Dialogs;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.CsvExporter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/** SQL-backed reports loaded from reports.sql, with CSV export. */
public class ReportsPage extends Page {
    private final ReportService service = new ReportService();
    private final JList<ReportDefinition> list = new JList<>();
    private final JLabel title = Ui.heading("Select a report");
    private final JLabel desc = Ui.muted(" ");
    private final JLabel count = Ui.muted(" ");
    private final DataTable<Object[]> table = new DataTable<>("No rows", "This report returned no rows.");
    private final StatTile kpiTotalReports = new StatTile("Analytics Reports", "Configured SQL analyses", false);
    private final StatTile kpiSelectedReport = new StatTile("Active Query", "Live analytical view", false);
    private final StatTile kpiRowCount = new StatTile("Records Returned", "Data points generated", false);

    public ReportsPage() {
        super("Reports", "Placement analytics computed by SQL queries and views at the moment you open them.");
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(Theme.SURFACE);
        list.setFixedCellHeight(34);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean f) {
                super.getListCellRendererComponent(l, v, i, sel, false);
                setFont(sel ? Theme.sansBold(13) : Theme.sans(13));
                setForeground(sel ? Theme.DEEP_PLUM : Theme.TEXT);
                setBackground(sel ? Theme.LIGHT_PLUM : Theme.SURFACE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, sel ? Theme.PLUM : Theme.SURFACE),
                        BorderFactory.createEmptyBorder(0, 12, 0, 8)));
                return this;
            }
        });
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) { runReport(); } });
        JPanel left = Ui.card(Ui.scroll(list), 0);
        left.setPreferredSize(new Dimension(300, 200));

        Btn rerun = new Btn("Run again", Btn.Variant.SECONDARY, Glyph.REFRESH);
        rerun.addActionListener(e -> runReport());
        Btn export = new Btn("Export CSV", Btn.Variant.PRIMARY, Glyph.EXPORT);
        export.addActionListener(e -> {
            ReportDefinition r = list.getSelectedValue();
            if (need(r, "a report")) {
                exportCsv(this, table.model(), r.title().toLowerCase().replaceAll("[^a-z0-9]+", "_") + ".csv");
            }
        });
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = Ui.vstack(0);
        titles.add(title);
        titles.add(desc);
        titles.add(count);
        head.add(titles, BorderLayout.WEST);
        head.add(Ui.rightRow(rerun, export), BorderLayout.EAST);
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(head, BorderLayout.NORTH);
        right.add(table, BorderLayout.CENTER);

        JPanel rightCard = Ui.card(right, 18);

        KpiBanner kpiBanner = new KpiBanner(kpiTotalReports, kpiSelectedReport, kpiRowCount);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(left, BorderLayout.WEST);
        center.add(rightCard, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(center, BorderLayout.CENTER);
        setBody(body);
    }

    private void runReport() {
        ReportDefinition r = list.getSelectedValue();
        if (r == null) {
            return;
        }
        title.setText(r.number() + ". " + r.title());
        desc.setText(r.description());
        TableData data = load(() -> service.run(r), null);
        table.clearColumns();
        if (data == null) {
            count.setText("The report could not be run.");
            table.setRows(List.of());
            return;
        }
        for (int i = 0; i < data.columns().size(); i++) {
            final int idx = i;
            boolean numeric = !data.rows().isEmpty() && data.rows().stream().anyMatch(row -> row[idx] instanceof Number);
            Kind kind = data.columns().get(i).equals("Status") || data.columns().get(i).equals("Result")
                    ? Kind.BADGE : numeric ? Kind.NUMBER : Kind.TEXT;
            table.col(data.columns().get(i), row -> row[idx], 0, kind);
        }
        table.setRows(data.rows());
        count.setText(data.rows().size() + " row(s)");
        kpiTotalReports.setValue(list.getModel().getSize());
        kpiSelectedReport.setValue(r.title());
        kpiRowCount.setValue(data.rows().size());
    }

    /** Writes any table model to a CSV file chosen by the user. */
    public static void exportCsv(Component parent, TableModel model, String suggested) {
        if (model.getRowCount() == 0) {
            Dialogs.info(parent, "Nothing to export", "The table is empty.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggested));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getParentFile(), f.getName() + ".csv");
        }
        try {
            CsvExporter.export(model, f.toPath());
            Dialogs.info(parent, "Exported", model.getRowCount() + " row(s) saved to\n" + f.getAbsolutePath());
        } catch (IOException e) {
            Dialogs.error(parent, "Export failed", "The file could not be written: " + e.getMessage());
        }
    }

    @Override
    public void refresh() {
        if (list.getModel().getSize() == 0) {
            List<ReportDefinition> reports = load(service::reports, List.of());
            list.setListData(reports.toArray(new ReportDefinition[0]));
            kpiTotalReports.setValue(reports.size());
            if (!reports.isEmpty()) {
                list.setSelectedIndex(0);
            }
        } else {
            runReport();
        }
    }
}
