package com.campusplacement.ui.officer;

import com.campusplacement.model.Company;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.service.CompanyService;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Badge;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DrivesPage extends Page implements Searchable {
    private final DriveService service = new DriveService();
    private final EligibilityService eligibility = new EligibilityService();
    private final DataTable<Drive> table = new DataTable<Drive>("No drives match", "Create a drive or adjust the filters.")
            .col("#", Drive::driveId, 40, Kind.NUMBER)
            .col("Company", Drive::companyName, 160)
            .col("Job profile", Drive::position, 160)
            .col("Status", Drive::status, 110, Kind.BADGE)
            .col("Package", Drive::packageLpa, 110, Kind.MONEY)
            .col("Deadline", Drive::deadline, 110, Kind.DATE)
            .col("Applications", Drive::applicationCount, 95, Kind.NUMBER)
            .col("Drive date", Drive::driveDate, 110, Kind.DATE)
            .col("Location", Drive::location, 100)
            .col("Eligibility", DrivesPage::shortCriteria, 200);
    private final StatTile kpiActive = new StatTile("Active Drives", "Accepting applications", false);
    private final StatTile kpiUpcoming = new StatTile("Scheduled", "Upcoming drives", false);
    private final StatTile kpiCompleted = new StatTile("Completed", "Finished campaigns", false);
    private final StatTile kpiApps = new StatTile("Applications", "Total submissions", false);
    private final HintField search;
    private final JComboBox<String> status;
    private final JComboBox<Company> company;
    private boolean loading;

    private final JPanel detail = Ui.vstack(0);

    public DrivesPage() {
        super("Placement Drives", "Recruitment drives with dates, status, eligibility and application counts.");
        Btn add = new Btn("New drive", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> { if (form(() -> DriveForms.createOrEdit(this, null))) { refresh(); } });
        addAction(add);
        FilterBar bar = new FilterBar(() -> { if (!loading) { reloadTable(); } });
        search = bar.search("Search company, position, location");
        status = bar.combo(Ui.filterCombo("All statuses", Arrays.stream(Drive.Status.values()).map(Enum::name).toList()));
        company = bar.combo(Ui.filterCombo("All companies", List.of()));
        table.onSelect(this::showDetail);
        table.onDoubleClick(d -> new DriveDetailDialog(this, d).setVisible(true));

        KpiBanner kpiBanner = new KpiBanner(kpiActive, kpiUpcoming, kpiCompleted, kpiApps);

        JPanel detailCard = Ui.card(Ui.scroll(detail), 0);
        detail.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        detail.setOpaque(true);
        detail.setBackground(Theme.SURFACE);
        detailCard.setPreferredSize(new Dimension(330, 200));
        showDetail(null);

        JPanel tableCardContent = new JPanel(new BorderLayout(0, 12));
        tableCardContent.setOpaque(false);
        tableCardContent.add(bar, BorderLayout.NORTH);
        tableCardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(tableCardContent, 18);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(tableCard, BorderLayout.CENTER);
        center.add(detailCard, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(center, BorderLayout.CENTER);
        setBody(body);
    }

    static String shortCriteria(Drive d) {
        if (d.minCgpa() == null) {
            return "Not defined";
        }
        return "CGPA " + d.minCgpa() + "+, ≤" + d.maxBacklogs() + " backlogs"
                + (d.graduationYear() == null ? "" : ", " + d.graduationYear());
    }

    private boolean form(java.util.function.BooleanSupplier s) {
        try {
            return s.getAsBoolean();
        } catch (ServiceException e) {
            com.campusplacement.ui.components.Dialogs.error(this, "Could not open form", e.getMessage());
            return false;
        }
    }

    private void showDetail(Drive d) {
        detail.removeAll();
        if (d == null) {
            detail.add(Ui.heading("Select a drive"));
            detail.add(Box.createVerticalStrut(6));
            detail.add(Ui.muted("<html><div style='width:250px'>Details, eligibility criteria and actions for the "
                    + "selected drive appear here.</div></html>"));
        } else {
            JLabel co = Ui.label("<html><div style='width:280px'>" + Ui.escape(d.companyName()) + "</div></html>",
                    Theme.serif(22), Theme.TEXT);
            detail.add(left(co));
            detail.add(left(Ui.body(d.position())));
            detail.add(Box.createVerticalStrut(10));
            detail.add(left(Badge.on(d.status(), Theme.SURFACE)));
            detail.add(Box.createVerticalStrut(10));
            detail.add(Ui.kv("Package", Formats.lpa(d.packageLpa())));
            detail.add(Ui.kv("Location", d.location()));
            detail.add(Ui.kv("Venue", d.venue()));
            detail.add(Ui.kv("Drive date", Formats.date(d.driveDate())));
            detail.add(Ui.kv("Deadline", Formats.date(d.deadline()) + " (" + Formats.relative(d.deadline()) + ")"));
            detail.add(Ui.kv("Applications", String.valueOf(d.applicationCount())));
            String crit;
            try {
                EligibilityCriteria cr = eligibility.criteria(d.driveId());
                crit = cr.summary();
            } catch (ServiceException e) {
                crit = e.getMessage();
            }
            detail.add(Ui.kv("Eligibility", crit));
            detail.add(Box.createVerticalStrut(14));
            JPanel actions = new JPanel(new GridLayout(0, 2, 8, 8));
            actions.setOpaque(false);
            actions.setAlignmentX(LEFT_ALIGNMENT);
            actions.add(btn("View details", Btn.Variant.SECONDARY, () -> new DriveDetailDialog(this, d).setVisible(true)));
            actions.add(btn("Check eligibility", Btn.Variant.PRIMARY, () -> {
                EligibilityPage.preselect(d.driveId());
                MainFrame.navigate("eligibility");
            }));
            actions.add(btn("Edit drive", Btn.Variant.SECONDARY, () -> {
                if (form(() -> DriveForms.createOrEdit(this, d))) { refresh(); }
            }));
            actions.add(btn("Edit criteria", Btn.Variant.SECONDARY, () -> {
                if (form(() -> DriveForms.editCriteria(this, d))) { refresh(); }
            }));
            if ("UPCOMING".equals(d.status()) || "CLOSED".equals(d.status())) {
                actions.add(btn("Open drive", Btn.Variant.SECONDARY, () -> setStatus(d, "OPEN")));
            }
            if ("OPEN".equals(d.status()) || "UPCOMING".equals(d.status())) {
                actions.add(btn("Close drive", Btn.Variant.SECONDARY, () -> setStatus(d, "CLOSED")));
            }
            if (!"COMPLETED".equals(d.status())) {
                actions.add(btn("Mark completed", Btn.Variant.SECONDARY, () -> setStatus(d, "COMPLETED")));
            }
            actions.add(btn("Delete", Btn.Variant.DANGER, () -> {
                if (confirmDelete("drive #" + d.driveId()) && run(() -> service.delete(d))) {
                    refresh();
                }
            }));
            actions.setMaximumSize(actions.getPreferredSize());
            detail.add(actions);
        }
        detail.revalidate();
        detail.repaint();
    }

    private static <T extends javax.swing.JComponent> T left(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private Btn btn(String text, Btn.Variant v, Runnable r) {
        Btn b = new Btn(text, v);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void setStatus(Drive d, String s) {
        String verb = switch (s) {
            case "OPEN" -> "Open";
            case "CLOSED" -> "Close";
            default -> "Complete";
        };
        if (confirm(verb + " drive #" + d.driveId() + "?", d.title() + " will be marked " + s + ".", verb + " drive")
                && run(() -> service.setStatus(d.driveId(), s))) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        loading = true;
        Object sel = company.getSelectedItem();
        company.removeAllItems();
        company.addItem(null);
        List<Company> all = load(() -> new CompanyService().list(""), List.of());
        all.forEach(company::addItem);
        if (sel instanceof Company s) {
            all.stream().filter(c -> c.companyId() == s.companyId()).findFirst().ifPresent(company::setSelectedItem);
        }
        loading = false;
        reloadTable();
    }

    private void reloadTable() {
        Company c = (Company) company.getSelectedItem();
        Drive keep = table.selected();
        List<Drive> rows = load(() -> service.list(search.getText(), (String) status.getSelectedItem(),
                c == null ? null : c.companyId()), List.of());
        table.setRows(rows);

        long active = rows.stream().filter(d -> "OPEN".equals(d.status())).count();
        long upcoming = rows.stream().filter(d -> "UPCOMING".equals(d.status())).count();
        long completed = rows.stream().filter(d -> "COMPLETED".equals(d.status())).count();
        long apps = rows.stream().mapToInt(Drive::applicationCount).sum();

        kpiActive.setValue(active);
        kpiUpcoming.setValue(upcoming);
        kpiCompleted.setValue(completed);
        kpiApps.setValue(apps);

        if (keep != null && table.selected() == null) {
            rows.stream().filter(d -> d.driveId() == keep.driveId()).findFirst().ifPresent(d -> {
                int idx = rows.indexOf(d);
                int v = table.table().convertRowIndexToView(idx);
                table.table().setRowSelectionInterval(v, v);
            });
        }
        if (table.selected() == null && !rows.isEmpty()) {
            table.table().setRowSelectionInterval(0, 0);
        }
        if (table.selected() == null) {
            showDetail(null);
        }
    }

    @Override
    public void setSearch(String text) {
        if (search != null) {
            search.setText(text == null ? "" : text);
            reloadTable();
        }
    }

    @Override
    public String getSearch() {
        return search == null ? "" : search.getText();
    }
}
