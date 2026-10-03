package com.campusplacement.ui.officer;

import com.campusplacement.model.Drive;
import com.campusplacement.model.RoundCandidate;
import com.campusplacement.model.SelectionRound;
import com.campusplacement.service.DriveService;
import com.campusplacement.service.SelectionService;
import com.campusplacement.ui.MainFrame;
import com.campusplacement.ui.components.Badge;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.DataTable;
import com.campusplacement.ui.components.DataTable.Kind;
import com.campusplacement.ui.components.FormDialog;
import com.campusplacement.ui.components.HintField;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Drive → rounds → candidates → results. */
public class SelectionPage extends Page {
    private final SelectionService service = new SelectionService();
    private final JComboBox<Drive> drive = Ui.combo(List.of());
    private final JPanel stepper = Ui.vstack(0);
    private final JLabel roundTitle = Ui.heading("Select a round");
    private final JLabel roundHint = Ui.muted(" ");
    private final HintField remarks = new HintField("Remarks (optional)");
    private final DataTable<RoundCandidate> candidates = new DataTable<RoundCandidate>("No candidates",
            "Select a drive with applications, then a round.")
            .col("Student ID", RoundCandidate::studentId, 90)
            .col("Student", RoundCandidate::studentName, 160)
            .col("Dept", RoundCandidate::deptCode, 55)
            .col("Previous result", c -> c.previousResult() == null ? (isFirst() ? "—" : "AWAITING") : c.previousResult(), 120, Kind.BADGE)
            .col("Current result", c -> c.currentResult() == null ? "" : c.currentResult(), 110, Kind.BADGE)
            .col("Remarks", RoundCandidate::remarks, 160)
            .col("Application", RoundCandidate::applicationStatus, 110, Kind.BADGE)
            .multiSelect();
    private List<SelectionRound> rounds = List.of();
    private SelectionRound current;
    private boolean loading;

    public SelectionPage() {
        super("Selection", "Run ordered selection rounds. A candidate enters a round only after passing the previous one.");
        drive.setPreferredSize(new Dimension(360, 34));
        drive.addActionListener(e -> { if (!loading) { loadRounds(null); } });
        Btn addRound = new Btn("Add round", Btn.Variant.PRIMARY, Glyph.PLUS);
        addRound.addActionListener(e -> editRound(null));

        Btn editRound = new Btn("Edit", Btn.Variant.SECONDARY, Glyph.EDIT);
        editRound.addActionListener(e -> { if (need(current, "a round")) { editRound(current); } });
        Btn delRound = new Btn("Delete", Btn.Variant.DANGER, Glyph.TRASH);
        delRound.addActionListener(e -> {
            if (need(current, "a round") && confirmDelete("round " + current.sequenceNo() + " (" + current.name() + ")")
                    && run(() -> service.deleteRound(current.roundId()))) {
                loadRounds(null);
            }
        });
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(310, 200));
        JPanel lh = new JPanel(new BorderLayout());
        lh.setOpaque(false);
        lh.add(Ui.heading("Rounds"), BorderLayout.WEST);
        lh.add(Ui.rightRow(editRound, delRound), BorderLayout.EAST);
        left.add(lh, BorderLayout.NORTH);
        JPanel stepWrap = new JPanel(new BorderLayout());
        stepWrap.setOpaque(false);
        stepWrap.add(stepper, BorderLayout.NORTH);
        left.add(Ui.scroll(stepWrap), BorderLayout.CENTER);

