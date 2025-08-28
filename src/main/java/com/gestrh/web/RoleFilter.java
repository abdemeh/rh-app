package com.gestrh.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebFilter("/admin/*")
public class RoleFilter implements Filter {

    @PersistenceContext(unitName = "gestRH-PU")
    private EntityManager em;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) session.getAttribute("roles");
        if (roles == null) {
            // Lazy-load roles into session
            Integer uid = (Integer) session.getAttribute("userId");
            roles = com.gestrh.web.SecurityUtil.loadRoles(em, uid);
            session.setAttribute("roles", roles);
        }

        if (!roles.contains("ADMIN")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admins only");
            return;
        }
        chain.doFilter(request, response);
    }
}
