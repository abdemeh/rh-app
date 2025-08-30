package com.gestrh.web.calendar;

import com.gestrh.entity.CalendrierEvenement;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Parse month param (yyyy-MM)
        String monthParam = req.getParameter("month");
        YearMonth ym;
        try { ym = (monthParam == null || monthParam.isBlank()) ? YearMonth.now() : YearMonth.parse(monthParam); }
        catch (Exception e) { ym = YearMonth.now(); }

        LocalDate firstOfMonth = ym.atDay(1);
        DayOfWeek firstDow = firstOfMonth.getDayOfWeek();
        int shift = (firstDow.getValue() + 6) % 7; // Monday=0
        LocalDate gridStart = firstOfMonth.minusDays(shift);
        LocalDate gridEnd = gridStart.plusDays(6 * 7 - 1);

        // Load events in range
        List<CalendrierEvenement> evts = em.createQuery(
                        "SELECT e FROM CalendrierEvenement e " +
                                "LEFT JOIN FETCH e.creePar cp " +
                                "WHERE e.dateEvenement BETWEEN :start AND :end " +
                                "ORDER BY e.dateEvenement ASC, e.id ASC", CalendrierEvenement.class)
                .setParameter("start", gridStart)
                .setParameter("end", gridEnd)
                .getResultList();

        // Build map keyed by ISO date string for easy EL access
        Map<String, List<CalendrierEvenement>> eventsByDateStr = new HashMap<>();
        for (CalendrierEvenement e : evts) {
            String k = e.getDateEvenement().toString();
            eventsByDateStr.computeIfAbsent(k, __ -> new ArrayList<>()).add(e);
        }

        // Build 6x7 grid as ISO strings so JSP can use pure EL
        List<List<String>> weeksIso = new ArrayList<>();
        LocalDate d = gridStart;
        for (int w = 0; w < 6; w++) {
            List<String> week = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                week.add(d.toString());
                d = d.plusDays(1);
            }
            weeksIso.add(week);
        }

        // Upcoming (14 days)
        LocalDate today = LocalDate.now();
        LocalDate upcomingEnd = today.plusDays(14);
        List<CalendrierEvenement> upcoming = em.createQuery(
                        "SELECT e FROM CalendrierEvenement e " +
                                "LEFT JOIN FETCH e.creePar cp " +
                                "WHERE e.dateEvenement BETWEEN :t0 AND :t1 " +
                                "ORDER BY e.dateEvenement ASC, e.id ASC", CalendrierEvenement.class)
                .setParameter("t0", today)
                .setParameter("t1", upcomingEnd)
                .getResultList();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH);
        List<Map<String, String>> upcomingVM = new ArrayList<>();
        for (CalendrierEvenement e : upcoming) {
            Map<String, String> m = new HashMap<>();
            m.put("dateLabel", df.format(e.getDateEvenement()));
            m.put("title", e.getTitre() == null ? "" : e.getTitre());
            Utilisateur u = e.getCreePar();
            m.put("creator", (u != null) ? (String.valueOf(u.getPrenom()) + " " + String.valueOf(u.getNom())) : "Anonyme");
            m.put("dateIso", e.getDateEvenement().toString());
            m.put("desc", e.getDescription() == null ? "" : e.getDescription());
            upcomingVM.add(m);
        }

        // Navigation strings
        String monthStr = ym.toString(); // yyyy-MM
        String prevMonth = ym.minusMonths(1).toString();
        String nextMonth = ym.plusMonths(1).toString();

        // Flags for styling in JSP
        req.setAttribute("yearMonth", ym);
        req.setAttribute("monthValue", ym.getMonthValue());
        req.setAttribute("yearValue", ym.getYear());
        req.setAttribute("weeksIso", weeksIso);
        req.setAttribute("eventsByDateStr", eventsByDateStr);
        req.setAttribute("todayIso", today.toString());
        req.setAttribute("monthStr", monthStr);
        req.setAttribute("prevMonth", prevMonth);
        req.setAttribute("nextMonth", nextMonth);
        req.setAttribute("upcomingVM", upcomingVM);

        req.getRequestDispatcher("/calendar/index.jsp").forward(req, resp);
    }
}
