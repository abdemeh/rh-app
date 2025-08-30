package com.gestrh.web.account;

import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/account/profile")
public class ProfileServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Get current user id from session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { // <-- change if your key differs
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId"); // <--

        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) {
            resp.sendError(404, "Utilisateur introuvable");
            return;
        }

        req.setAttribute("u", u);
        req.getRequestDispatcher("/account/profile.jsp").forward(req, resp);
    }
}
