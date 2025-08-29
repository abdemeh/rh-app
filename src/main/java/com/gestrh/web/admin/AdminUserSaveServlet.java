package com.gestrh.web.admin;

import com.gestrh.entity.Departement;
import com.gestrh.entity.Poste;
import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/admin/users/save")
public class AdminUserSaveServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        try { return Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }
    private BigDecimal parseMoney(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (Exception e) { return null; }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // IDs: safely parse; non-numeric becomes null instead of throwing
        Integer id            = parseIntOrNull(req.getParameter("id"));
        Integer posteId       = parseIntOrNull(req.getParameter("poste_id"));
        Integer departementId = parseIntOrNull(req.getParameter("departement_id"));
        Integer managerId     = parseIntOrNull(req.getParameter("manager_id"));

        // Simple fields
        String nom        = req.getParameter("nom");
        String prenom     = req.getParameter("prenom");
        String email      = req.getParameter("email");
        String motDePasse = req.getParameter("mot_de_passe"); // blank => keep existing
        String adresse    = req.getParameter("adresse");
        String telephone  = req.getParameter("telephone");
        String statut     = req.getParameter("statut");
        String contratType= req.getParameter("contrat_type");
        LocalDate dateEmbauche = parseDate(req.getParameter("date_embauche"));
        LocalDate dateSortie   = parseDate(req.getParameter("date_sortie"));
        BigDecimal salaireBase = parseMoney(req.getParameter("salaire_base"));

        try {
            utx.begin();

            Utilisateur u = (id != null) ? em.find(Utilisateur.class, id) : new Utilisateur();
            if (id != null && u == null) { utx.rollback(); resp.sendError(404, "Utilisateur introuvable"); return; }

            // Map fields
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            if (motDePasse != null && !motDePasse.isBlank()) {
                u.setMotDePasse(motDePasse); // TODO: hash if needed
            }
            u.setAdresse(adresse);
            u.setTelephone(telephone);
            u.setStatut(statut);
            u.setContratType(contratType);
            u.setDateEmbauche(dateEmbauche);
            u.setDateSortie(dateSortie);
            u.setSalaireBase(salaireBase);

            // Relations (null-safe)
            u.setPoste(posteId != null ? em.find(Poste.class, posteId) : null);
            u.setDepartement(departementId != null ? em.find(Departement.class, departementId) : null);
            u.setManager(managerId != null ? em.find(Utilisateur.class, managerId) : null);

            if (id == null) em.persist(u); else em.merge(u);

            utx.commit();
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de l'enregistrement"); return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
