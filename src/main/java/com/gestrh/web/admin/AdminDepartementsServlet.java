package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/departements")
public class AdminDepartementsServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Departement> depts = em.createQuery("SELECT d FROM Departement d ORDER BY d.nom", Departement.class).getResultList();
        req.setAttribute("depts", depts);
        req.getRequestDispatcher("/admin/departements.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String nom = req.getParameter("nom");
        if (nom != null && !nom.isBlank()) {
            Departement d = new Departement();
            d.setNom(nom);
            em.persist(d);
        }
        resp.sendRedirect(req.getContextPath()+"/admin/departements");
    }
}
