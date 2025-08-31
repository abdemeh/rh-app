package com.gestrh.web.secure;

import com.gestrh.entity.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/secure/ping")
public class PingServlet extends HttpServlet {
    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private Integer me(HttpSession s){
        if(s==null) return null;
        Object o=s.getAttribute("userId");
        if(o==null) return null;
        try { return (o instanceof Integer)?(Integer)o:Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer uid = me(req.getSession(false));
        if (uid != null) {
            try {
                utx.begin();
                em.joinTransaction();

                Utilisateur u = em.find(Utilisateur.class, uid);
                if (u != null) {
                    u.setLastLogin(LocalDateTime.now());   // your setter expects LocalDateTime
                    em.merge(u);
                }

                utx.commit();
            } catch (Exception ex) {
                try { utx.rollback(); } catch (Exception ignore) {}
                resp.sendError(500, "Ping failed: " + ex.getMessage());
                return;
            }
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"ok\":true}");
    }
}
