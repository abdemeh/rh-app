package com.gestrh.web.admin;

import com.gestrh.entity.Presence;
import com.gestrh.entity.PresenceInterval;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// iText 5 — imports spécifiques
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@WebServlet({"/admin/presences", "/admin/presences/pdf"})
public class AdminPresenceServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    /** Bean pour EL (avec getters) */
    public static class DayAgg {
        private int totalMinutes = 0;
        private int closedIntervals = 0;
        private java.util.List<PresenceInterval> intervals = new java.util.ArrayList<>();

        public int getTotalMinutes() { return totalMinutes; }
        public int getClosedIntervals() { return closedIntervals; }
        public java.util.List<PresenceInterval> getIntervals() { return intervals; }

        void addMinutes(int m) { this.totalMinutes += m; }
        void incClosed() { this.closedIntervals++; }
        void addInterval(PresenceInterval pi) { this.intervals.add(pi); }
    }

    private static class MonthData {
        LinkedHashMap<LocalDate, DayAgg> days = new LinkedHashMap<>();
        int monthTotal = 0;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String servletPath = req.getServletPath();
        if ("/admin/presences/pdf".equals(servletPath)) {
            generatePdf(req, resp);
            return;
        }

        // Liste utilisateurs
        java.util.List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.deletedAt IS NULL ORDER BY u.nom, u.prenom",
                Utilisateur.class).getResultList();
        req.setAttribute("users", users);

        Integer userId = paramInt(req, "userId");
        if (userId == null && !users.isEmpty()) userId = users.get(0).getId();
        req.setAttribute("selectedUserId", userId);

        YearMonth ym = paramYearMonth(req, "month");
        if (ym == null) ym = YearMonth.now();
        req.setAttribute("selectedMonth", ym.toString()); // yyyy-MM

        if (userId != null) {
            MonthData data = loadMonthData(userId, ym);

            // Split en deux colonnes (moitié / moitié en conservant l’ordre chronologique)
            LinkedHashMap<LocalDate, DayAgg> leftDays = new LinkedHashMap<>();
            LinkedHashMap<LocalDate, DayAgg> rightDays = new LinkedHashMap<>();
            int size = data.days.size();
            int mid  = size / 2 + (size % 2); // 1ère moitié = ceil(n/2)
            int i = 0;
            for (Map.Entry<LocalDate, DayAgg> e : data.days.entrySet()) {
                if (i < mid) leftDays.put(e.getKey(), e.getValue());
                else rightDays.put(e.getKey(), e.getValue());
                i++;
            }

            // Totaux par colonne
            int leftTotal = 0, rightTotal = 0;
            for (DayAgg d : leftDays.values())  leftTotal  += d.getTotalMinutes();
            for (DayAgg d : rightDays.values()) rightTotal += d.getTotalMinutes();

            req.setAttribute("leftDays", leftDays);
            req.setAttribute("rightDays", rightDays);
            req.setAttribute("leftTotalMinutes", leftTotal);
            req.setAttribute("rightTotalMinutes", rightTotal);

            // Pour compat descendante, on laisse aussi les attributs “plein mois”
            req.setAttribute("days", data.days); // (si jamais tu en as besoin)
            req.setAttribute("monthTotalMinutes", data.monthTotal);
        } else {
            req.setAttribute("leftDays", new LinkedHashMap<LocalDate, DayAgg>());
            req.setAttribute("rightDays", new LinkedHashMap<LocalDate, DayAgg>());
            req.setAttribute("leftTotalMinutes", 0);
            req.setAttribute("rightTotalMinutes", 0);
            req.setAttribute("days", new LinkedHashMap<LocalDate, DayAgg>());
            req.setAttribute("monthTotalMinutes", 0);
        }

        // Qui est en ligne ?
        req.setAttribute("allUsers", users);
        req.setAttribute("onlineThresholdMinutes", 5);

        req.getRequestDispatcher("/admin/presences.jsp").forward(req, resp);
    }

    private static Integer paramInt(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try { return Integer.valueOf(v.trim()); } catch (Exception e) { return null; }
    }

    private static YearMonth paramYearMonth(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try { return YearMonth.parse(v.trim()); } catch (Exception e) { return null; }
    }

    private MonthData loadMonthData(Integer userId, YearMonth ym) {
        MonthData result = new MonthData();

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        java.util.List<Presence> presences = em.createQuery(
                        "SELECT p FROM Presence p WHERE p.utilisateur.id=:uid AND p.jour BETWEEN :s AND :e ORDER BY p.jour ASC",
                        Presence.class)
                .setParameter("uid", userId)
                .setParameter("s", start)
                .setParameter("e", end)
                .getResultList();

        // Init tous les jours du mois
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.days.put(d, new DayAgg());
        }

        if (!presences.isEmpty()) {
            java.util.List<PresenceInterval> ints = em.createQuery(
                            "SELECT i FROM PresenceInterval i WHERE i.presence IN :ps ORDER BY i.startTime ASC, i.id ASC",
                            PresenceInterval.class)
                    .setParameter("ps", presences)
                    .getResultList();

            Map<Integer, LocalDate> presenceIdToDay = new HashMap<>();
            for (Presence p : presences) {
                presenceIdToDay.put(p.getId(), p.getJour());
            }

            for (PresenceInterval pi : ints) {
                Integer pid = pi.getPresence().getId();
                LocalDate day = presenceIdToDay.get(pid);
                if (day == null) continue;

                DayAgg agg = result.days.get(day);
                if (agg == null) {
                    agg = new DayAgg();
                    result.days.put(day, agg);
                }
                agg.addInterval(pi);

                Integer m = pi.getMinutes();
                if (m == null && pi.getStartTime()!=null && pi.getEndTime()!=null) {
                    long mm = java.time.Duration.between(pi.getStartTime(), pi.getEndTime()).toMinutes();
                    m = (int)Math.max(0, mm);
                }
                if (m != null) {
                    agg.addMinutes(m);
                    result.monthTotal += m;
                }
                if (pi.getEndTime()!=null) {
                    agg.incClosed();
                }
            }
        }

        return result;
    }

    // --- PDF (inchangé) ---
    private void generatePdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer userId = paramInt(req, "userId");
        YearMonth ym = paramYearMonth(req, "month");
        if (userId == null) { resp.sendError(400, "userId manquant"); return; }
        if (ym == null) ym = YearMonth.now();

        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) { resp.sendError(404, "Utilisateur introuvable"); return; }

        MonthData data = loadMonthData(userId, ym);

        resp.setContentType("application/pdf");
        String fn = String.format("presence_%s_%s.pdf",
                (safe(u.getNom())+"_"+safe(u.getPrenom())).replaceAll("\\s+",""),
                ym.toString());
        resp.setHeader("Content-Disposition", "inline; filename=\"" + fn + "\"");

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            OutputStream os = resp.getOutputStream();
            PdfWriter.getInstance(doc, os);
            doc.open();

            Font fTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fSub   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font fNorm  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph head = new Paragraph("GestRH — Rapport de présence", fTitle);
            head.setAlignment(Element.ALIGN_LEFT);
            doc.add(head);

            doc.add(new Paragraph(
                    "Employé : " + safe(u.getPrenom()) + " " + safe(u.getNom()) +
                            "    |    Mois : " + ym.toString(), fNorm));
            doc.add(new Paragraph("Généré le : " + java.time.LocalDateTime.now().toString().replace('T', ' '), fNorm));
            doc.add(new Paragraph(" ", fNorm));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{24f, 24f, 26f, 26f});
            addHeaderCell(table, "Jour");
            addHeaderCell(table, "Sessions terminées");
            addHeaderCell(table, "Total (h:mm)");
            addHeaderCell(table, "Total (min)");

            int monthTotal = 0;
            for (Map.Entry<LocalDate, DayAgg> e : data.days.entrySet()) {
                LocalDate d = e.getKey();
                DayAgg agg = e.getValue();
                int mins = agg.getTotalMinutes();
                monthTotal += mins;

                int h = mins / 60, m = mins % 60;

                addCell(table, d.toString(), fNorm);
                addCell(table, String.valueOf(agg.getClosedIntervals()), fNorm);
                addCell(table, String.format("%d:%02d", h, m), fNorm);
                addCell(table, String.valueOf(mins), fNorm);
            }
            doc.add(table);

            doc.add(new Paragraph(" ", fNorm));
            int th = monthTotal / 60, tm = monthTotal % 60;
            doc.add(new Paragraph(String.format("Total du mois : %d:%02d (h:mm) — %d minutes", th, tm, monthTotal), fSub));
            doc.add(new Paragraph(" ", fNorm));

            doc.add(new Paragraph("Détails par jour", fSub));
            for (Map.Entry<LocalDate, DayAgg> e : data.days.entrySet()) {
                LocalDate d = e.getKey();
                DayAgg agg = e.getValue();
                if (agg.getIntervals().isEmpty()) continue;

                doc.add(new Paragraph(d.toString(), fSub));

                PdfPTable td = new PdfPTable(4);
                td.setWidthPercentage(100);
                td.setWidths(new float[]{30f, 30f, 20f, 20f});
                addHeaderCell(td, "Début");
                addHeaderCell(td, "Fin");
                addHeaderCell(td, "Durée (min)");
                addHeaderCell(td, "Commentaire");

                for (PresenceInterval pi : agg.getIntervals()) {
                    String st = (pi.getStartTime()!=null) ? pi.getStartTime().toString().replace('T',' ') : "-";
                    String et = (pi.getEndTime()!=null) ? pi.getEndTime().toString().replace('T',' ') : "—";
                    Integer m = pi.getMinutes();
                    if (m == null && pi.getStartTime()!=null && pi.getEndTime()!=null) {
                        long mm = java.time.Duration.between(pi.getStartTime(), pi.getEndTime()).toMinutes();
                        m = (int)Math.max(0, mm);
                    }
                    addCell(td, st, fNorm);
                    addCell(td, et, fNorm);
                    addCell(td, (pi.getEndTime()==null) ? "—" : String.valueOf(m!=null?m:0), fNorm);
                    addCell(td, safe(pi.getCommentaire()), fNorm);
                }

                doc.add(td);
                doc.add(new Paragraph(" ", fNorm));
            }

            doc.close();
            os.flush();
        } catch (DocumentException de) {
            resp.sendError(500, "PDF error: " + de.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, "PDF error: " +
                    (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage()));
        }
    }

    private static void addHeaderCell(PdfPTable t, String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private static void addCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        t.addCell(c);
    }

    private static String safe(String s) { return (s == null) ? "" : s; }
}
