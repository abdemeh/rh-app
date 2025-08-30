package com.gestrh.web.account;

import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;

@WebServlet("/account/profile/save")
public class ProfileSaveServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { // <-- change if your key differs
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId"); // <--

        Utilisateur u = em.find(Utilisateur.class, userId);
        if (u == null) { resp.sendError(404, "Utilisateur introuvable"); return; }

        String nom        = req.getParameter("nom");
        String prenom     = req.getParameter("prenom");
        String email      = req.getParameter("email");
        String password   = req.getParameter("mot_de_passe"); // empty => keep
        String adresse    = req.getParameter("adresse");
        String telephone  = req.getParameter("telephone");

        try {
            utx.begin();

            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            if (password != null && !password.isBlank()) {
                u.setMotDePasse(password); // TODO: hash if you have a password encoder
            }
            u.setAdresse(adresse);
            u.setTelephone(telephone);

            em.merge(u);
            utx.commit();
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de la mise à jour du profil");
            return;
        }

        // Back to profile with success message
        resp.sendRedirect(req.getContextPath() + "/account/profile?ok=1");
    }
}
