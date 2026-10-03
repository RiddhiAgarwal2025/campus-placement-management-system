package com.campusplacement.ui.officer;

import com.campusplacement.model.Application;
import com.campusplacement.model.Drive;
import com.campusplacement.model.EligibilityCriteria;
import com.campusplacement.model.SelectionRound;
import com.campusplacement.service.ApplicationService;
import com.campusplacement.service.EligibilityService;
import com.campusplacement.service.SelectionService;
import com.campusplacement.service.ServiceException;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.Tabs;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Read-only overview of a drive: facts, criteria, rounds and applicants. */
public class DriveDetailDialog extends JDialog {
    public DriveDetailDialog(Component parent, Drive d) {
        super(SwingUtilities.getWindowAncestor(parent), "Drive #" + d.driveId(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Theme.PLUM),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        JPanel head = Ui.vstack(0);
        head.add(Ui.label(d.companyName(), Theme.serif(24), Theme.TEXT));
        head.add(Ui.muted(d.position() + "   |   " + Formats.lpa(d.packageLpa()) + "   |   " + d.location()
                + "   |   " + d.status()));
        root.add(head, BorderLayout.NORTH);

        JPanel facts = Ui.vstack(0);
        facts.add(Ui.kv("Drive date", Formats.date(d.driveDate()), 520));
        facts.add(Ui.kv("Application deadline", Formats.date(d.deadline()) + " (" + Formats.relative(d.deadline()) + ")", 520));
        facts.add(Ui.kv("Venue", d.venue(), 520));
        facts.add(Ui.kv("Applications", String.valueOf(d.applicationCount()), 520));
        try {
            EligibilityCriteria cr = new EligibilityService().criteria(d.driveId());
            facts.add(Ui.kv("Minimum CGPA", cr.minCgpa().toPlainString(), 520));
            facts.add(Ui.kv("Maximum backlogs", String.valueOf(cr.maxBacklogs()), 520));
            facts.add(Ui.kv("Graduation year", cr.graduationYear() == null ? "Any" : String.valueOf(cr.graduationYear()), 520));
            facts.add(Ui.kv("Departments", cr.departments().isEmpty() ? "All departments"
                    : String.join(", ", cr.departments().stream().map(x -> x.code()).toList()), 520));
            facts.add(Ui.kv("Required skills", cr.skills().isEmpty() ? "None"
                    : String.join(", ", cr.skills().stream().map(x -> x.name()).toList()), 520));
        } catch (ServiceException e) {
            facts.add(Ui.kv("Eligibility", e.getMessage(), 520));
        }

        DataTable<SelectionRound> rounds = new DataTable<SelectionRound>("No rounds yet", "Rounds are added from Selection.")
                .col("Round", SelectionRound::sequenceNo, 60, Kind.NUMBER)
                .col("Name", SelectionRound::name, 200)
                .col("Date", SelectionRound::roundDate, 110, Kind.DATE)
                .col("Passed", SelectionRound::passCount, 70, Kind.NUMBER)
                .col("Failed", SelectionRound::failCount, 70, Kind.NUMBER)
                .col("Status", SelectionRound::status, 110, Kind.BADGE);
        DataTable<Application> apps = new DataTable<Application>("No applications", "No student has applied yet.")
                .col("Student", Application::studentName, 170)
                .col("Dept", Application::deptCode, 60)
                .col("CGPA", Application::cgpa, 60, Kind.NUMBER)
                .col("Applied", Application::appliedAt, 140, Kind.DATE)
                .col("Status", Application::status, 110, Kind.BADGE);
        try {
            rounds.setRows(new SelectionService().rounds(d.driveId()));
            apps.setRows(new ApplicationService().list("", d.driveId(), null, null, null));
        } catch (ServiceException e) {
            rounds.setEmptyMessage("Could not load", e.getMessage());
        }
        root.add(new Tabs().tab("Overview", Ui.card(facts, 18)).tab("Applicants (" + d.applicationCount() + ")", apps)
                .tab("Selection rounds", rounds), BorderLayout.CENTER);
        Btn close = new Btn("Close", Btn.Variant.SECONDARY);
        close.addActionListener(e -> dispose());
        root.add(Ui.rightRow(close), BorderLayout.SOUTH);
        setContentPane(root);
        setSize(820, 600);
        setLocationRelativeTo(getOwner());
    }

}
