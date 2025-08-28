package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/secure/reports/my-leaves.csv")
public class MyLeavesReportServlet extends HttpServlet {

    @PersistenceContext(unitName="gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        Utilisateur me = em.find(Utilisateur.class, uid);
        List<Conge> list = em.createQuery(
                "SELECT c FROM Conge c JOIN FETCH c.type t WHERE c.utilisateur = :me ORDER BY c.createdAt DESC",
                Conge.class).setParameter("me", me).getResultList();

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"my-leaves.csv\"");
        try (PrintWriter out = resp.getWriter()) {
            out.write("\uFEFF");
            out.println("ID;Type;Du;Au;NbJours;Statut;Motif;CreeLe");
            for (Conge c : list) {
                out.printf("%s;%s;%s;%s;%s;%s;%s;%s%n",
                        c.getId(),
                        safe(c.getType().getLibelle()),
                        c.getDateDebut()!=null ? c.getDateDebut().toString() : "",
                        c.getDateFin()!=null ? c.getDateFin().toString() : "",
                        c.getNbJours()!=null ? c.getNbJours().toPlainString() : "",
                        safe(c.getStatut()),
                        safe(c.getMotif()),
                        c.getCreatedAt()!=null ? c.getCreatedAt().toString() : "");
            }
        }
    }

    private String safe(String s){ return s==null ? "" : s.replace(';', ','); }
}
