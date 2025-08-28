package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.Utilisateur;
import jakarta.persistence.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

@WebServlet("/secure/files/justif")
public class JustificatifViewServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;

    private static final String BASE_DIR =
            System.getProperty("user.home") + "/gest-rh-uploads/justifs";

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");

        String idParam = req.getParameter("congeId");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "congeId manquant");
            return;
        }
        Integer congeId = Integer.valueOf(idParam);

        Conge conge = em.find(Conge.class, congeId);
        if (conge == null || conge.getJustificatifPath() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean isOwner = conge.getUtilisateur().getId().equals(userId);

        // Vérifie si l’utilisateur a le rôle ADMIN (sans getRoles())
        boolean isAdmin = false;
        try {
            @SuppressWarnings("unchecked")
            List<Object> rows = em.createNativeQuery(
                            "SELECT 1 FROM user_roles ur " +
                                    "JOIN roles r ON r.id = ur.role_id " +
                                    "WHERE ur.utilisateur_id = ? AND r.code = 'ADMIN' " +
                                    "LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();
            isAdmin = !rows.isEmpty();
        } catch (Exception ignored) {}

        if (!isOwner && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path file = Paths.get(BASE_DIR).resolve(conge.getJustificatifPath()).normalize();
        if (!Files.exists(file) || !file.toString().toLowerCase().endsWith(".pdf")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentType("application/pdf");
        String fname = conge.getJustificatifName() != null ? conge.getJustificatifName() : "justificatif.pdf";
        resp.setHeader("Content-Disposition", "inline; filename=\"" + fname.replace("\"","") + "\"");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file, out);
        }
    }
}
