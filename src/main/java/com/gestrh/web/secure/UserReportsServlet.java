package com.gestrh.web.secure;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
import com.gestrh.entity.Departement;
import com.gestrh.entity.Poste;
import com.gestrh.entity.Utilisateur;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// iText (PDF)
import com.itextpdf.text.BaseColor;
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

@WebServlet("/secure/reports")
public class UserReportsServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Guard: user must be logged in
        Integer me = getUserId(req.getSession(false));
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // Provide default view data (current user entity)
        Utilisateur u = em.find(Utilisateur.class, me);
        req.setAttribute("me", u);

        String action = (req.getParameter("action") == null) ? "" : req.getParameter("action").toLowerCase(Locale.ROOT);

        switch (action) {
            case "pdf_my_conges_period":
                exportMyUserPeriodPdf(me, req, resp);
                return;
            case "pdf_my_salary_month":
                exportMySalariesMonthPdf(me, req, resp);
                return;
            default:
                req.getRequestDispatcher("/secure/reports.jsp").forward(req, resp);
        }
    }

    // =========================================================
    // A) PDF — Mes Congés & Rémunération sur période
    // =========================================================
    private void exportMyUserPeriodPdf(Integer userId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalDate start = parseDate(req.getParameter("start_date"));
        LocalDate end   = parseDate(req.getParameter("end_date"));
        if (start == null || end == null || end.isBefore(start)) {
            resp.sendError(400, "Dates invalides.");
            return;
        }

        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) { resp.sendError(404, "Utilisateur introuvable."); return; }

        List<Conge> conges = em.createQuery(
                        "SELECT c FROM Conge c " +
                                "LEFT JOIN FETCH c.type t " +
                                "LEFT JOIN FETCH c.utilisateur cu " +
                                "WHERE cu.id = :uid " +
                                "AND c.statut IN ('APPROUVE', 'APPROUVÉ', 'APPROUVÉ') " +
                                "AND c.dateDebut <= :end AND c.dateFin >= :start " +
                                "ORDER BY c.dateDebut ASC", Conge.class)
                .setParameter("uid", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        long joursPeriode = ChronoUnit.DAYS.between(start, end) + 1;
        long joursConge = 0;
        Map<String, Long> joursParType = new LinkedHashMap<>();

        for (Conge c : conges) {
            LocalDate d0 = c.getDateDebut(), d1 = c.getDateFin();
            if (d0 == null || d1 == null) continue;
            LocalDate effStart = d0.isBefore(start) ? start : d0;
            LocalDate effEnd   = d1.isAfter(end)    ? end   : d1;
            if (!effEnd.isBefore(effStart)) {
                long j = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
                joursConge += j;

                CongeType t = c.getType();
                String lib = (t != null && nz(t.getLibelle())) ? t.getLibelle()
                        : (t != null && nz(t.getCode())) ? t.getCode()
                        : "Type inconnu";
                joursParType.merge(lib, j, Long::sum);
            }
        }

        long heuresEstimees = joursConge * 8;
        BigDecimal salaireBase = nz(u.getSalaireBase());
        BigDecimal tauxJournalier = (salaireBase.compareTo(BigDecimal.ZERO) > 0)
                ? salaireBase.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal remunerationEstimee = tauxJournalier
                .multiply(BigDecimal.valueOf(joursConge))
                .setScale(2, RoundingMode.HALF_UP);

        resp.setContentType("application/pdf");
        String filename = String.format("mes_conges_%s_%s.pdf", start, end);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, os);
            doc.open();

            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("Mes congés & rémunération (état utilisateur)",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(15f);
            doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{35f,65f});
            addKV(info, "Période", start + "  →  " + end);
            addKV(info, "Nom", fullName(u));
            addKV(info, "Email", safe(u.getEmail()));
            addKV(info, "Poste", (u.getPoste()!=null ? safe(u.getPoste().getIntitule()) : "-"));
            addKV(info, "Département", (u.getDepartement()!=null ? safe(deptName(u.getDepartement())) : "-"));
            addKV(info, "Salaire de base (mensuel)", salaireBase.toPlainString() + " MAD");
            doc.add(info);

            PdfPTable synth = new PdfPTable(5);
            synth.setWidthPercentage(100); synth.setSpacingAfter(10f);
            synth.setWidths(new float[]{22f, 22f, 18f, 18f, 20f});
            header(synth, "Jours dans la période");
            header(synth, "Total jours de congé");
            header(synth, "Heures estimées");
            header(synth, "Taux journalier");
            header(synth, "Rémunération (jours de congé)");
            synth.addCell(cell(String.valueOf(joursPeriode)));
            synth.addCell(cell(String.valueOf(joursConge)));
            synth.addCell(cell(heuresEstimees + " h"));
            synth.addCell(cell(tauxJournalier.toPlainString() + " MAD"));
            synth.addCell(cell(remunerationEstimee.toPlainString() + " MAD"));
            doc.add(synth);

            Paragraph dt = new Paragraph("Détail des congés par type", new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD));
            dt.setSpacingBefore(8f); dt.setSpacingAfter(6f); doc.add(dt);

            PdfPTable details = new PdfPTable(2);
            details.setWidthPercentage(100); details.setWidths(new float[]{70f,30f});
            header(details, "Type de congé"); header(details, "Jours");
            if (joursParType.isEmpty()) {
                PdfPCell c1 = new PdfPCell(new Phrase("Aucun congé approuvé sur la période.")); c1.setColspan(2);
                details.addCell(c1);
            } else {
                for (Map.Entry<String, Long> e : joursParType.entrySet()) {
                    details.addCell(cell(e.getKey()));
                    details.addCell(cell(String.valueOf(e.getValue())));
                }
            }
            doc.add(details);

            Paragraph foot = new Paragraph("Document généré par GestRH", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            foot.setSpacingBefore(20f); doc.add(foot);
            doc.close();
        } catch (DocumentException de) {
            resp.sendError(500, "Erreur PDF: " + de.getMessage());
        }
    }

    // =========================================================
    // B) PDF — Mon Salaire & Rémunération des congés pour un mois donné
    // =========================================================
    private void exportMySalariesMonthPdf(Integer userId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String month = nv(req.getParameter("month"));
        YearMonth ym;
        try { ym = YearMonth.parse(month); } catch (Exception e) {
            resp.sendError(400, "Mois invalide (format attendu: yyyy-MM).");
            return;
        }

        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) { resp.sendError(404, "Utilisateur introuvable."); return; }

        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        // Salaire proratisé sur le mois compte tenu de l’embauche/sortie
        LocalDate emb = u.getDateEmbauche();
        LocalDate sort = u.getDateSortie();
        LocalDate effStart = (emb != null && emb.isAfter(mStart)) ? emb : mStart;
        LocalDate effEnd   = (sort != null && sort.isBefore(mEnd)) ? sort : mEnd;

        long daysPaid = 0;
        BigDecimal salaireDuMois = BigDecimal.ZERO;
        if (!effEnd.isBefore(effStart)) {
            daysPaid = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
            BigDecimal base = nz(u.getSalaireBase());
            salaireDuMois = base.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(daysPaid))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Congés approuvés intersectant ce mois
        List<Conge> conges = em.createQuery(
                        "SELECT c FROM Conge c " +
                                "LEFT JOIN FETCH c.type t " +
                                "LEFT JOIN FETCH c.utilisateur cu " +
                                "WHERE cu.id = :uid " +
                                "AND c.statut IN ('APPROUVE', 'APPROUVÉ', 'APPROUVÉ') " +
                                "AND c.dateDebut <= :end AND c.dateFin >= :start " +
                                "ORDER BY c.dateDebut ASC", Conge.class)
                .setParameter("uid", userId)
                .setParameter("start", mStart)
                .setParameter("end", mEnd)
                .getResultList();

        long joursConge = 0;
        Map<String, Long> joursParType = new LinkedHashMap<>();
        for (Conge c : conges) {
            LocalDate d0 = c.getDateDebut(), d1 = c.getDateFin();
            if (d0 == null || d1 == null) continue;
            LocalDate s = d0.isBefore(mStart) ? mStart : d0;
            LocalDate e = d1.isAfter(mEnd)    ? mEnd   : d1;
            if (!e.isBefore(s)) {
                long j = ChronoUnit.DAYS.between(s, e) + 1;
                joursConge += j;

                CongeType t = c.getType();
                String lib = (t != null && nz(t.getLibelle())) ? t.getLibelle()
                        : (t != null && nz(t.getCode())) ? t.getCode()
                        : "Type inconnu";
                joursParType.merge(lib, j, Long::sum);
            }
        }

        BigDecimal tauxJournalier = nz(u.getSalaireBase()).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        BigDecimal remunerationConges = tauxJournalier.multiply(BigDecimal.valueOf(joursConge)).setScale(2, RoundingMode.HALF_UP);

        resp.setContentType("application/pdf");
        String filename = String.format("mon_salaire_%s.pdf", ym);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, os);
            doc.open();

            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("Mon salaire & rémunération des congés — " + ym,
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(15f);
            doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{35f,65f});
            addKV(info, "Nom", fullName(u));
            addKV(info, "Email", safe(u.getEmail()));
            addKV(info, "Poste", (u.getPoste()!=null ? safe(u.getPoste().getIntitule()) : "-"));
            addKV(info, "Département", (u.getDepartement()!=null ? safe(deptName(u.getDepartement())) : "-"));
            addKV(info, "Salaire de base (mensuel)", nz(u.getSalaireBase()).toPlainString() + " MAD");
            addKV(info, "Période du mois", mStart + " → " + mEnd);
            doc.add(info);

            PdfPTable synth = new PdfPTable(4);
            synth.setWidthPercentage(100); synth.setSpacingAfter(10f);
            synth.setWidths(new float[]{25f, 25f, 25f, 25f});
            header(synth, "Jours payés (mois)");
            header(synth, "Salaire du mois (prorata)");
            header(synth, "Jours de congé approuvés");
            header(synth, "Rémunération des congés");
            synth.addCell(cell(String.valueOf(daysPaid)));
            synth.addCell(cell(salaireDuMois.toPlainString() + " MAD"));
            synth.addCell(cell(String.valueOf(joursConge)));
            synth.addCell(cell(remunerationConges.toPlainString() + " MAD"));
            doc.add(synth);

            Paragraph dt = new Paragraph("Détail des congés approuvés ce mois", new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD));
            dt.setSpacingBefore(8f); dt.setSpacingAfter(6f); doc.add(dt);

            PdfPTable details = new PdfPTable(2);
            details.setWidthPercentage(100); details.setWidths(new float[]{70f,30f});
            header(details, "Type de congé"); header(details, "Jours dans le mois");
            if (joursParType.isEmpty()) {
                PdfPCell c1 = new PdfPCell(new Phrase("Aucun congé approuvé dans ce mois.")); c1.setColspan(2);
                details.addCell(c1);
            } else {
                for (Map.Entry<String, Long> e : joursParType.entrySet()) {
                    details.addCell(cell(e.getKey()));
                    details.addCell(cell(String.valueOf(e.getValue())));
                }
            }
            doc.add(details);

            Paragraph foot = new Paragraph("Document généré par GestRH", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            foot.setSpacingBefore(18f); doc.add(foot);

            doc.close();
        } catch (DocumentException de) {
            resp.sendError(500, "Erreur PDF: " + de.getMessage());
        }
    }

    // ====================== Helpers ======================
    private Integer getUserId(HttpSession session) {
        if (session == null) return null;
        Object o = session.getAttribute("userId");
        if (o == null) return null;
        try { return (o instanceof Integer) ? (Integer)o : Integer.valueOf(o.toString()); }
        catch (Exception e) { return null; }
    }
    private static String nv(String s) { return s == null ? "" : s; }
    private static boolean nz(String s) { return s != null && !s.isBlank(); }
    private static BigDecimal nz(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }
    private static String safe(String s) { return (s == null || s.isBlank()) ? "-" : s; }
    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }
    private static String fullName(Utilisateur u) {
        String p = u.getPrenom()==null?"":u.getPrenom().trim();
        String n = u.getNom()==null?"":u.getNom().trim();
        String full = (p + " " + n).trim();
        return full.isEmpty()? "-" : full;
    }
    private static String deptName(Departement d) {
        if (d == null) return "-";
        String n = d.getNom();
        return (n==null || n.isBlank()) ? "-" : n;
    }
    private static void addKV(PdfPTable t, String k, String v) {
        PdfPCell kCell = new PdfPCell(new Phrase(k));
        kCell.setBackgroundColor(new BaseColor(245,245,245));
        t.addCell(kCell);
        t.addCell(new PdfPCell(new Phrase(v)));
    }
    private static void header(PdfPTable t, String label) {
        PdfPCell h = new PdfPCell(new Phrase(label, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));
        h.setBackgroundColor(new BaseColor(230,230,230));
        t.addCell(h);
    }
    private static PdfPCell cell(String s) {
        return new PdfPCell(new Phrase(s == null ? "-" : s));
    }
}
