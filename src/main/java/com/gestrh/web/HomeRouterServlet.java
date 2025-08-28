// HomeRouterServlet.java
package com.gestrh.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebServlet("") // maps to context root "/"
public class HomeRouterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        String ctx = req.getContextPath();
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(ctx + "/login.jsp");
            return;
        }
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) s.getAttribute("roles");
        if (roles != null && roles.contains("ADMIN")) {
            resp.sendRedirect(ctx + "/admin/");
        } else {
            resp.sendRedirect(ctx + "/index.jsp");
        }
    }
}
