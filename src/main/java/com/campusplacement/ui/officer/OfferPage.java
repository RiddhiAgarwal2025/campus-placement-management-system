package com.campusplacement.ui.officer;

import com.campusplacement.model.Application;
import com.campusplacement.model.Offer;
import com.campusplacement.service.OfferService;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FilterBar;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class OfferPage extends Page {
    private final OfferService service = new OfferService();
    private final DataTable<Offer> table = new DataTable<Offer>("No offers match", "Issue offers to SELECTED candidates.")
            .col("#", Offer::offerId, 40, Kind.NUMBER)
            .col("Student", Offer::studentName, 150)
            .col("Dept", Offer::deptCode, 55)
            .col("Company", Offer::companyName, 160)
            .col("Position", Offer::position, 160)
            .col("Package", Offer::packageLpa, 110, Kind.MONEY)
            .col("Offer date", Offer::offerDate, 100, Kind.DATE)
            .col("Joining date", Offer::joiningDate, 100, Kind.DATE)
            .col("Status", Offer::status, 100, Kind.BADGE)
            .col("Responded", Offer::respondedAt, 140, Kind.DATE);
    private final HintField search;
    private final JComboBox<String> status;

    public OfferPage() {
        super("Offers", "Offers can only be issued to candidates whose application is SELECTED, once per application.");
        Btn issue = new Btn("Issue offer", Btn.Variant.PRIMARY, Glyph.PLUS);
        issue.addActionListener(e -> issue());
        Btn export = new Btn("Export CSV", Btn.Variant.SECONDARY, Glyph.EXPORT);
        export.addActionListener(e -> ReportsPage.exportCsv(this, table.model(), "offers.csv"));
        addAction(export);
        addAction(issue);
        FilterBar bar = new FilterBar(this::refresh);
        search = bar.search("Search student, company, position");
        status = bar.combo(Ui.filterCombo("All statuses", Arrays.stream(Offer.Status.values()).map(Enum::name).toList()));
        Btn edit = new Btn("Edit terms", Btn.Variant.SECONDARY, Glyph.EDIT);
        edit.addActionListener(e -> editTerms());
        Btn withdraw = new Btn("Withdraw", Btn.Variant.DANGER, Glyph.TRASH);
        withdraw.addActionListener(e -> {
            Offer o = table.selected();
            if (need(o, "an offer") && confirm("Withdraw offer to " + o.studentName() + "?",
                    "The pending offer is deleted. The application stays SELECTED.", "Withdraw")
                    && run(() -> service.withdraw(o))) {
                refresh();
            }
        });
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(bar, BorderLayout.WEST);
        top.add(Ui.rightRow(edit, withdraw), BorderLayout.EAST);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        setBody(body);
    }

    private void issue() {
        List<Application> candidates = load(service::candidatesForOffer, null);
        if (candidates == null) {
            return;
        }
        if (candidates.isEmpty()) {
            info("No candidates awaiting offers", "Every SELECTED candidate already has an offer. Candidates become "
                    + "SELECTED after passing all selection rounds (or when marked Selected on the Applications page).");
            return;
        }
        JComboBox<Application> app = Ui.combo(candidates);
        app.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                Application a = (Application) v;
                super.getListCellRendererComponent(l, a == null ? "" : a.studentName() + " (" + a.studentId() + ") — "
                        + a.companyName() + ", " + a.position(), i, s, f);
                setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return this;
            }
        });
        JTextField pkg = Ui.field(candidates.get(0).packageLpa().toPlainString());
        app.addActionListener(e -> {
            Application a = (Application) app.getSelectedItem();
            if (a != null) {
                pkg.setText(a.packageLpa().toPlainString());
            }
        });
        JTextField od = Ui.field(Formats.iso(LocalDate.now()));
        JTextField jd = Ui.field(Formats.iso(LocalDate.now().plusMonths(8).withDayOfMonth(1)));
        FormDialog f = new FormDialog(this, "Issue offer", "Only SELECTED candidates without an offer are listed.")
                .field("Candidate", app).field("Package (LPA)", pkg).field("Offer date", od).field("Joining date", jd);
        if (f.open("Issue offer", () -> {
            Application a = (Application) app.getSelectedItem();
            service.issue(a == null ? -1 : a.applicationId(), pkg.getText(), od.getText(), jd.getText());
        })) {
            refresh();
        }
    }

    private void editTerms() {
        Offer o = table.selected();
        if (!need(o, "an offer")) {
            return;
        }
        JTextField pkg = Ui.field(o.packageLpa().toPlainString());
        JTextField od = Ui.field(Formats.iso(o.offerDate()));
        JTextField jd = Ui.field(Formats.iso(o.joiningDate()));
        FormDialog f = new FormDialog(this, "Edit offer #" + o.offerId(), "Only pending offers can be changed.")
                .field("Package (LPA)", pkg).field("Offer date", od).field("Joining date", jd);
        if (f.open("Save changes", () -> service.updateTerms(o, pkg.getText(), od.getText(), jd.getText()))) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        List<Offer> rows = load(() -> service.list(search.getText(), (String) status.getSelectedItem()), List.of());
        table.setRows(rows);
        Map<String, Long> c = rows.stream().collect(Collectors.groupingBy(Offer::status, Collectors.counting()));
        setSubtitle(rows.size() + " offer(s)   |   Pending " + c.getOrDefault("PENDING", 0L) + "   Accepted "
                + c.getOrDefault("ACCEPTED", 0L) + "   Rejected " + c.getOrDefault("REJECTED", 0L));
    }
}
