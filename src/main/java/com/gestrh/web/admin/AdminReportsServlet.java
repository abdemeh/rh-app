package com.gestrh.web.admin;

import com.gestrh.entity.Conge;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/admin/reports/*")
public class AdminReportsServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo(); // null or like "/users.csv"
        if (path == null || "/".equals(path)) {
            // Landing page
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp);
            return;
        }

        switch (path) {
            case "/users.csv"  -> exportUsersCsv(resp);
            case "/leaves.csv" -> exportLeavesCsv(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void exportUsersCsv(HttpServletResponse resp) throws IOException {
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u LEFT JOIN FETCH u.departement d LEFT JOIN FETCH u.poste p " +
                        "ORDER BY u.nom, u.prenom", Utilisateur.class).getResultList();

        prepareCsv(resp, "users.csv");
        try (PrintWriter out = resp.getWriter()) {
            writeBom(out);
            out.println("ID;Prenom;Nom;Email;Statut;Departement;Poste;Contrat;DateEmbauche;DateSortie;SalaireBase");
            for (Utilisateur u : users) {
                out.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                        n(u.getId()),
                        s(u.getPrenom()), s(u.getNom()), s(u.getEmail()), s(u.getStatut()),
                        u.getDepartement()!=null ? s(u.getDepartement().getNom()) : "",
                        u.getPoste()!=null ? s(u.getPoste().getIntitule()) : "",
                        s(u.getContratType()),
                        d(u.getDateEmbauche()),
                        d(u.getDateSortie()),
                        m(u.getSalaireBase()));
            }
        }
    }

    private void exportLeavesCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String startStr = req.getParameter("start");
        String endStr   = req.getParameter("end");
        String statut   = req.getParameter("statut");

        LocalDate start = (startStr!=null && !startStr.isBlank()) ? LocalDate.parse(startStr) : LocalDate.of(2000,1,1);
        LocalDate end   = (endStr!=null && !endStr.isBlank()) ? LocalDate.parse(endStr)   : LocalDate.of(2100,1,1);

        String jpql = "SELECT c FROM Conge c JOIN FETCH c.utilisateur u JOIN FETCH c.type t " +
                "WHERE c.dateDebut <= :end AND c.dateFin >= :start";
        if (statut != null && !statut.isBlank()) jpql += " AND c.statut = :statut";
        jpql += " ORDER BY c.createdAt DESC";

        var q = em.createQuery(jpql, Conge.class)
                .setParameter("start", start)
                .setParameter("end", end);
        if (statut != null && !statut.isBlank()) q.setParameter("statut", statut);
        List<Conge> leaves = q.getResultList();

        prepareCsv(resp, "leaves.csv");
        try (PrintWriter out = resp.getWriter()) {
            writeBom(out);
            out.println("ID;Employe;Email;Type;Du;Au;NbJours;Statut;Motif;CreeLe");
            for (Conge c : leaves) {
                out.printf("%s;%s %s;%s;%s;%s;%s;%s;%s;%s%n",
                        n(c.getId()),
                        s(c.getUtilisateur().getPrenom()), s(c.getUtilisateur().getNom()),
                        s(c.getUtilisateur().getEmail()),
                        s(c.getType().getLibelle()),
                        d(c.getDateDebut()), d(c.getDateFin()),
                        m(c.getNbJours()),
                        s(c.getStatut()),
                        s(c.getMotif()),
                        dt(c.getCreatedAt()));
            }
        }
    }

    // CSV helpers
    private void prepareCsv(HttpServletResponse resp, String filename) {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }
    private void writeBom(PrintWriter out){ out.write("\uFEFF"); }
    private String s(String v){ return v==null ? "" : v.replace(';', ','); }
    private String d(java.time.LocalDate v){ return v==null ? "" : v.toString(); }
    private String dt(java.time.LocalDateTime v){ return v==null ? "" : v.toString(); }
    private String m(BigDecimal v){ return v==null ? "" : v.toPlainString(); }
    private String n(Number v){ return v==null ? "" : v.toString(); }
}
