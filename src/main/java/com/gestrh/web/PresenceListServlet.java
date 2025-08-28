package com.gestrh.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/secure/presences")
public class PresenceListServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        LocalDate today = LocalDate.now();

        // Portable JPQL: use correlated subselects to fetch presence fields for the date.
        List<Object[]> rows = em.createQuery(
                        "SELECT u.prenom, u.nom, " +
                                " (SELECT p.jour FROM Presence p WHERE p.utilisateur = u AND p.jour = :d), " +
                                " (SELECT p.heureEntree FROM Presence p WHERE p.utilisateur = u AND p.jour = :d), " +
                                " (SELECT p.heureSortie FROM Presence p WHERE p.utilisateur = u AND p.jour = :d), " +
                                " (SELECT p.statut FROM Presence p WHERE p.utilisateur = u AND p.jour = :d) " +
                                "FROM Utilisateur u " +
                                "ORDER BY u.nom, u.prenom", Object[].class)
                .setParameter("d", today)
                .getResultList();

        req.setAttribute("rows", rows);
        req.getRequestDispatcher("/secure/presences.jsp").forward(req, resp);
    }
}
