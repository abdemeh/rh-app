package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import com.gestrh.entity.Poste;
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

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        try { return Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Utilisateur u = null;
        Integer id = parseIntOrNull(req.getParameter("id"));
        if (req.getParameter("id") != null && !req.getParameter("id").isBlank()) {
            if (id == null) { resp.sendError(400, "Paramètre id invalide"); return; }
            u = em.find(Utilisateur.class, id);
            if (u == null) { resp.sendError(404, "Utilisateur introuvable"); return; }
        }

        // Match your entities: Departement.nom, Poste.intitule
        List<Departement> departements = em.createQuery(
                "SELECT d FROM Departement d ORDER BY d.nom", Departement.class
        ).getResultList();

        List<Poste> postes = em.createQuery(
                "SELECT p FROM Poste p ORDER BY p.intitule", Poste.class
        ).getResultList();

        List<Utilisateur> managers = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class
        ).getResultList();

        req.setAttribute("u", u);
        req.setAttribute("departements", departements);
        req.setAttribute("postes", postes);
        req.setAttribute("managers", managers);

        // JSP path: src/main/webapp/admin/user_form.jsp
        req.getRequestDispatcher("/admin/user_form.jsp").forward(req, resp);
    }
}
