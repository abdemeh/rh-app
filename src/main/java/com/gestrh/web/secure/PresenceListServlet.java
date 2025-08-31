package com.gestrh.web.secure;

import com.gestrh.entity.Presence;
import com.gestrh.entity.PresenceInterval;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/secure/presences")
public class PresenceListServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    private Integer me(HttpSession s){
        if(s==null) return null;
        Object o=s.getAttribute("userId");
        if(o==null) return null;
        try { return (o instanceof Integer)?(Integer)o:Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer uid = me(req.getSession(false));
        if (uid == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        Utilisateur me = em.find(Utilisateur.class, uid);
        req.setAttribute("me", me);

        LocalDate today = LocalDate.now();

        // A) Aujourd'hui : présence + intervalles
        List<Presence> todayList = em.createQuery(
                        "SELECT p FROM Presence p WHERE p.utilisateur.id=:uid AND p.jour=:j", Presence.class)
                .setParameter("uid", uid)
                .setParameter("j", today)
                .setMaxResults(1)
                .getResultList();

        Presence day = todayList.isEmpty() ? null : todayList.get(0);
        req.setAttribute("todayPresence", day);

        List<PresenceInterval> todayIntervals;
        if (day == null) {
            todayIntervals = Collections.emptyList();
        } else {
            todayIntervals = em.createQuery(
                            "SELECT i FROM PresenceInterval i WHERE i.presence=:p ORDER BY i.startTime ASC, i.id ASC",
                            PresenceInterval.class)
                    .setParameter("p", day)
                    .getResultList();
        }
        req.setAttribute("todayIntervals", todayIntervals);

        boolean hasOpenSession = false;
        for (PresenceInterval pi : todayIntervals) {
            if (pi.getEndTime() == null) { hasOpenSession = true; break; }
        }
        req.setAttribute("hasOpenSession", hasOpenSession);

        // B) 30 derniers jours : jours + intervalles + totaux et compte de sessions terminées
        LocalDate start = today.minusDays(29);
        List<Presence> days = em.createQuery(
                        "SELECT p FROM Presence p WHERE p.utilisateur.id=:uid AND p.jour BETWEEN :s AND :e " +
                                "ORDER BY p.jour DESC", Presence.class)
                .setParameter("uid", uid)
                .setParameter("s", start)
                .setParameter("e", today)
                .getResultList();
        req.setAttribute("days", days);

        Map<Integer, Integer> totals = new LinkedHashMap<>(); // presence.id -> minutes
        Map<Integer, Integer> counts = new LinkedHashMap<>(); // presence.id -> nb intervalles clos
        if (!days.isEmpty()) {
            List<Presence> forIn = new ArrayList<>(days);

            List<PresenceInterval> ints = em.createQuery(
                            "SELECT i FROM PresenceInterval i WHERE i.presence IN :ps",
                            PresenceInterval.class)
                    .setParameter("ps", forIn)
                    .getResultList();

            for (Presence p : days) {
                totals.put(p.getId(), 0);
                counts.put(p.getId(), 0);
            }

            for (PresenceInterval i : ints) {
                Integer pid = i.getPresence().getId();

                // minutes (recalcule si la colonne générée est NULL)
                Integer m = i.getMinutes();
                if (m == null && i.getStartTime() != null && i.getEndTime() != null) {
                    long mm = java.time.Duration.between(i.getStartTime(), i.getEndTime()).toMinutes();
                    m = (int) Math.max(0, mm);
                }
                if (m != null) {
                    totals.put(pid, totals.get(pid) + m);
                }

                // compter seulement les sessions terminées
                if (i.getEndTime() != null) {
                    counts.put(pid, counts.get(pid) + 1);
                }
            }
        }
        req.setAttribute("totalsByPresenceId", totals);
        req.setAttribute("countsByPresenceId", counts);

        // C) Statuts collègues
        List<Utilisateur> all = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class).getResultList();
        req.setAttribute("allUsers", all);

        req.setAttribute("onlineThresholdMinutes", 5);

        req.getRequestDispatcher("/secure/presences.jsp").forward(req, resp);
    }
}
