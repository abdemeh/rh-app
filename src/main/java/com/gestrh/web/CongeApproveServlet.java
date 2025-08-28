package com.gestrh.web;

import com.gestrh.entity.Conge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/secure/conges/approve")
public class CongeApproveServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU")
    private EntityManager em;

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        String idStr = req.getParameter("id");
        String action = req.getParameter("action"); // "approuve" or "rejete"
        Conge c = em.find(Conge.class, Integer.valueOf(idStr));
        if (c != null) {
            c.setStatut("approuve".equals(action) ? "approuve" : "rejete");
            // (you can also insert a CongeApprobation row here)
        }
        resp.sendRedirect(req.getContextPath()+"/secure/conges");
    }
}
