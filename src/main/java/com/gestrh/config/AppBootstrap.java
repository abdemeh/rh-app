package com.gestrh.config;

import com.gestrh.entity.CongeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;

@WebListener
public class AppBootstrap implements ServletContextListener {

    @PersistenceContext(unitName="gestRH-PU")
    private EntityManager em;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        // Because CDI injection in listeners can be subtle, fallback if em is null by JNDI is possible.
        try {
            List<CongeType> types = em.createQuery("SELECT t FROM CongeType t ORDER BY t.libelle", CongeType.class)
                    .getResultList();
            ctx.setAttribute("congesTypes", types);
        } catch (Exception ignore) {
            // If injection did not happen, you can also move this fetch to CongeListServlet and set request attr instead.
        }
    }
}
