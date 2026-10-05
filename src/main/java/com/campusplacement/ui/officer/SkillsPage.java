package com.campusplacement.ui.officer;

import com.campusplacement.model.Skill;
import com.campusplacement.service.SkillService;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Ui;
import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SkillsPage extends Page implements Searchable {
    private final SkillService service = new SkillService();
    private final DataTable<Skill> table = new DataTable<Skill>("No skills match", "Add a skill or change the search.")
            .col("Skill", Skill::name, 260)
            .col("Category", Skill::category, 200)
            .col("Students with skill", Skill::studentCount, 140, Kind.NUMBER);
    private final StatTile kpiTotalSkills = new StatTile("Skills Catalogue", "Registered competencies", false);
    private final StatTile kpiCategories = new StatTile("Skill Categories", "Functional domains", false);
    private final StatTile kpiTopSkill = new StatTile("Most Endorsed", "Highest student count", false);
    private final HintField search;

    public SkillsPage() {
        super("Skills", "Skills catalogue used for student profiles, job profiles and eligibility rules.");
        Btn add = new Btn("Add skill", Btn.Variant.PRIMARY, Glyph.PLUS);
        add.addActionListener(e -> edit(null));
        Btn assign = new Btn("Assign to students", Btn.Variant.SECONDARY, Glyph.STUDENTS);
        assign.addActionListener(e -> {
            info("Assign skills", "Skills are assigned from a student's record. Open Students, select a student and "
                    + "choose Details, then use the Skills tab.");
            MainFrame.navigate("students");
        });
        addAction(assign);
        addAction(add);
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search skill or category");
        Btn edit = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> { if (need(table.selected(), "a skill")) { edit(table.selected()); } });
        Btn del = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        del.addActionListener(e -> {
            Skill s = table.selected();
            if (need(s, "a skill") && confirmDelete(s.name()) && run(() -> service.delete(s))) {
                refresh();
            }
        });

        KpiBanner kpiBanner = new KpiBanner(kpiTotalSkills, kpiCategories, kpiTopSkill);

        JPanel cardContent = new JPanel(new BorderLayout(0, 12));
        cardContent.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, del), BorderLayout.EAST);
        table.onDoubleClick(this::edit);

        cardContent.add(top, BorderLayout.NORTH);
        cardContent.add(table, BorderLayout.CENTER);

        JPanel tableCard = Ui.card(cardContent, 18);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(kpiBanner, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        setBody(body);
    }

    private void edit(Skill s) {
        JTextField name = Ui.field(s == null ? "" : s.name());
        JTextField cat = Ui.field(s == null ? "General" : s.category());
        FormDialog f = new FormDialog(this, s == null ? "Add skill" : "Edit skill", null)
                .field("Skill name", name).field("Category", cat);
        if (f.open(s == null ? "Add skill" : "Save changes", () -> {
            if (s == null) {
                service.create(name.getText(), cat.getText());
            } else {
                service.update(s.skillId(), name.getText(), cat.getText());
            }
        })) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        List<Skill> list = load(() -> service.list(search.getText()), List.of());
        table.setRows(list);

        int count = list.size();
        long cats = list.stream().map(Skill::category).filter(c -> c != null && !c.isBlank()).distinct().count();
        Skill top = list.stream().max(Comparator.comparingInt(Skill::studentCount)).orElse(null);

        kpiTotalSkills.setValue(count);
        kpiCategories.setValue(cats);
        kpiTopSkill.setValue(top != null ? top.name() + " (" + top.studentCount() + ")" : "—");
    }

    @Override
    public void setSearch(String text) {
        if (search != null) {
            search.setText(text == null ? "" : text);
            refresh();
        }
    }

    @Override
    public String getSearch() {
        return search == null ? "" : search.getText();
    }
}
