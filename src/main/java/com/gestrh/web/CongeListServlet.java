package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/secure/conges")
public class CongeListServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        Utilisateur me = em.find(Utilisateur.class, uid);

        List<Conge> conges = em.createQuery(
                        "SELECT c FROM Conge c JOIN FETCH c.type t WHERE c.utilisateur = :me ORDER BY c.createdAt DESC", Conge.class)
                .setParameter("me", me)
                .getResultList();

        List<CongeType> types = em.createQuery(
                "SELECT t FROM CongeType t ORDER BY t.libelle", CongeType.class).getResultList();

        req.setAttribute("conges", conges);
        req.setAttribute("types", types);
        req.getRequestDispatcher("/secure/conges.jsp").forward(req, resp);
    }
}
