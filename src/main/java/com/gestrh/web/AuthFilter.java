package com.gestrh.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/secure/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getSession(false) == null || req.getSession().getAttribute("userId") == null) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        chain.doFilter(request, response);
    }
}
