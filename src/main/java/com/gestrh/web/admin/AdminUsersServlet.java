package com.gestrh.web.admin;

import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class).getResultList();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }
}
