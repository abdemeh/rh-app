package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

@WebServlet("/file/conge")
public class FileDownloadServlet extends HttpServlet {

    @PersistenceContext(unitName="gestRH-PU")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) { resp.sendError(401); return; }

        String idStr = req.getParameter("id");
        if (idStr == null) { resp.sendError(400, "id manquant"); return; }

        Conge c = em.find(Conge.class, Integer.valueOf(idStr));
        if (c == null || c.getJustificatifPath() == null) { resp.sendError(404); return; }

        // Authorization: admin can see all; user can see only own conge
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) req.getSession().getAttribute("roles");
        boolean isAdmin = (roles != null && roles.contains("ADMIN"));
        if (!isAdmin && (c.getUtilisateur() == null || !c.getUtilisateur().getId().equals(uid))) {
            resp.sendError(403); return;
        }

        File f = new File(c.getJustificatifPath());
        if (!f.exists()) { resp.sendError(404); return; }

        resp.setContentType("application/pdf");
        // Show inline in browser, with a sensible filename
        String name = (c.getJustificatifName() != null) ? c.getJustificatifName() : ("conge-" + c.getId() + ".pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + name.replace("\"","") + "\"");
        resp.setContentLengthLong(f.length());

        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(f.toPath(), out);
        }
    }
}
