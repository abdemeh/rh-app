package com.gestrh.web;

import jakarta.persistence.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;

@WebServlet("/secure/conges1")
public class CongePageServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer uid = (Integer) req.getSession().getAttribute("userId");

        var types = em.createQuery(
                        "SELECT t FROM CongeType t WHERE t.actif = true ORDER BY t.libelle", com.gestrh.entity.CongeType.class)
                .getResultList();
        req.setAttribute("types", types);

        var q = em.createQuery(
                "SELECT c.id, t.libelle, c.dateDebut, c.dateFin, c.nbJours, c.statut, c.motif, c.justificatifPath " +
                        "FROM Conge c JOIN c.type t WHERE c.utilisateur.id = :u ORDER BY c.id DESC", Object[].class);
        q.setParameter("u", uid);
        req.setAttribute("conges", q.getResultList());

        req.getRequestDispatcher("/secure/conges.jsp").forward(req, resp);
    }
}
