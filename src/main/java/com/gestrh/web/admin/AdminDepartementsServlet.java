package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/admin/departements")
public class AdminDepartementsServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // List non-deleted departments and fetch responsable (optional)
        List<Departement> departements = em.createQuery(
                "SELECT d FROM Departement d " +
                        "LEFT JOIN FETCH d.responsable r " +
                        "WHERE d.deletedAt IS NULL " +
                        "ORDER BY d.id DESC",
                Departement.class
        ).getResultList();

        // All users to choose as responsable
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom",
                Utilisateur.class
        ).getResultList();

        req.setAttribute("departements", departements);
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin/departements.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");                // create | update | delete
        Integer id = parseIntOrNull(req.getParameter("id"));
        String nom = req.getParameter("nom_departement");
        Integer responsableId = parseIntOrNull(req.getParameter("responsable_id"));

        try {
            utx.begin();

            String redirectParam = "success=saved";

            if ("delete".equalsIgnoreCase(action) && id != null) {
                Departement d = em.find(Departement.class, id);
                if (d != null && d.getDeletedAt() == null) {
                    d.setDeletedAt(LocalDateTime.now());
                    em.merge(d);
                    redirectParam = "success=deleted";
                }

            } else if ("update".equalsIgnoreCase(action) && id != null) {
                Departement d = em.find(Departement.class, id);
                if (d == null) { utx.rollback(); resp.sendError(404, "Département introuvable"); return; }
                if (nom != null && !nom.isBlank()) d.setNom(nom);
                d.setResponsable(responsableId != null ? em.find(Utilisateur.class, responsableId) : null);
                em.merge(d);
                redirectParam = "success=updated";

            } else { // create
                Departement d = new Departement();
                d.setNom(nom);
                d.setResponsable(responsableId != null ? em.find(Utilisateur.class, responsableId) : null);
                d.setDeletedAt(null);
                em.persist(d);
                redirectParam = "success=created";
            }

            utx.commit();

            resp.sendRedirect(req.getContextPath() + "/admin/departements?" + redirectParam);
            return;
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de l’écriture du département");
            return;
        }



    }
}
