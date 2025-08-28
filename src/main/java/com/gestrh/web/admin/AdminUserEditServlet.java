package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users/edit")
public class AdminUserEditServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        Utilisateur u = (id!=null) ? em.find(Utilisateur.class, Integer.valueOf(id)) : new Utilisateur();
        List<Departement> depts = em.createQuery("SELECT d FROM Departement d ORDER BY d.nom", Departement.class).getResultList();

        req.setAttribute("u", u);
        req.setAttribute("depts", depts);
        req.getRequestDispatcher("/admin/user_form.jsp").forward(req, resp);
    }
}
