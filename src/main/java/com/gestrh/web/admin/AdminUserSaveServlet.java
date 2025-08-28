package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

@WebServlet("/admin/users/save")
public class AdminUserSaveServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String id = req.getParameter("id");
        Utilisateur u = (id!=null && !id.isBlank()) ? em.find(Utilisateur.class, Integer.valueOf(id)) : new Utilisateur();

        u.setPrenom(req.getParameter("prenom"));
        u.setNom(req.getParameter("nom"));
        u.setEmail(req.getParameter("email"));
        u.setStatut(req.getParameter("statut"));

        String depId = req.getParameter("departementId");
        if (depId!=null && !depId.isBlank()) {
            u.setDepartement(em.find(Departement.class, Integer.valueOf(depId)));
        } else {
            u.setDepartement(null);
        }

        String plain = req.getParameter("password");
        if (plain != null && !plain.isBlank()) {
            u.setMotDePasse(BCrypt.hashpw(plain, BCrypt.gensalt(12)));
        } else if (u.getId() == null) {
            // require password on create
            req.setAttribute("u", u);
            req.setAttribute("error", "Mot de passe requis pour une création");
            req.getRequestDispatcher("/admin/user_form.jsp").forward(req, resp);
            return;
        }

        if (u.getId() == null) em.persist(u);
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
