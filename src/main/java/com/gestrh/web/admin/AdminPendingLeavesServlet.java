package com.gestrh.web.admin;

import com.gestrh.entity.Conge;
import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/conges/pending")
public class AdminPendingLeavesServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Conge> list = em.createQuery(
                "SELECT c FROM Conge c JOIN FETCH c.utilisateur u JOIN FETCH c.type t WHERE c.statut = 'en_attente' ORDER BY c.createdAt DESC",
                Conge.class).getResultList();
        req.setAttribute("list", list);
        req.getRequestDispatcher("/admin/conges_pending.jsp").forward(req, resp);
    }
}
