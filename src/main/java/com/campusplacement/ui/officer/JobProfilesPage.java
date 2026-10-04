package com.campusplacement.ui.officer;

import com.campusplacement.model.Company;
import com.campusplacement.model.JobProfile;
import com.campusplacement.model.Skill;
import com.campusplacement.service.CompanyService;
import com.campusplacement.service.SkillService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.CheckList;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class JobProfilesPage extends Page {
    private final CompanyService service = new CompanyService();
    private final DataTable<JobProfile> table = jobTable();
    private final StatTile kpiTotalJobs = new StatTile("Job Profiles", "Available career profiles", false);
    private final StatTile kpiAvgPkg = new StatTile("Average Package", "Mean compensation", false);
    private final StatTile kpiMaxPkg = new StatTile("Top Package", "Highest offering", true);
    private final HintField search;
    private JComboBox<Company> company;
    private boolean loading;

    static DataTable<JobProfile> jobTable() {
        return new DataTable<JobProfile>("No job profiles", "Create a job profile for a company.")
                .col("Company", JobProfile::companyName, 170)
                .col("Position", JobProfile::position, 180)
                .col("Package", JobProfile::packageLpa, 110, Kind.MONEY)
                .col("Location", JobProfile::location, 100)
                .col("Required skills", j -> j.requiredSkills().stream().map(Skill::name).collect(Collectors.joining(", ")), 200)
                .col("Description", JobProfile::description, 260);
    }

    public JobProfilesPage() {
        super("Job Profiles", "Positions offered by each company, with package, location and skills.");
        Btn add = new Btn("New job profile", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> { if (edit(this, null, (Company) company.getSelectedItem())) { refresh(); } });
        addAction(add);
        FilterBar bar = new FilterBar(() -> { if (!loading) { reloadTable(); } });
        search = bar.search("Search company, position, location");
        company = bar.combo(Ui.filterCombo("All companies", List.of()));
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> {
            if (need(table.selected(), "a job profile") && edit(this, table.selected(), null)) {
                refresh();
            }
        });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> {
            JobProfile j = table.selected();
            if (need(j, "a job profile") && confirmDelete(j.position() + " at " + j.companyName())
                    && run(() -> service.deleteJob(j.jobId()))) {
                refresh();
            }
        });
        table.onDoubleClick(j -> { if (edit(this, j, null)) { refresh(); } });

        KpiBanner kpiBanner = new KpiBanner(kpiTotalJobs, kpiAvgPkg, kpiMaxPkg);

        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, del), BorderLayout.EAST);

        cardContent.add(top, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    /** Shared create/edit dialog for job profiles. Returns true if saved. */
    static boolean edit(Component parent, JobProfile j, Company preset) {
        CompanyService service = new CompanyService();
        List<Company> companies = service.list("");
        List<Skill> skills = new SkillService().list("");
        JComboBox<Company> co = Ui.combo(companies);
        Integer selectedId = j != null ? Integer.valueOf(j.companyId()) : preset != null ? Integer.valueOf(preset.companyId()) : null;
        if (selectedId != null) {
            companies.stream().filter(c -> c.companyId() == selectedId).findFirst().ifPresent(co::setSelectedItem);
        }
        JTextField position = Ui.field(j == null ? "" : j.position());
        JTextField pkg = Ui.field(j == null ? "" : j.packageLpa().toPlainString());
        JTextField location = Ui.field(j == null ? "" : j.location());
        JTextArea desc = Ui.area(j == null ? "" : j.description(), 3);
        List<Integer> have = j == null ? List.of() : j.requiredSkills().stream().map(Skill::skillId).toList();
        CheckList<Skill> sk = new CheckList<>(skills, Skill::name, have, Skill::skillId, 3);
        FormDialog f = new FormDialog(parent, j == null ? "New job profile" : "Edit job profile",
                "Required skills describe the role; drive eligibility skills are set per drive.")
                .field("Company", co).field("Position", position).field("Package (LPA)", pkg).field("Location", location)
                .field("Description", Ui.areaScroll(desc)).field("Required skills", sk);
        return f.open(j == null ? "Create job profile" : "Save changes", () -> {
            if (j == null) {
                service.createJob((Company) co.getSelectedItem(), position.getText(), desc.getText(), pkg.getText(),
                        location.getText(), sk.selected());
            } else {
                service.updateJob(j.jobId(), (Company) co.getSelectedItem(), position.getText(), desc.getText(),
                        pkg.getText(), location.getText(), sk.selected());
            }
        });
    }

    @Override
    public void refresh() {
        loading = true;
        Object sel = company.getSelectedItem();
        company.removeAllItems();
        company.addItem(null);
        List<Company> all = load(() -> service.list(""), List.of());
        all.forEach(company::addItem);
        if (sel instanceof Company s) {
            all.stream().filter(c -> c.companyId() == s.companyId()).findFirst().ifPresent(company::setSelectedItem);
        }
        loading = false;
        reloadTable();
    }

    private void reloadTable() {
        Company c = (Company) company.getSelectedItem();
        List<JobProfile> list = load(() -> service.jobs(c == null ? null : c.companyId(), search.getText()), List.of());
        table.setRows(list);

        int count = list.size();
        double avg = list.stream().mapToDouble(j -> j.packageLpa() != null ? j.packageLpa().doubleValue() : 0.0).average().orElse(0.0);
        BigDecimal max = list.stream().map(JobProfile::packageLpa).filter(p -> p != null).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        kpiTotalJobs.setValue(count);
        kpiAvgPkg.setValue(avg > 0 ? String.format("\u20B9 %.2f LPA", avg) : "—");
        kpiMaxPkg.setValue(max.compareTo(BigDecimal.ZERO) > 0 ? Formats.lpa(max) : "—");
    }
}
