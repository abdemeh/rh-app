package com.gestrh.web.calendar;

import com.gestrh.entity.CalendrierEvenement;
import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@WebServlet("/calendar/save")
public class CalendarSaveServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { // <--
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId"); // <--

        String titre = req.getParameter("titre");
        String description = req.getParameter("description");
        String dateStr = req.getParameter("date_evenement"); // yyyy-MM-dd expected
        String month = req.getParameter("month"); // keep current view after redirect

        LocalDate date;
        try { date = LocalDate.parse(dateStr); } catch (Exception e) { date = LocalDate.now(); }

        try {
            utx.begin();

            Utilisateur user = em.find(Utilisateur.class, userId);
            CalendrierEvenement e = new CalendrierEvenement();
            e.setTitre(titre);
            e.setDescription(description);
            e.setDateEvenement(date);
            e.setCreePar(user);
            e.setCreatedAt(LocalDateTime.now());

            em.persist(e);
            utx.commit();
        } catch (Exception ex) {
            try { utx.rollback(); } catch (Exception ignore) {}
            resp.sendError(500, "Erreur lors de l’enregistrement de l’événement");
            return;
        }

        String redirectMonth = (month != null && !month.isBlank()) ? month : (date.getYear() + "-" + String.format("%02d", date.getMonthValue()));
        resp.sendRedirect(req.getContextPath() + "/calendar?month=" + redirectMonth + "&success=created");
    }
}
