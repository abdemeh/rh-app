package com.gestrh.web.admin;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet({"/admin/", "/admin"})
public class AdminDashboardServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String format = Optional.ofNullable(req.getParameter("format")).orElse("html");
        int months = parseIntOrDefault(req.getParameter("months"), 12);

        if ("json".equalsIgnoreCase(format)) {
            writeJson(resp, buildMetrics(months));
            return;
        }

        // Widgets (compteurs + masse salariale estimée du mois courant)
        long users = countLong("SELECT COUNT(*) FROM utilisateurs");
        long depts = countLong("SELECT COUNT(*) FROM departements");
        long pending = countLong("SELECT COUNT(*) FROM conges WHERE statut = 'en_attente'");
        long presencesToday = countLong("SELECT COUNT(*) FROM presences WHERE jour = CURRENT_DATE()");

        YearMonth nowYm = YearMonth.now();
        BigDecimal payrollThisMonth = sumPayrollForMonth(nowYm); // somme des salaires des actifs ce mois

        // Congés démarrant ce mois (ou demandes créées ce mois si tu préfères created_at)
        long congesThisMonth = countLong(
                "SELECT COUNT(*) FROM conges WHERE YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())"
        );

        req.setAttribute("usersCount", users);
        req.setAttribute("deptCount", depts);
        req.setAttribute("pendingCount", pending);
        req.setAttribute("presencesToday", presencesToday);
        req.setAttribute("payrollThisMonth", payrollThisMonth); // affichage formaté côté JSP
        req.setAttribute("congesThisMonth", congesThisMonth);
        req.setAttribute("defaultMonths", months);

        req.getRequestDispatcher("/admin/index.jsp").forward(req, resp);
    }

    private JsonObject buildMetrics(int months) {
        // N derniers mois
        List<YearMonth> span = new ArrayList<>();
        YearMonth cur = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) span.add(cur.minusMonths(i));
        DateTimeFormatter labFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder conges = Json.createArrayBuilder();
        JsonArrayBuilder presences = Json.createArrayBuilder();
        JsonArrayBuilder payroll = Json.createArrayBuilder();

        for (YearMonth ym : span) {
            labels.add(ym.format(labFmt));

            // 1) Congés: basés sur created_at (change en date_debut si tu préfères)
            conges.add(countByMonthOnDateTime("conges", "created_at", ym));

            // 2) Présences: colonne 'jour' (DATE)
            presences.add(countByMonthOnDate("presences", "jour", ym));

            // 3) Masse salariale estimée
            payroll.add(sumPayrollForMonth(ym));
        }

        // Doughnut: répartition statuts conges (approuve / rejete / en_attente)
        long approuve = countLong("SELECT COUNT(*) FROM conges WHERE statut='approuve'");
        long rejete = countLong("SELECT COUNT(*) FROM conges WHERE statut='rejete'");
        long enAtt = countLong("SELECT COUNT(*) FROM conges WHERE statut='en_attente'");

        // Bar: utilisateurs par département
        @SuppressWarnings("unchecked")
        List<Object[]> usersByDept = em.createNativeQuery(
                "SELECT d.nom_departement, COUNT(u.id) " +
                        "FROM departements d LEFT JOIN utilisateurs u ON u.departement_id = d.id " +
                        "GROUP BY d.nom_departement ORDER BY 2 DESC"
        ).getResultList();

        JsonArrayBuilder deptLabels = Json.createArrayBuilder();
        JsonArrayBuilder deptCounts = Json.createArrayBuilder();
        for (Object[] row : usersByDept) {
            deptLabels.add(Objects.toString(row[0], "—"));
            deptCounts.add(((Number) row[1]).longValue());
        }

        return Json.createObjectBuilder()
                .add("labels", labels)
                .add("series", Json.createObjectBuilder()
                        .add("conges", conges)
                        .add("presences", presences)
                        .add("payroll", payroll) // masse salariale estimée
                )
                .add("congesStatus", Json.createObjectBuilder()
                        .add("approuve", approuve)
                        .add("rejete", rejete)
                        .add("en_attente", enAtt)
                )
                .add("usersByDept", Json.createObjectBuilder()
                        .add("labels", deptLabels)
                        .add("counts", deptCounts)
                )
                .build();
    }

    /* --------------------- Helpers SQL --------------------- */

    private long countLong(String sql) {
        Object v = em.createNativeQuery(sql).getSingleResult();
        return ((Number) v).longValue();
    }

    // COUNT(*) pour une colonne DATE
    private long countByMonthOnDate(String table, String dateCol, YearMonth ym) {
        Object v = em.createNativeQuery(
                        "SELECT COUNT(*) FROM " + table + " WHERE YEAR(" + dateCol + ") = ?1 AND MONTH(" + dateCol + ") = ?2"
                )
                .setParameter(1, ym.getYear())
                .setParameter(2, ym.getMonthValue())
                .getSingleResult();
        return ((Number) v).longValue();
    }

    // COUNT(*) pour une colonne DATETIME (created_at)
    private long countByMonthOnDateTime(String table, String dtCol, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        Object v = em.createNativeQuery(
                        "SELECT COUNT(*) FROM " + table + " WHERE " + dtCol + " >= ?1 AND " + dtCol + " < ?2"
                )
                .setParameter(1, java.sql.Timestamp.valueOf(start.atStartOfDay()))
                .setParameter(2, java.sql.Timestamp.valueOf(end.plusDays(1).atStartOfDay()))
                .getSingleResult();
        return ((Number) v).longValue();
    }

    /**
     * Masse salariale estimée pour un mois: somme des salaire_base des utilisateurs
     * actifs dans le mois (embauchés au plus tard le dernier jour du mois et
     * pas sortis avant le 1er du mois).
     */
    private BigDecimal sumPayrollForMonth(YearMonth ym) {
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();

        Object v = em.createNativeQuery(
                        "SELECT COALESCE(SUM(salaire_base), 0) " +
                                "FROM utilisateurs " +
                                "WHERE salaire_base IS NOT NULL " +
                                "AND (date_embauche IS NULL OR date_embauche <= ?1) " +
                                "AND (date_sortie IS NULL OR date_sortie >= ?2)"
                )
                .setParameter(1, java.sql.Date.valueOf(lastDay))
                .setParameter(2, java.sql.Date.valueOf(firstDay))
                .getSingleResult();

        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        return BigDecimal.ZERO;
    }

    private void writeJson(HttpServletResponse resp, jakarta.json.JsonObject data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) { out.print(data.toString()); }
    }

    private int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.isEmpty()) ? def : Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }
}
