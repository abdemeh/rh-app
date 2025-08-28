package com.gestrh.web.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/")
public class AdminDashboardServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long users = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM utilisateurs").getSingleResult()).longValue();
        Long depts = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM departements").getSingleResult()).longValue();
        Long pending = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM conges WHERE statut='en_attente'").getSingleResult()).longValue();
        req.setAttribute("usersCount", users);
        req.setAttribute("deptCount", depts);
        req.setAttribute("pendingCount", pending);
        req.getRequestDispatcher("/admin/index.jsp").forward(req, resp);
    }
}
