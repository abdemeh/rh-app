package com.gestrh.web.admin;

import com.gestrh.entity.Conge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebServlet("/admin/conges/approve")
public class AdminApproveLeaveServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // RoleFilter should protect /admin/*, but double-check:
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) req.getSession().getAttribute("roles");
        if (roles == null || !roles.contains("ADMIN")) { resp.sendError(403); return; }

        String idStr = req.getParameter("id");
        String action = req.getParameter("action"); // approuve | rejete

        if (idStr != null && !idStr.isBlank()) {
            Conge c = em.find(Conge.class, Integer.valueOf(idStr));
            if (c != null && "en_attente".equals(c.getStatut())) {
                c.setStatut("approuve".equals(action) ? "approuve" : "rejete");
            }
        }
        resp.sendRedirect(req.getContextPath()+"/admin/conges/pending");
    }
}
