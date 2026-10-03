package com.campusplacement.ui.officer;

import com.campusplacement.model.Company;
import com.campusplacement.model.JobProfile;
import com.campusplacement.service.CompanyService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CompaniesPage extends Page {
    private final CompanyService service = new CompanyService();
    private final DataTable<Company> table = new DataTable<Company>("No companies match", "Add a recruiting company.")
            .col("Company", Company::name, 190)
            .col("Industry", Company::industry, 150)
            .col("Contact", Company::contactPerson, 130)
            .col("Jobs", Company::jobCount, 50, Kind.NUMBER)
            .col("Drives", Company::driveCount, 55, Kind.NUMBER);
    private final DataTable<JobProfile> jobs = new DataTable<JobProfile>("No job profiles", "Add a job profile for this company.")
            .col("Position", JobProfile::position, 170)
            .col("Package", JobProfile::packageLpa, 110, Kind.MONEY)
            .col("Location", JobProfile::location, 100);
    private final HintField search;
    private final JLabel name = Ui.label("Select a company", Theme.serif(22), Theme.TEXT);
    private final JPanel facts = Ui.vstack(0);

    public CompaniesPage() {
        super("Companies", "Recruiting organisations and the job profiles they hire for.");
        Btn add = new Btn("Add company", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> edit(null));
        addAction(add);
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search name or industry");
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> { if (need(table.selected(), "a company")) { edit(table.selected()); } });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> {
            Company c = table.selected();
            if (need(c, "a company") && confirmDelete(c.name()) && run(() -> service.delete(c))) {
                refresh();
            }
        });
        table.onSelect(this::showDetail);
        table.onDoubleClick(this::edit);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, del), BorderLayout.EAST);

        JPanel detail = new JPanel(new BorderLayout(0, 12));
        detail.setOpaque(false);
        JPanel head = Ui.vstack(0);
        head.add(name);
        head.add(Box.createVerticalStrut(8));
        head.add(facts);
        detail.add(head, BorderLayout.NORTH);
        Btn addJob = new Btn("Add job profile", Btn.Variant.GHOST, Glyph.PLUS);
        addJob.addActionListener(e -> {
            if (need(table.selected(), "a company") && JobProfilesPage.edit(this, null, table.selected())) {
                refresh();
            }
        });
        JPanel jobsBox = new JPanel(new BorderLayout(0, 8));
        jobsBox.setOpaque(false);
        JPanel jh = new JPanel(new BorderLayout());
        jh.setOpaque(false);
        jh.add(Ui.heading("Job profiles"), BorderLayout.WEST);
        jh.add(addJob, BorderLayout.EAST);
        jobsBox.add(jh, BorderLayout.NORTH);
        jobsBox.add(jobs, BorderLayout.CENTER);
        detail.add(jobsBox, BorderLayout.CENTER);
        JPanel detailCard = Ui.card(detail, 18);
        detailCard.setPreferredSize(new Dimension(420, 200));

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(table, BorderLayout.CENTER);
        center.add(detailCard, BorderLayout.EAST);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(center, BorderLayout.CENTER);
        setBody(body);
    }

    private void showDetail(Company c) {
        facts.removeAll();
        if (c == null) {
            name.setText("Select a company");
            jobs.setRows(List.of());
        } else {
            name.setText(c.name());
            facts.add(Ui.kv("Industry", c.industry(), 250));
            facts.add(Ui.kv("Website", c.website(), 250));
            facts.add(Ui.kv("Contact", c.contactPerson(), 250));
            facts.add(Ui.kv("Email", c.contactEmail(), 250));
            facts.add(Ui.kv("Phone", c.contactPhone(), 250));
            facts.add(Ui.kv("Drives held", String.valueOf(c.driveCount()), 250));
            jobs.setRows(load(() -> service.jobs(c.companyId(), ""), List.of()));
        }
        facts.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        facts.revalidate();
        facts.repaint();
    }

    private void edit(Company c) {
        JTextField n = Ui.field(c == null ? "" : c.name());
        JTextField ind = Ui.field(c == null ? "" : c.industry());
        JTextField web = Ui.field(c == null ? "https://" : c.website());
        JTextField person = Ui.field(c == null ? "" : c.contactPerson());
        JTextField email = Ui.field(c == null ? "" : c.contactEmail());
        JTextField phone = Ui.field(c == null ? "" : c.contactPhone());
        FormDialog f = new FormDialog(this, c == null ? "Add company" : "Edit company", "Company names must be unique.")
                .field("Company name", n).field("Industry", ind).field("Website", web).field("Contact person", person)
                .field("Contact email", email).field("Contact phone", phone);
        if (f.open(c == null ? "Add company" : "Save changes", () -> {
            String w = "https://".equals(web.getText().trim()) ? "" : web.getText();
            if (c == null) {
                service.create(n.getText(), ind.getText(), w, person.getText(), email.getText(), phone.getText());
            } else {
                service.update(c.companyId(), n.getText(), ind.getText(), w, person.getText(), email.getText(), phone.getText());
            }
        })) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        Company keep = table.selected();
        table.setRows(load(() -> service.list(search.getText()), List.of()));
        if (keep == null || table.selected() == null) {
            showDetail(table.selected());
        }
    }
}
