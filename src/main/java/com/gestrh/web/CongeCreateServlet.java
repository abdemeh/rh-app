package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.UserTransaction;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@WebServlet("/secure/conges/create")
@MultipartConfig( maxFileSize = 5 * 1024 * 1024 ) // 5MB
public class CongeCreateServlet extends HttpServlet {
    @PersistenceContext(unitName="gestRH-PU") private EntityManager em;
    @Resource private UserTransaction utx;

    private static final String DEFAULT_UPLOAD_DIR = System.getProperty("user.home") + File.separator + "gest-rh-uploads";

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

            // If type requires doc, enforce a PDF file is uploaded
            Part part = req.getPart("justif"); // may be null if no file field
            boolean needDoc = (type != null && Boolean.TRUE.equals(type.getRequiresDoc()));
            if (needDoc) {
                if (part == null || part.getSize() == 0) throw new IllegalArgumentException("Justificatif requis.");
                String ct = part.getContentType();
                if (ct == null || !ct.equalsIgnoreCase("application/pdf")) {
                    throw new IllegalArgumentException("Le justificatif doit être un PDF.");
                }
            }

            String uploadDir = System.getProperty("gest.upload.dir", DEFAULT_UPLOAD_DIR);
            Files.createDirectories(Paths.get(uploadDir));

            String savedPath = null, origName = null, mime = null;
            if (part != null && part.getSize() > 0) {
                origName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                mime = part.getContentType();
                String ext = ".pdf";
                String name = UUID.randomUUID() + ext;
                Path dest = Paths.get(uploadDir, name);
                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                savedPath = dest.toString();
            }

            utx.begin();
            Conge c = new Conge();
            c.setUtilisateur(me);
            c.setType(type);
            c.setDateDebut(d1);
            c.setDateFin(d2);
            c.setNbJours(new BigDecimal(days));
            c.setMotif(motif);
            c.setStatut("en_attente");
            if (savedPath != null) {
                c.setJustificatifPath(savedPath);
                c.setJustificatifName(origName);
                c.setJustificatifType(mime);
            }
            em.persist(c);
            utx.commit();

            resp.sendRedirect(req.getContextPath()+"/secure/conges?ok=1");
        } catch (Exception e) {
            try { if (utx!=null) utx.rollback(); } catch (Exception ignore) {}
            resp.sendRedirect(req.getContextPath()+"/secure/conges?err=1");
        }
    }
}
