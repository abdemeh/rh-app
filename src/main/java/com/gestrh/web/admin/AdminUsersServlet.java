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

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // No references to possibly-missing fields like deletedAt/dateCreation
        List<Utilisateur> users = em.createQuery(
                "SELECT DISTINCT u FROM Utilisateur u " +
                        "LEFT JOIN FETCH u.poste " +
                        "LEFT JOIN FETCH u.departement " +
                        "LEFT JOIN FETCH u.manager " +
                        "ORDER BY u.id DESC",
                Utilisateur.class
        ).getResultList();

        req.setAttribute("users", users);
        // JSP path: src/main/webapp/admin/users.jsp
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }
}
