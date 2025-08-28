package com.gestrh.web.admin;

import com.gestrh.entity.CongeType;
import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/admin/conges-types")
public class AdminCongeTypesServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<CongeType> types = em.createQuery("SELECT t FROM CongeType t ORDER BY t.libelle", CongeType.class).getResultList();
        req.setAttribute("types", types);
        req.getRequestDispatcher("/admin/conges_types.jsp").forward(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        CongeType t = (id!=null && !id.isBlank()) ? em.find(CongeType.class, Integer.valueOf(id)) : new CongeType();
        t.setCode(req.getParameter("code"));
        t.setLibelle(req.getParameter("libelle"));
        String max = req.getParameter("maxJoursAn");
        t.setMaxJoursAn((max==null || max.isBlank()) ? null : new BigDecimal(max));
        t.setApprovalLevels(Short.parseShort(req.getParameter("approvalLevels")));
        t.setRequiresDoc("on".equals(req.getParameter("requiresDoc")));
        if (t.getId()==null) em.persist(t);
        resp.sendRedirect(req.getContextPath()+"/admin/conges-types");
    }
}
