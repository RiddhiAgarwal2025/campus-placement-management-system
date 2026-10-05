package com.campusplacement.ui.student;

import com.campusplacement.model.Offer;
import com.campusplacement.service.OfferService;
import com.campusplacement.ui.components.Badge;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.KpiBanner;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Searchable;
import com.campusplacement.ui.components.StatTile;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

public class MyOffersPage extends Page implements Searchable {
    private final OfferService service = new OfferService();
    private final JPanel list = Ui.vstack(0);
    private String filterQuery = "";
    private final StatTile kpiTotal = new StatTile("Total Offers", "Extended to you", false);
    private final StatTile kpiAccepted = new StatTile("Accepted", "Confirmed placement", false);
    private final StatTile kpiMaxPkg = new StatTile("Top Package", "Highest offering", false);

    public MyOffersPage() {
        super("My Offers", "Review your offers. Accepting or rejecting is final, and only one offer can be accepted.");
        KpiBanner kpiBanner = new KpiBanner(kpiTotal, kpiAccepted, kpiMaxPkg);

        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(false);
        wrap.add(kpiBanner, BorderLayout.NORTH);
        wrap.add(list, BorderLayout.CENTER);
        setBody(Ui.scroll(wrap));
    }

    private JPanel card(Offer o) {
        boolean pending = "PENDING".equals(o.status());
        JPanel c = new JPanel(new BorderLayout(24, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        JPanel left = Ui.vstack(0);
        left.add(Ui.label(o.companyName(), Theme.sansBold(18), Theme.TEXT));
        left.add(Ui.body(o.position() + ", " + o.location()));
        left.add(Box.createVerticalStrut(10));
        left.add(Badge.on(o.status(), Theme.SURFACE));
        c.add(left, BorderLayout.WEST);

        JPanel facts = new JPanel(new GridLayout(1, 3, 18, 0));
        facts.setOpaque(false);
        facts.add(fact("Package", Formats.lpa(o.packageLpa())));
        facts.add(fact("Offer date", Formats.date(o.offerDate())));
        facts.add(fact("Joining date", Formats.date(o.joiningDate())));
        c.add(facts, BorderLayout.CENTER);

        if (pending) {
            Btn accept = new Btn("Accept offer", Btn.Variant.PRIMARY, Glyph.CHECK);
            accept.addActionListener(e -> respond(o, true));
            Btn reject = new Btn("Reject", Btn.Variant.DANGER);
            reject.addActionListener(e -> respond(o, false));
            JPanel b = Ui.vstack(0);
            b.add(accept);
            b.add(Box.createVerticalStrut(8));
            b.add(reject);
            accept.setMaximumSize(new Dimension(150, 36));
            reject.setMaximumSize(new Dimension(150, 36));
            c.add(b, BorderLayout.EAST);
        } else {
            c.add(Ui.muted("Responded " + Formats.dateTime(o.respondedAt())), BorderLayout.EAST);
        }
        return c;
    }

    private JPanel fact(String label, String value) {
        JPanel p = Ui.vstack(0);
        p.add(Ui.muted(label));
        p.add(Ui.label(value, Theme.sansBold(16), Theme.TEXT));
        return p;
    }

    private void respond(Offer o, boolean accept) {
        String verb = accept ? "Accept" : "Reject";
        String msg = accept ? "You are accepting " + o.position() + " at " + o.companyName() + " (" + Formats.lpa(o.packageLpa())
                + "). This decision is final, and you will not be able to accept another offer."
                : "You are declining the offer from " + o.companyName() + ". This decision is final.";
        boolean ok = accept ? confirm(verb + " this offer?", msg, verb + " offer")
                : com.campusplacement.ui.components.Dialogs.confirmDanger(this, verb + " this offer?", msg, verb + " offer");
        if (ok && run(() -> service.respond(o.offerId(), accept))) {
            info(accept ? "Offer accepted" : "Offer rejected", accept
                    ? "Congratulations. Your acceptance has been recorded with the placement office."
                    : "Your response has been recorded.");
            refresh();
        }
    }

    @Override
    public void refresh() {
        List<Offer> all = load(service::mine, List.of());
        List<Offer> offers = filterQuery.isEmpty() ? all : all.stream().filter(o ->
                (o.companyName() != null && o.companyName().toLowerCase().contains(filterQuery)) ||
                (o.position() != null && o.position().toLowerCase().contains(filterQuery)) ||
                (o.status() != null && o.status().toLowerCase().contains(filterQuery))).toList();
        list.removeAll();
        if (offers.isEmpty()) {
            JPanel empty = Ui.vstack(0);
            empty.add(Ui.heading("No offers yet"));
            empty.add(Box.createVerticalStrut(4));
            empty.add(Ui.muted("Offers appear here after you are selected in a drive."));
            list.add(Ui.card(empty, 28));
        }
        for (Offer o : offers) {
            list.add(card(o));
            list.add(Box.createVerticalStrut(12));
        }

        long accepted = offers.stream().filter(o -> "ACCEPTED".equals(o.status())).count();
        BigDecimal maxPkg = offers.stream().map(Offer::packageLpa).filter(p -> p != null).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        kpiTotal.setValue(offers.size());
        kpiAccepted.setValue(accepted);
        kpiMaxPkg.setValue(maxPkg.compareTo(BigDecimal.ZERO) > 0 ? Formats.lpa(maxPkg) : "—");

        list.revalidate();
        list.repaint();
    }

    @Override
    public void setSearch(String text) {
        this.filterQuery = text == null ? "" : text.trim().toLowerCase();
        refresh();
    }

    @Override
    public String getSearch() {
        return filterQuery;
    }
}