        Btn pass = new Btn("Mark PASS", Btn.Variant.PRIMARY, Glyph.CHECK);
        pass.addActionListener(e -> record("PASS"));
        Btn fail = new Btn("Mark FAIL", Btn.Variant.DANGER, Glyph.CLOSE);
        fail.addActionListener(e -> record("FAIL"));
        Btn clear = new Btn("Clear result", Btn.Variant.SECONDARY);
        clear.addActionListener(e -> clearResults());
        Btn offer = new Btn("Go to offers", Btn.Variant.SECONDARY, Glyph.OFFERS);
        offer.addActionListener(e -> MainFrame.navigate("offers"));
        addAction(offer);
        addAction(addRound);
        remarks.setFont(Theme.sans(13));
        remarks.setBorder(Ui.FIELD_BORDER);
        remarks.setPreferredSize(new Dimension(200, 34));
        JPanel rh = Ui.vstack(0);
        rh.add(roundTitle);
        rh.add(roundHint);
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(Ui.row(remarks, pass, fail, clear), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        JPanel rtop = new JPanel(new BorderLayout(0, 10));
        rtop.setOpaque(false);
        rtop.add(rh, BorderLayout.NORTH);
        rtop.add(actions, BorderLayout.SOUTH);
        right.add(rtop, BorderLayout.NORTH);
        right.add(candidates, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);
        center.add(left, BorderLayout.WEST);
        center.add(Ui.card(right, 18), BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(Ui.row(Ui.fieldLabel("Drive"), drive), BorderLayout.NORTH);
        body.add(center, BorderLayout.CENTER);
        setBody(body);
    }

    private boolean isFirst() {
        return current != null && service.previousRound(rounds, current) == null;
    }

    private void editRound(SelectionRound r) {
        Drive d = (Drive) drive.getSelectedItem();
        if (!need(d, "a drive")) {
            return;
        }
        int nextSeq = rounds.stream().mapToInt(SelectionRound::sequenceNo).max().orElse(0) + 1;
        JTextField name = Ui.field(r == null ? "" : r.name());
        JTextField seq = Ui.field(String.valueOf(r == null ? nextSeq : r.sequenceNo()));
        JTextField date = Ui.field(r == null ? Formats.iso(d.driveDate()) : Formats.iso(r.roundDate()));
        JComboBox<String> status = Ui.combo(SelectionService.ROUND_STATUSES);
        status.setSelectedItem(r == null ? "UPCOMING" : r.status());
        FormDialog f = new FormDialog(this, r == null ? "Add round" : "Edit round",
                "Sequence numbers order the rounds and must be unique within the drive.")
                .field("Round name", name).field("Sequence", seq).field("Date (optional)", date).field("Status", status);
        if (f.open(r == null ? "Add round" : "Save changes", () -> {
            if (r == null) {
                service.addRound(d.driveId(), name.getText(), seq.getText(), date.getText(), (String) status.getSelectedItem());
            } else {
                service.updateRound(r.roundId(), d.driveId(), name.getText(), seq.getText(), date.getText(),
                        (String) status.getSelectedItem());
            }
        })) {
            loadRounds(r == null ? null : r.roundId());
        }
    }

    private void record(String result) {
        List<RoundCandidate> sel = candidates.selectedRows();
        if (!need(current, "a round") || !need(sel.isEmpty() ? null : sel, "one or more candidates")) {
            return;
        }
        for (RoundCandidate c : sel) {
            if (!run(() -> service.recordResult(current, c.applicationId(), result, remarks.getText()))) {
                break;
            }
        }
        remarks.setText("");
        loadRounds(current.roundId());
    }

    private void clearResults() {
        List<RoundCandidate> sel = candidates.selectedRows();
        if (!need(current, "a round") || !need(sel.isEmpty() ? null : sel, "one or more candidates")) {
            return;
        }
        if (!confirm("Clear results?", "Results for " + sel.size() + " candidate(s) in " + current.name()
                + " will be removed.", "Clear")) {
            return;
        }
        for (RoundCandidate c : sel) {
            if (c.currentResult() != null && !run(() -> service.clearResult(current, c.applicationId()))) {
                break;
            }
        }
        loadRounds(current.roundId());
    }

    private void loadRounds(Integer keepRoundId) {
        Drive d = (Drive) drive.getSelectedItem();
        rounds = d == null ? List.of() : load(() -> service.rounds(d.driveId()), List.of());
        current = rounds.stream().filter(r -> keepRoundId != null && r.roundId() == keepRoundId).findFirst()
                .orElse(rounds.isEmpty() ? null : rounds.get(0));
        renderStepper();
        loadCandidates();
    }

    private void renderStepper() {
        stepper.removeAll();
        if (rounds.isEmpty()) {
            stepper.add(Ui.muted("<html><div style='width:240px'>No rounds yet. Use Add round to create the first "
                    + "round, for example an aptitude test.</div></html>"));
        }
        for (SelectionRound r : rounds) {
            stepper.add(new Step(r, current != null && r.roundId() == current.roundId()));
            stepper.add(Box.createVerticalStrut(8));
        }
        stepper.revalidate();
        stepper.repaint();
    }

    private void loadCandidates() {
        if (current == null) {
            roundTitle.setText("No round selected");
            roundHint.setText(" ");
            candidates.setRows(List.of());
            return;
        }
        SelectionRound prev = service.previousRound(rounds, current);
        roundTitle.setText("Round " + current.sequenceNo() + ": " + current.name());
        roundHint.setText(prev == null ? "First round: every applicant can receive a result."
                : "Only candidates who passed " + prev.name() + " can receive a result. Others show AWAITING or FAIL.");
        candidates.setRows(load(() -> service.candidates(current), List.of()));
    }

    @Override
    public void refresh() {
        loading = true;
        Drive keep = (Drive) drive.getSelectedItem();
        List<Drive> all = load(() -> new DriveService().list("", null, null), List.<Drive>of()).stream()
                .filter(d -> d.applicationCount() > 0 || !"UPCOMING".equals(d.status())).toList();
        drive.removeAllItems();
        all.forEach(drive::addItem);
        if (keep != null) {
            all.stream().filter(d -> d.driveId() == keep.driveId()).findFirst().ifPresent(drive::setSelectedItem);
        } else {
            all.stream().filter(d -> "CLOSED".equals(d.status())).findFirst().ifPresent(drive::setSelectedItem);
        }
        loading = false;
        loadRounds(current == null ? null : current.roundId());
    }

    /** One round in the vertical stepper. */
    private class Step extends JPanel {
        private final boolean active;

        Step(SelectionRound r, boolean active) {
            super(new BorderLayout(12, 0));
            this.active = active;
            setBackground(active ? Theme.SURFACE : Theme.BG);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(active ? Theme.PLUM : Theme.BORDER),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
            JLabel num = new JLabel(String.valueOf(r.sequenceNo()), JLabel.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor("COMPLETED".equals(r.status()) ? Theme.PLUM : active ? Theme.LIGHT_PLUM : Theme.SURFACE);
                    g2.fillOval(0, 0, 29, 29);
                    g2.setColor(Theme.PLUM);
                    g2.drawOval(0, 0, 29, 29);
                    super.paintComponent(g);
                }
            };
            num.setFont(Theme.serif(15));
            num.setForeground("COMPLETED".equals(r.status()) ? Theme.SURFACE : Theme.DEEP_PLUM);
            num.setPreferredSize(new Dimension(30, 30));
            JPanel numWrap = new JPanel(new BorderLayout());
            numWrap.setOpaque(false);
            numWrap.add(num, BorderLayout.NORTH);
            add(numWrap, BorderLayout.WEST);
            JPanel text = Ui.vstack(0);
            text.add(Ui.label(r.name(), active ? Theme.sansBold(13) : Theme.sans(13), Theme.TEXT));
            text.add(Ui.muted((r.roundDate() == null ? "Date not set" : Formats.date(r.roundDate()))
                    + "   " + r.passCount() + " passed, " + r.failCount() + " failed"));
            text.add(Box.createVerticalStrut(6));
            Badge b = Badge.on(r.status(), active ? Theme.SURFACE : Theme.BG);
            b.setAlignmentX(LEFT_ALIGNMENT);
            text.add(b);
            add(text, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    current = r;
                    renderStepper();
                    loadCandidates();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                g.setColor(Theme.PLUM);
                g.fillRect(0, 0, 3, getHeight());
            }
        }
    }

}
