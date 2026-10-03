package com.campusplacement.ui.student;

import com.campusplacement.model.Offer;
import com.campusplacement.service.OfferService;
import com.campusplacement.ui.components.Badge;
import com.campusplacement.ui.components.Btn;
import com.campusplacement.ui.components.Icons.Glyph;
import com.campusplacement.ui.components.Page;
import com.campusplacement.ui.components.Theme;
import com.campusplacement.ui.components.Ui;
import com.campusplacement.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

public class MyOffersPage extends Page {
    private final OfferService service = new OfferService();
    private final JPanel list = Ui.vstack(0);

    public MyOffersPage() {
        super("My Offers", "Review your offers. Accepting or rejecting is final, and only one offer can be accepted.");
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(list, BorderLayout.NORTH);
        setBody(Ui.scroll(wrap));
    }

    private JPanel card(Offer o) {
        JPanel c = new JPanel(new BorderLayout(24, 0));
        c.setBackground(Theme.SURFACE);
        boolean pending = "PENDING".equals(o.status());
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, pending ? Theme.PLUM : Theme.BORDER),
                        BorderFactory.createMatteBorder(1, 0, 1, 1, Theme.BORDER)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        JPanel left = Ui.vstack(0);
        left.add(Ui.label(o.companyName(), Theme.serif(22), Theme.TEXT));
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
        p.add(Ui.label(value, Theme.serif(18), Theme.TEXT));
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
        List<Offer> offers = load(service::mine, List.of());
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
        list.revalidate();
        list.repaint();
    }
}
