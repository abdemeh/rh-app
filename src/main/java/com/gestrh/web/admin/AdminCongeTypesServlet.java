package com.gestrh.web.admin;

import com.gestrh.entity.CongeType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/conges/types")
public class AdminCongeTypesServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }
    private Boolean parseBool(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase();
        return ("true".equals(v) || "1".equals(v) || "on".equals(v) || "yes".equals(v));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<CongeType> types = em.createQuery(
                "SELECT t FROM CongeType t ORDER BY t.id DESC", CongeType.class
        ).getResultList();

        req.setAttribute("types", types);
        req.getRequestDispatcher("/admin/conges_types.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");       // create | update | delete
        Integer id    = parseIntOrNull(req.getParameter("id"));

        String code            = req.getParameter("code");
        String libelle         = req.getParameter("libelle");
        Integer maxJoursAn     = parseIntOrNull(req.getParameter("max_jours_an"));
        Integer approvalLevels = parseIntOrNull(req.getParameter("approval_levels"));
        Boolean requiresDoc    = parseBool(req.getParameter("requires_doc"));
        Boolean actif          = parseBool(req.getParameter("actif"));

        try {
            utx.begin();

            if ("delete".equalsIgnoreCase(action) && id != null) {
                CongeType t = em.find(CongeType.class, id);
                if (t != null) em.remove(t);

            } else if ("update".equalsIgnoreCase(action) && id != null) {
                CongeType t = em.find(CongeType.class, id);
                if (t == null) { utx.rollback(); resp.sendError(404, "Type introuvable"); return; }

                if (code != null)            t.setCode(code);
                if (libelle != null)         t.setLibelle(libelle);
                if (maxJoursAn != null)      t.setMaxJoursAn(maxJoursAn);
                if (approvalLevels != null)  t.setApprovalLevels(approvalLevels);
                if (requiresDoc != null)     t.setRequiresDoc(requiresDoc);
                if (actif != null)           t.setActif(actif);

                em.merge(t);

            } else { // create
                CongeType t = new CongeType();
                t.setCode(code);
                t.setLibelle(libelle);
                t.setMaxJoursAn(maxJoursAn);
                t.setApprovalLevels(approvalLevels);
                t.setRequiresDoc(requiresDoc);
                t.setActif(actif);

                em.persist(t);
            }

            utx.commit();
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de l’écriture du type de congé");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/conges/types");
    }
}
