package com.gestrh.web.admin;

import com.gestrh.entity.Conge;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;

import java.io.IOException;

@WebServlet("/admin/conges/approve")
public class AdminApproveLeaveServlet extends HttpServlet {
    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr  = req.getParameter("id");
        String action = req.getParameter("action"); // "approuve" | "rejete"

        if (idStr == null || idStr.isBlank() || action == null || action.isBlank()) {
            resp.sendError(400, "Paramètres manquants"); return;
        }

        try {
            utx.begin();
            Conge c = em.find(Conge.class, Integer.valueOf(idStr));
            if (c == null) { utx.rollback(); resp.sendError(404, "Congé introuvable"); return; }
            if (!"en_attente".equals(c.getStatut())) {
                utx.rollback(); resp.sendError(409, "Déjà traité"); return;
            }
            c.setStatut("approuve".equalsIgnoreCase(action) ? "approuve" : "rejete");
            em.merge(c);
            utx.commit();
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de la mise à jour"); return;
        }

        // Si appelé via fetch (AJAX) -> JSON ; sinon redirection
        if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"ok\":true,\"id\":" + idStr + ",\"statut\":\"" + action + "\"}");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/conges/pending");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(405);
    }
}
