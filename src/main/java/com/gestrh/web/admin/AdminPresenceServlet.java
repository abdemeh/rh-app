package com.gestrh.web.admin;

import com.gestrh.entity.Presence;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/admin/presences")
public class AdminPresenceServlet extends HttpServlet {
    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.now();
        LocalDate mStart = ym.atDay(1);
        LocalDate mEnd   = ym.atEndOfMonth();

        // utilisateurs
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class).getResultList();
        req.setAttribute("users", users);

        // présences du jour
        List<Presence> todayAll = em.createQuery(
                        "SELECT p FROM Presence p JOIN FETCH p.utilisateur u WHERE p.jour=:j", Presence.class)
                .setParameter("j", today).getResultList();
        Map<Integer, Presence> todayByUser = todayAll.stream()
                .collect(Collectors.toMap(p->p.getUtilisateur().getId(), p->p, (a,b)->a, LinkedHashMap::new));
        req.setAttribute("todayByUser", todayByUser);

        // présences du mois
        List<Presence> monthAll = em.createQuery(
                        "SELECT p FROM Presence p JOIN FETCH p.utilisateur u WHERE p.jour BETWEEN :s AND :e ORDER BY p.jour DESC", Presence.class)
                .setParameter("s", mStart).setParameter("e", mEnd).getResultList();

        // agrégats du mois par user
        class Agg { int days; int minutes; }
        Map<Integer, Agg> aggMap = new LinkedHashMap<>();
        for (Presence p : monthAll) {
            int uid = p.getUtilisateur().getId();
            Agg a = aggMap.computeIfAbsent(uid, k->new Agg());
            a.days += 1;
            a.minutes += (p.getDureeMinutes()==null?0:p.getDureeMinutes());
        }
        req.setAttribute("monthAgg", aggMap);
        req.setAttribute("mLabel", ym.toString());

        // Bonus indicatif : si minutes >= (20 jours * 8h) => “✔ bonus”
        Map<Integer, String> bonusMap = new LinkedHashMap<>();
        int targetMinutes = 20 * 8 * 60;
        for (Map.Entry<Integer, Agg> e : aggMap.entrySet()) {
            bonusMap.put(e.getKey(), e.getValue().minutes >= targetMinutes ? "✔ Bonus" : "—");
        }
        req.setAttribute("bonusMap", bonusMap);

        req.getRequestDispatcher("/admin/presences.jsp").forward(req, resp);
    }
}
