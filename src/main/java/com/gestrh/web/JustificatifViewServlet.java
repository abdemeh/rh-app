package com.gestrh.web;

import com.gestrh.entity.Conge;
import jakarta.persistence.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;

@WebServlet("/secure/files/justif")
public class JustificatifViewServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendError(403); return; }

        int congeId = Integer.parseInt(req.getParameter("congeId"));
        Conge c = em.find(Conge.class, congeId);
        if (c == null || c.getJustificatifPath() == null) { resp.sendError(404); return; }

        // Allow owner or admins (simple check via role string in session if you use it)
        Object isAdmin = req.getSession().getAttribute("isAdmin");
        if (!c.getUtilisateur().getId().equals(uid) && !(isAdmin instanceof Boolean && ((Boolean)isAdmin))) {
            resp.sendError(403); return;
        }

        File f = new File(c.getJustificatifPath());
        if (!f.exists()) { resp.sendError(404); return; }

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + (c.getJustificatifName()==null?"justificatif.pdf":c.getJustificatifName()) + "\"");
        try (OutputStream out = resp.getOutputStream(); InputStream in = new FileInputStream(f)) {
            in.transferTo(out);
        }
    }
}
