package com.gestrh.web.secure;

import com.gestrh.entity.Presence;
import com.gestrh.entity.PresenceInterval;
import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@WebServlet("/secure/presences/check")
public class PresenceCheckServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private Integer me(HttpSession s) {
        if (s == null) return null;
        Object o = s.getAttribute("userId");
        if (o == null) return null;
        try { return (o instanceof Integer) ? (Integer) o : Integer.valueOf(o.toString()); } catch (Exception e) { return null; }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer uid = me(req.getSession(false));
        if (uid == null) { resp.sendError(401); return; }

        String action = req.getParameter("action"); // "in" | "out"
        if (action == null) { resp.sendError(400, "action manquante"); return; }

        LocalDate today = LocalDate.now();

        try {
            utx.begin();
            em.joinTransaction();

            Utilisateur u = em.find(Utilisateur.class, uid);
            if (u == null) { utx.rollback(); resp.sendError(404, "user introuvable"); return; }

            // 1) garantir la ligne du jour
            Presence day = em.createQuery(
                            "SELECT p FROM Presence p WHERE p.utilisateur.id=:uid AND p.jour=:j",
                            Presence.class)
                    .setParameter("uid", uid)
                    .setParameter("j", today)
                    .setMaxResults(1)
                    .getResultStream().findFirst().orElse(null);

            if (day == null) {
                day = new Presence();
                day.setUtilisateur(u);
                day.setJour(today);
                day.setStatut("ABSENT");
                day.setHeureEntree(null);
                day.setHeureSortie(null);
                day.setCommentaire(null);
                em.persist(day);
            }

            // 2) intervalle ouvert ?
            PresenceInterval current = em.createQuery(
                            "SELECT i FROM PresenceInterval i WHERE i.presence=:p AND i.endTime IS NULL " +
                                    "ORDER BY i.startTime DESC, i.id DESC", PresenceInterval.class)
                    .setParameter("p", day)
                    .setMaxResults(1)
                    .getResultStream().findFirst().orElse(null);

            if ("in".equalsIgnoreCase(action)) {
                if (current == null) {
                    PresenceInterval ni = new PresenceInterval();
                    ni.setPresence(day);
                    ni.setStartTime(LocalDateTime.now());
                    em.persist(ni);

                    day.setStatut("PRESENT");
                    em.merge(day);
                }
                u.setLastLogin(LocalDateTime.now());
                em.merge(u);

            } else if ("out".equalsIgnoreCase(action)) {
                if (current != null) {
                    current.setEndTime(LocalDateTime.now());
                    em.merge(current);
                }
                // encore ouvert ?
                Long stillOpen = em.createQuery(
                                "SELECT COUNT(i) FROM PresenceInterval i WHERE i.presence=:p AND i.endTime IS NULL", Long.class)
                        .setParameter("p", day)
                        .getSingleResult();
                if (stillOpen == 0) {
                    day.setStatut("ABSENT");
                    em.merge(day);
                }
                u.setLastLogin(LocalDateTime.now());
                em.merge(u);

            } else {
                utx.rollback();
                resp.sendError(400, "action invalide");
                return;
            }

            em.flush();
            utx.commit();
            resp.sendRedirect(req.getContextPath()+"/secure/presences");

        } catch (Exception ex) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Presence check failed: " + (ex.getCause()!=null ? ex.getCause().getMessage() : ex.getMessage()));
        }
    }
}
