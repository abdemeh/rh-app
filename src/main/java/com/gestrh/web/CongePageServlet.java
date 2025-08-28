// src/main/java/com/gestrh/web/CongePageServlet.java
package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
import jakarta.persistence.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

@WebServlet("/secure/conges")
public class CongePageServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        Integer userId = (Integer) s.getAttribute("userId");

        List<CongeType> types = em.createQuery("SELECT t FROM CongeType t WHERE t.actif = true ORDER BY t.libelle",
                CongeType.class).getResultList();
        List<Conge> conges = em.createQuery(
                        "SELECT c FROM Conge c WHERE c.utilisateur.id = :u ORDER BY c.id DESC", Conge.class)
                .setParameter("u", userId)
                .getResultList();

        req.setAttribute("types", types);
        req.setAttribute("conges", conges);
        req.getRequestDispatcher("/secure/conges.jsp").forward(req, resp);
    }
}
