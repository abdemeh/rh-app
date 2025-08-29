package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
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

@WebServlet("/secure/conges/create")
public class CongeCreateServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;
    @Resource private UserTransaction utx;

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        String typeId = req.getParameter("typeId");
        String d1s = req.getParameter("dateDebut");
        String d2s = req.getParameter("dateFin");
        String motif = req.getParameter("motif");

        try {
            if (typeId==null || d1s==null || d2s==null) throw new IllegalArgumentException("Champs requis manquants.");
            LocalDate d1 = LocalDate.parse(d1s);
            LocalDate d2 = LocalDate.parse(d2s);
            if (d2.isBefore(d1)) throw new IllegalArgumentException("Fin avant début.");

            Utilisateur me = em.find(Utilisateur.class, uid);
            CongeType type = em.find(CongeType.class, Integer.valueOf(typeId));
            long days = java.time.temporal.ChronoUnit.DAYS.between(d1, d2) + 1;

            utx.begin();
            Conge c = new Conge();
            c.setUtilisateur(me);
            c.setType(type);
            c.setDateDebut(d1);
            c.setDateFin(d2);
            c.setNbJours(new BigDecimal(days));
            c.setMotif(motif);
            c.setStatut("en_attente");
            em.persist(c);
            utx.commit();

            resp.sendRedirect(req.getContextPath()+"/secure/conges?ok=1");
        } catch (Exception e) {
            try { if (utx!=null) utx.rollback(); } catch (Exception ignore) {}
            resp.sendRedirect(req.getContextPath()+"/secure/conges?err=1");
        }
    }
}
