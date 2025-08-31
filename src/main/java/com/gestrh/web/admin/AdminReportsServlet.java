package com.gestrh.web.admin;

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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// iText: imports ciblés (éviter clash com.itextpdf.text.List)
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

@WebServlet("/admin/reports")
public class AdminReportsServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Toujours alimenter les listes pour les formulaires
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class
        ).getResultList();

        List<Departement> depts = em.createQuery(
                "SELECT d FROM Departement d ORDER BY d.id ASC", Departement.class
        ).getResultList();

        List<Poste> postes = em.createQuery(
                "SELECT p FROM Poste p ORDER BY p.id ASC", Poste.class
        ).getResultList();

        req.setAttribute("users", users);
        req.setAttribute("departements", depts);
        req.setAttribute("postes", postes);

        String action = req.getParameter("action");
        action = (action == null) ? "" : action.toLowerCase(Locale.ROOT);

        // tolérant aux variantes pour éviter les mismatchs
        switch (action) {
            case "pdf":
            case "pdf_user_period":
                exportUserPeriodPdf(req, resp);
                return;

            case "pdf_users_by_dept":
            case "users_by_dept_pdf":
                exportDeptUsersPdf(req, resp);
                return;

            case "pdf_users_by_poste":
            case "users_by_poste_pdf":
                exportPosteUsersPdf(req, resp);
                return;

            case "pdf_salaries_month":
            case "salaries_month_pdf":
                exportSalariesMonthPdf(req, resp);
                return;

            // (facultatif) handlers HTML si tu veux des listes à l'écran
            case "users_by_dept":
                handleUsersByDept(req, resp); return;
            case "users_by_poste":
                handleUsersByPoste(req, resp); return;
            case "salaries_month":
                handleSalariesMonth(req, resp); return;
            case "salaries_range":
                handleSalariesRange(req, resp); return;
            case "leaves_pay_range":
                handleLeavesPayRange(req, resp); return;

            default:
                req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
        }
    }

    // ============== PDF: Congés & rémunération par utilisateur/période ==============
    private void exportUserPeriodPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer userId = parseInt(req.getParameter("user_id"));
        LocalDate start = parseDate(req.getParameter("start_date"));
        LocalDate end   = parseDate(req.getParameter("end_date"));
        if (userId == null || start == null || end == null || end.isBefore(start)) {
            resp.sendError(400, "Paramètres invalides (utilisateur ou dates).");
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

        // PDF
        resp.setContentType("application/pdf");
        String filename = String.format("etat_%s_%s_%s.pdf", userId, start, end);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, os);
            doc.open();

            // Logo / app name top-right
            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("État récapitulatif des congés et rémunération",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(15f);
            doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{35f,65f});
            addKV(info, "Utilisateur", fullName(u) + "  (" + safe(u.getEmail()) + ")");
            addKV(info, "Période", start + "  →  " + end);
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

    // ============== PDF: Employés par Département ==============
    private void exportDeptUsersPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer deptId = parseInt(req.getParameter("dept_id"));
        if (deptId == null) { resp.sendError(400, "Département manquant."); return; }

        Departement d = em.find(Departement.class, deptId);
        if (d == null) { resp.sendError(404, "Département introuvable."); return; }

        List<Utilisateur> list = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement dep " +
                                "WHERE dep.id = :did ORDER BY u.nom, u.prenom", Utilisateur.class)
                .setParameter("did", deptId)
                .getResultList();

        resp.setContentType("application/pdf");
        String filename = "employes_departement_" + deptId + ".pdf";
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36); // paysage
            PdfWriter.getInstance(doc, os);
            doc.open();

            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("Liste des employés par département",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(12f);
            doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{25f,75f});
            addKV(info, "Département", safe(deptName(d)));
            addKV(info, "Nombre d'employés", String.valueOf(list.size()));
            doc.add(info);

            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100); t.setWidths(new float[]{6f, 22f, 28f, 18f, 14f, 12f});
            header(t, "#");
            header(t, "Nom complet");
            header(t, "Email");
            header(t, "Poste");
            header(t, "Date d'embauche");
            header(t, "Salaire (MAD)");

            int i = 1;
            for (Utilisateur u : list) {
                t.addCell(cell(String.valueOf(i++)));
                t.addCell(cell(fullName(u)));
                t.addCell(cell(safe(u.getEmail())));
                t.addCell(cell(u.getPoste() != null ? safe(u.getPoste().getIntitule()) : "-"));
                t.addCell(cell(u.getDateEmbauche() != null ? u.getDateEmbauche().toString() : "-"));
                t.addCell(cell(nz(u.getSalaireBase()).toPlainString()));
            }
            if (list.isEmpty()) {
                PdfPCell c1 = new PdfPCell(new Phrase("Aucun employé dans ce département."));
                c1.setColspan(6); t.addCell(c1);
            }
            doc.add(t);

            Paragraph foot = new Paragraph("Document généré par GestRH", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            foot.setSpacingBefore(18f); doc.add(foot);

            doc.close();
        } catch (DocumentException de) {
            resp.sendError(500, "Erreur PDF: " + de.getMessage());
        }
    }

    // ============== PDF: Employés par Poste ==============
    private void exportPosteUsersPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer posteId = parseInt(req.getParameter("poste_id"));
        if (posteId == null) { resp.sendError(400, "Poste manquant."); return; }

        Poste p = em.find(Poste.class, posteId);
        if (p == null) { resp.sendError(404, "Poste introuvable."); return; }

        List<Utilisateur> list = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste po LEFT JOIN FETCH u.departement d " +
                                "WHERE po.id = :pid ORDER BY u.nom, u.prenom", Utilisateur.class)
                .setParameter("pid", posteId)
                .getResultList();

        resp.setContentType("application/pdf");
        String filename = "employes_poste_" + posteId + ".pdf";
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, os);
            doc.open();

            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("Liste des employés par poste",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(12f);
            doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{25f,75f});
            addKV(info, "Poste", safe(p.getIntitule()));
            addKV(info, "Nombre d'employés", String.valueOf(list.size()));
            doc.add(info);

            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100); t.setWidths(new float[]{6f, 22f, 28f, 20f, 12f, 12f});
            header(t, "#");
            header(t, "Nom complet");
            header(t, "Email");
            header(t, "Département");
            header(t, "Date d'embauche");
            header(t, "Salaire (MAD)");

            int i = 1;
            for (Utilisateur u : list) {
                t.addCell(cell(String.valueOf(i++)));
                t.addCell(cell(fullName(u)));
                t.addCell(cell(safe(u.getEmail())));
                t.addCell(cell(u.getDepartement() != null ? safe(deptName(u.getDepartement())) : "-"));
                t.addCell(cell(u.getDateEmbauche() != null ? u.getDateEmbauche().toString() : "-"));
                t.addCell(cell(nz(u.getSalaireBase()).toPlainString()));
            }
            if (list.isEmpty()) {
                PdfPCell c1 = new PdfPCell(new Phrase("Aucun employé pour ce poste."));
                c1.setColspan(6); t.addCell(c1);
            }
            doc.add(t);

            Paragraph foot = new Paragraph("Document généré par GestRH", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            foot.setSpacingBefore(18f); doc.add(foot);

            doc.close();
        } catch (DocumentException de) {
            resp.sendError(500, "Erreur PDF: " + de.getMessage());
        }
    }

    // ============== PDF: Salaires pour un mois donné (yyyy-MM) ==============
    private void exportSalariesMonthPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String month = nv(req.getParameter("month"));
        YearMonth ym;
        try { ym = YearMonth.parse(month); } catch (Exception e) {
            resp.sendError(400, "Mois invalide (format attendu: yyyy-MM).");
            return;
        }

        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        List<Utilisateur> all = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement d " +
                                "ORDER BY u.nom, u.prenom", Utilisateur.class)
                .getResultList();

        class Row { Utilisateur u; long days; BigDecimal amount; }
        List<Row> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Utilisateur u : all) {
            LocalDate emb = u.getDateEmbauche();
            LocalDate sort = u.getDateSortie();

            LocalDate effStart = (emb != null && emb.isAfter(mStart)) ? emb : mStart;
            LocalDate effEnd   = (sort != null && sort.isBefore(mEnd)) ? sort : mEnd;

            if (!effEnd.isBefore(effStart)) {
                long days = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
                BigDecimal base = nz(u.getSalaireBase());
                BigDecimal amount = base.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(days))
                        .setScale(2, RoundingMode.HALF_UP);
                total = total.add(amount);

                Row r = new Row();
                r.u = u; r.days = days; r.amount = amount;
                rows.add(r);
            }
        }

        resp.setContentType("application/pdf");
        String filename = "salaires_" + ym + ".pdf";
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (OutputStream os = resp.getOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, os);
            doc.open();

            Paragraph logo = new Paragraph("GestRH", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);

            Paragraph titre = new Paragraph("Rapport des salaires — " + ym,
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            titre.setSpacingBefore(10f); titre.setSpacingAfter(12f); doc.add(titre);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100); info.setSpacingAfter(10f); info.setWidths(new float[]{30f,70f});
            addKV(info, "Mois", ym.toString());
            addKV(info, "Total à payer (MAD)", total.toPlainString());
            doc.add(info);

            PdfPTable t = new PdfPTable(7);
            t.setWidthPercentage(100); t.setWidths(new float[]{6f, 20f, 26f, 18f, 16f, 10f, 14f});
            header(t, "#");
            header(t, "Employé");
            header(t, "Email");
            header(t, "Département");
            header(t, "Poste");
            header(t, "Jours");
            header(t, "Montant (MAD)");

            int i = 1;
            for (Row r : rows) {
                Utilisateur u = r.u;
                t.addCell(cell(String.valueOf(i++)));
                t.addCell(cell(fullName(u)));
                t.addCell(cell(safe(u.getEmail())));
                t.addCell(cell(u.getDepartement() != null ? safe(deptName(u.getDepartement())) : "-"));
                t.addCell(cell(u.getPoste() != null ? safe(u.getPoste().getIntitule()) : "-"));
                t.addCell(cell(String.valueOf(r.days)));
                t.addCell(cell(r.amount.toPlainString()));
            }
            if (rows.isEmpty()) {
                PdfPCell c1 = new PdfPCell(new Phrase("Aucun salaire à calculer pour ce mois."));
                c1.setColspan(7); t.addCell(c1);
            }
            doc.add(t);

            Paragraph foot = new Paragraph("Document généré par GestRH", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC));
            foot.setSpacingBefore(18f); doc.add(foot);

            doc.close();
        } catch (DocumentException de) {
            resp.sendError(500, "Erreur PDF: " + de.getMessage());
        }
    }

    // ====================== (HTML handlers optionnels) ======================
    private void handleUsersByDept(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer deptId = parseInt(req.getParameter("dept_id"));
        if (deptId == null) {
            req.setAttribute("error", "Veuillez choisir un département.");
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }
        List<Utilisateur> list = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement d " +
                                "WHERE d.id = :did ORDER BY u.nom, u.prenom", Utilisateur.class)
                .setParameter("did", deptId)
                .getResultList();

        req.setAttribute("r_usersByDept", list);
        req.setAttribute("r_deptId", deptId);
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    private void handleUsersByPoste(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer posteId = parseInt(req.getParameter("poste_id"));
        if (posteId == null) {
            req.setAttribute("error", "Veuillez choisir un poste.");
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }
        List<Utilisateur> list = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement d " +
                                "WHERE p.id = :pid ORDER BY u.nom, u.prenom", Utilisateur.class)
                .setParameter("pid", posteId)
                .getResultList();

        req.setAttribute("r_usersByPoste", list);
        req.setAttribute("r_posteId", posteId);
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    private void handleSalariesMonth(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String month = nv(req.getParameter("month"));
        YearMonth ym;
        try { ym = YearMonth.parse(month); } catch (Exception e) {
            req.setAttribute("error", "Mois invalide (format attendu: yyyy-MM).");
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }

        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        List<Utilisateur> all = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement d " +
                                "ORDER BY u.nom, u.prenom", Utilisateur.class)
                .getResultList();

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Utilisateur u : all) {
            LocalDate emb = u.getDateEmbauche();
            LocalDate sort = u.getDateSortie();

            LocalDate effStart = (emb != null && emb.isAfter(mStart)) ? emb : mStart;
            LocalDate effEnd   = (sort != null && sort.isBefore(mEnd)) ? sort : mEnd;

            if (!effEnd.isBefore(effStart)) {
                long days = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
                BigDecimal base = nz(u.getSalaireBase());
                BigDecimal amount = base.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(days))
                        .setScale(2, RoundingMode.HALF_UP);
                total = total.add(amount);

                Map<String,Object> r = new LinkedHashMap<>();
                r.put("user", u);
                r.put("days", days);
                r.put("amount", amount);
                rows.add(r);
            }
        }

        req.setAttribute("r_salMonth_rows", rows);
        req.setAttribute("r_salMonth_total", total);
        req.setAttribute("r_salMonth_label", ym.toString());
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    private void handleSalariesRange(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate start = parseDate(req.getParameter("start_date"));
        LocalDate end   = parseDate(req.getParameter("end_date"));
        if (start == null || end == null || end.isBefore(start)) {
            req.setAttribute("error", "Dates invalides pour les salaires (période).");
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }

        List<Utilisateur> all = em.createQuery(
                        "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.poste p LEFT JOIN FETCH u.departement d " +
                                "ORDER BY u.nom, u.prenom", Utilisateur.class)
                .getResultList();

        List<Map<String,Object>> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        long daysPeriod = ChronoUnit.DAYS.between(start, end) + 1;

        for (Utilisateur u : all) {
            LocalDate emb = u.getDateEmbauche();
            LocalDate sort = u.getDateSortie();

            LocalDate effStart = (emb != null && emb.isAfter(start)) ? emb : start;
            LocalDate effEnd   = (sort != null && sort.isBefore(end)) ? sort : end;

            if (!effEnd.isBefore(effStart)) {
                long days = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
                BigDecimal base = nz(u.getSalaireBase());
                BigDecimal amount = base.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(days))
                        .setScale(2, RoundingMode.HALF_UP);
                total = total.add(amount);

                Map<String,Object> r = new LinkedHashMap<>();
                r.put("user", u);
                r.put("days", days);
                r.put("amount", amount);
                rows.add(r);
            }
        }

        req.setAttribute("r_salRange_rows", rows);
        req.setAttribute("r_salRange_total", total);
        req.setAttribute("r_salRange_label", start + " → " + end + " (" + daysPeriod + " jours)");
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    private void handleLeavesPayRange(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate start = parseDate(req.getParameter("start_date"));
        LocalDate end   = parseDate(req.getParameter("end_date"));
        if (start == null || end == null || end.isBefore(start)) {
            req.setAttribute("error", "Dates invalides pour les rémunérations de congés.");
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }

        List<Conge> conges = em.createQuery(
                        "SELECT c FROM Conge c " +
                                "LEFT JOIN FETCH c.utilisateur u " +
                                "LEFT JOIN FETCH c.type t " +
                                "WHERE c.statut IN ('APPROUVE', 'APPROUVÉ', 'APPROUVÉ') " +
                                "AND c.dateDebut <= :end AND c.dateFin >= :start " +
                                "ORDER BY u.nom, u.prenom, c.dateDebut ASC", Conge.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<Integer, LeavesAgg> perUser = new LinkedHashMap<>();

        for (Conge c : conges) {
            Utilisateur u = c.getUtilisateur();
            if (u == null) continue;

            LocalDate d0 = c.getDateDebut(), d1 = c.getDateFin();
            if (d0 == null || d1 == null) continue;

            LocalDate effStart = d0.isBefore(start) ? start : d0;
            LocalDate effEnd   = d1.isAfter(end)    ? end   : d1;
            if (effEnd.isBefore(effStart)) continue;

            long days = ChronoUnit.DAYS.between(effStart, effEnd) + 1;
            BigDecimal base = nz(u.getSalaireBase());
            BigDecimal rate = base.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            BigDecimal amount = rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);

            LeavesAgg agg = perUser.computeIfAbsent(u.getId(), k -> new LeavesAgg(u));
            agg.days += days;
            agg.amount = agg.amount.add(amount);
            agg.conges.add(c);
        }

        BigDecimal grandTotal = perUser.values().stream()
                .map(a -> a.amount).reduce(BigDecimal.ZERO, BigDecimal::add);

        req.setAttribute("r_leaves_users", perUser.values());
        req.setAttribute("r_leaves_total", grandTotal);
        req.setAttribute("r_leaves_label", start + " → " + end);
        req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
    }

    // ====================== Helpers ======================
    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }
    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }
    private static String nv(String s) { return s == null ? "" : s; }
    private static boolean nz(String s) { return s != null && !s.isBlank(); }
    private static BigDecimal nz(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }
    private static String safe(String s) { return (s == null || s.isBlank()) ? "-" : s; }
    private static String fullName(Utilisateur u) {
        String p = u.getPrenom()==null?"":u.getPrenom().trim();
        String n = u.getNom()==null?"":u.getNom().trim();
        String full = (p + " " + n).trim();
        return full.isEmpty()? "-" : full;
    }
    private static String deptName(Departement d) {
        if (d == null) return "-";
        String n = d.getNom(); // mappé à nom_departement
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

    public static class LeavesAgg {
        public final Utilisateur user;
        public long days = 0;
        public BigDecimal amount = BigDecimal.ZERO;
        public final List<Conge> conges = new ArrayList<>();
        public LeavesAgg(Utilisateur u) { this.user = u; }
    }
}
