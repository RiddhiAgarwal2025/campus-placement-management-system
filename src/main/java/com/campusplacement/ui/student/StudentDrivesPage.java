package com.campusplacement.ui.student;

import com.campusplacement.model.Application;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.EligibilityResult;
import com.campusplacement.service.ApplicationService;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.service.Session;
import com.campusplacement.ui.components.Badge;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StudentDrivesPage extends Page {
    private final DriveService drives = new DriveService();
    private final EligibilityService eligibility = new EligibilityService();
    private final ApplicationService applications = new ApplicationService();
    private final Map<Integer, EligibilityResult> results = new HashMap<>();
    private Map<Integer, String> applied = Map.of();
    private final DataTable<Drive> table = new DataTable<Drive>("No drives available", "New placement drives will appear here.")
            .col("Company", Drive::companyName, 140)
            .col("Position", Drive::position, 140)
            .col("Package", Drive::packageLpa, 100, Kind.MONEY)
            .col("Deadline", Drive::deadline, 105, Kind.DATE)
            .col("Status", Drive::status, 100, Kind.BADGE)
            .col("Eligibility", d -> results.containsKey(d.driveId())
                    ? (results.get(d.driveId()).eligible() ? "ELIGIBLE" : "NOT ELIGIBLE") : "", 115, Kind.BADGE)
            .col("Your application", d -> applied.getOrDefault(d.driveId(), ""), 130, Kind.BADGE)
            .col("Location", Drive::location, 100);
    private final StatTile kpiTotalDrives = new StatTile("Available Drives", "Active hiring campaigns", false);
    private final StatTile kpiEligible = new StatTile("Eligible for You", "Criteria fully matched", false);
    private final StatTile kpiOpen = new StatTile("Open for Application", "Accepting submissions", false);
    private final HintField search;
    private final JComboBox<String> show;
    private final JPanel detail = Ui.vstack(0);

    public StudentDrivesPage() {
        super("Placement Drives", "Check eligibility and apply before the deadline. Completed drives are hidden.");
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search company, position, location");
        show = bar.combo(Ui.combo(List.of("All drives", "Eligible for me", "Open for applications")));
        table.onSelect(this::showDetail);
        detail.setOpaque(true);
        detail.setBackground(Theme.SURFACE);
        detail.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel card = Ui.card(Ui.scroll(detail), 0);
        card.setPreferredSize(new Dimension(340, 200));

        KpiBanner kpiBanner = new KpiBanner(kpiTotalDrives, kpiEligible, kpiOpen);

        JPanel tableCardContent = new JPanel(new BorderLayout(0, 12));
        tableCardContent.setOpaque(false);
        tableCardContent.add(bar, BorderLayout.NORTH);
        tableCardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(tableCardContent, 18);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(tableCard, BorderLayout.CENTER);
        center.add(card, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(center, BorderLayout.CENTER);
        setBody(body);
        showDetail(null);
    }

    private void showDetail(Drive d) {
        detail.removeAll();
        if (d == null) {
            detail.add(Ui.heading("Select a drive"));
            detail.add(Ui.muted("<html><div style='width:280px'>Requirements, your eligibility and the Apply button "
                    + "appear here.</div></html>"));
        } else {
            detail.add(left(Ui.label("<html><div style='width:300px'>" + Ui.escape(d.companyName()) + "</div></html>",
                    Theme.sansBold(20), Theme.TEXT)));
            detail.add(left(Ui.body(d.position())));
            detail.add(Box.createVerticalStrut(10));
            detail.add(left(Badge.on(d.status(), Theme.SURFACE)));
            detail.add(Box.createVerticalStrut(8));
            detail.add(Ui.kv("Package", Formats.lpa(d.packageLpa())));
            detail.add(Ui.kv("Location", d.location()));
            detail.add(Ui.kv("Drive date", Formats.date(d.driveDate()) + ", " + d.venue()));
            detail.add(Ui.kv("Deadline", Formats.date(d.deadline()) + " (" + Formats.relative(d.deadline()) + ")"));
            try {
                EligibilityCriteria cr = eligibility.criteria(d.driveId());
                detail.add(Ui.kv("Requirements", cr.summary()));
            } catch (ServiceException e) {
                detail.add(Ui.kv("Requirements", e.getMessage()));
            }
            detail.add(Box.createVerticalStrut(12));
            EligibilityResult r = results.get(d.driveId());
            if (r != null) {
                JPanel box = Ui.vstack(0);
                box.setOpaque(true);
                box.setBackground(r.eligible() ? Theme.LIGHT_PLUM : Theme.BRICK_BG);
                box.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                box.setAlignmentX(LEFT_ALIGNMENT);
                box.add(Ui.label(r.eligible() ? "ELIGIBLE" : "NOT ELIGIBLE", Theme.sansBold(12),
                        r.eligible() ? Theme.DEEP_PLUM : Theme.BRICK));
                String text = r.eligible() ? "You meet every requirement for this drive."
                        : String.join("\n\n", r.reasons()).replace("  |  ", "\n");
                box.add(Ui.label("<html><div style='width:250px'>" + Ui.escape(text) + "</div></html>", Theme.sans(12), Theme.TEXT));
                detail.add(box);
                detail.add(Box.createVerticalStrut(12));
            }
            Btn check = new Btn("Check eligibility", Btn.Variant.SECONDARY, Glyph.ELIGIBILITY);
            check.addActionListener(e -> {
                EligibilityResult res = load(() -> eligibility.check(Session.studentId(), d.driveId()), null);
                if (res != null) {
                    results.put(d.driveId(), res);
                    info(res.eligible() ? "ELIGIBLE" : "NOT ELIGIBLE", res.eligible()
                            ? "You meet every requirement for " + d.title() + "." : String.join("\n\n", res.reasons()));
                    showDetail(d);
                }
            });
            Btn apply = new Btn("Apply", Btn.Variant.PRIMARY, Glyph.CHECK);
            String status = applied.get(d.driveId());
            if (status != null) {
                apply.setText("Applied (" + Formats.title(status) + ")");
                apply.setEnabled(false);
            } else if (!d.acceptingApplications()) {
                apply.setText("Not accepting applications");
                apply.setEnabled(false);
            }
            apply.addActionListener(e -> apply(d));
            JPanel stack = Ui.vstack(0);
            stack.setAlignmentX(LEFT_ALIGNMENT);
            check.setAlignmentX(LEFT_ALIGNMENT);
            apply.setAlignmentX(LEFT_ALIGNMENT);
            stack.add(check);
            stack.add(Box.createVerticalStrut(8));
            stack.add(apply);
            detail.add(stack);
        }
        detail.revalidate();
        detail.repaint();
    }

    private static <T extends javax.swing.JComponent> T left(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private void apply(Drive d) {
        if (!confirm("Apply to " + d.companyName() + "?", d.position() + ", " + Formats.lpa(d.packageLpa())
                + ". Your eligibility is verified again when you submit.", "Submit application")) {
            return;
        }
        if (run(() -> applications.apply(Session.studentId(), d.driveId()))) {
            info("Application submitted", "Your application to " + d.title() + " has been recorded. Track it under My Applications.");
            refresh();
        }
    }

    @Override
    public void refresh() {
        String sid = Session.studentId();
        applied = load(applications::mine, List.<Application>of()).stream()
                .collect(Collectors.toMap(Application::driveId, Application::status, (a, b) -> a));
        List<Drive> all = load(() -> drives.listForStudents(search.getText()), List.of());
        results.clear();
        for (Drive d : all) {
            try {
                results.put(d.driveId(), eligibility.check(sid, d.driveId()));
            } catch (ServiceException ignored) {
                // drive without criteria: shown without an eligibility badge
            }
        }
        int mode = show.getSelectedIndex();
        List<Drive> rows = all.stream().filter(d -> mode == 0
                || mode == 1 && results.containsKey(d.driveId()) && results.get(d.driveId()).eligible()
                || mode == 2 && d.acceptingApplications()).toList();
        Drive keep = table.selected();
        table.setRows(rows);

        long eligibleCount = all.stream().filter(d -> results.containsKey(d.driveId()) && results.get(d.driveId()).eligible()).count();
        long openCount = all.stream().filter(Drive::acceptingApplications).count();

        kpiTotalDrives.setValue(all.size());
        kpiEligible.setValue(eligibleCount);
        kpiOpen.setValue(openCount);

        int idx = keep == null ? 0 : rows.stream().map(Drive::driveId).toList().indexOf(keep.driveId());
        if (!rows.isEmpty()) {
            int v = table.table().convertRowIndexToView(Math.max(idx, 0));
            table.table().setRowSelectionInterval(v, v);
        }
        showDetail(table.selected());
    }

}
