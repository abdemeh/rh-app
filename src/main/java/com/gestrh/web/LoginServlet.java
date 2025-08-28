package com.gestrh.web;

import com.gestrh.entity.Utilisateur;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Utilisateur user = em.createQuery(
                        "SELECT u FROM Utilisateur u WHERE u.email = :e", Utilisateur.class)
                .setParameter("e", email)
                .getResultStream().findFirst().orElse(null);

        if (user != null && BCrypt.checkpw(password, user.getMotDePasse())) {
            // LoginServlet.java (inside doPost, after successful password check)

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getPrenom() + " " + user.getNom());

// Load roles into session
            session.setAttribute("roles", com.gestrh.web.SecurityUtil.loadRoles(em, user.getId()));

// If you previously stored an intended URL:
            String target = (String) session.getAttribute("redirectAfterLogin");

// --- Force admins to land on Admin dashboard ---
            java.util.Set<String> roles = (java.util.Set<String>) session.getAttribute("roles");
            boolean isAdmin = (roles != null && roles.contains("ADMIN"));

            String ctx = req.getContextPath();
            if (isAdmin) {
                // Always send admins to /admin/, ignoring any earlier target
                resp.sendRedirect(ctx + "/admin/");
                return;
            }

// Non-admins: honor target if present, else go to normal home
            if (target != null && !target.isBlank()) {
                session.removeAttribute("redirectAfterLogin");
                resp.sendRedirect(target);
            } else {
                resp.sendRedirect(ctx + "/");
            }

        } else {
            req.setAttribute("error", "Invalid credentials");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
