package com.gestrh.web;

import com.gestrh.entity.Conge;
import com.gestrh.entity.CongeType;
import com.gestrh.entity.Utilisateur;

import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.transaction.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@WebServlet("/secure/conges/create")
@MultipartConfig(
        fileSizeThreshold = 256 * 1024,   // 256 KB in memory
        maxFileSize       = 5L * 1024 * 1024,     // 5 MB
        maxRequestSize    = 6L * 1024 * 1024      // 6 MB
)
public class CongeCreateServlet extends HttpServlet {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    // Where PDFs are stored
    private Path rootUploadDir;

    @Override
    public void init() throws ServletException {
        // default to ~/gest-rh-uploads/justifs
        String home = System.getProperty("user.home");
        String configured = System.getProperty("gest.upload.dir"); // optional -Dgest.upload.dir=/path
        Path base = Paths.get(configured != null ? configured : home + "/gest-rh-uploads");
        rootUploadDir = base.resolve("justifs");

        try {
            Files.createDirectories(rootUploadDir);
        } catch (IOException e) {
            throw new ServletException("Cannot create upload directory: " + rootUploadDir, e);
        }
    }

    @Override
    @Transactional
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");

        // Must be multipart to receive files
        String ctype = req.getContentType();
        if (ctype == null || !ctype.toLowerCase().startsWith("multipart/")) {
            setErrorAndBack(req, resp, "Formulaire invalide (multipart manquant).");
            return;
        }

        try {
            int typeId       = Integer.parseInt(req.getParameter("typeId"));
            LocalDate du     = LocalDate.parse(req.getParameter("du"));
            LocalDate au     = LocalDate.parse(req.getParameter("au"));
            String motif     = trimOrNull(req.getParameter("motif"));

            if (au.isBefore(du)) {
                setErrorAndBack(req, resp, "La date de fin doit être ≥ la date de début.");
                return;
            }

            // Load entities
            Utilisateur u  = em.find(Utilisateur.class, userId);
            CongeType type = em.find(CongeType.class, typeId);
            if (u == null || type == null) {
                setErrorAndBack(req, resp, "Utilisateur ou type de congé introuvable.");
                return;
            }

            // File part (may be null if not required)
            Part part = null;
            try {
                part = req.getPart("justificatif");
            } catch (IllegalStateException tooBig) {
                setErrorAndBack(req, resp, "Fichier trop volumineux (max 5 Mo).");
                return;
            }

            String savedPath = null, savedName = null, savedMime = null;

            boolean requiresDoc = type.isRequiresDoc(); // or getRequiresDoc() depending on your getter
            boolean hasFile = (part != null && part.getSize() > 0);

            if (requiresDoc && !hasFile) {
                setErrorAndBack(req, resp, "Ce type de congé exige un justificatif PDF.");
                return;
            }

            if (hasFile) {
                // Validate PDF
                String submittedName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String contentType = part.getContentType();
                if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                    setErrorAndBack(req, resp, "Le justificatif doit être un PDF.");
                    return;
                }
                if (part.getSize() > 5L * 1024 * 1024) {
                    setErrorAndBack(req, resp, "Le justificatif dépasse 5 Mo.");
                    return;
                }

                // Save to disk
                String uuid = UUID.randomUUID().toString().replace("-", "");
                String storedFileName = uuid + ".pdf";
                Path dest = rootUploadDir.resolve(storedFileName);
                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }

                savedPath = storedFileName;     // store only relative name
                savedName = submittedName;
                savedMime = "application/pdf";
            }

            // Compute nb jours simple (inclusif) — adjust to your business rules
            long nbJours = java.time.temporal.ChronoUnit.DAYS.between(du, au) + 1;

            // Persist Conge
            Conge c = new Conge();
            c.setUtilisateur(u);
            c.setType(type);
            c.setDateDebut(du);
            c.setDateFin(au);
            c.setNbJours(new java.math.BigDecimal(nbJours));
            c.setStatut("en_attente");
            c.setMotif(motif);
            c.setJustificatifPath(savedPath);
            c.setJustificatifName(savedName);
            c.setJustificatifType(savedMime);

            em.persist(c);

            // Success message, redirect pattern (PRG)
            session.setAttribute("flash_success", "Demande enregistrée.");
            resp.sendRedirect(req.getContextPath() + "/secure/conges");
        } catch (Exception e) {
            e.printStackTrace();
            setErrorAndBack(req, resp, "Erreur lors de l’enregistrement de la demande.");
        }
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private void setErrorAndBack(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws IOException, ServletException {
        req.getSession().setAttribute("flash_error", msg);
        resp.sendRedirect(req.getContextPath() + "/secure/conges");
    }
}
